package club.tifality.module.impl.movement;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.manager.event.impl.player.StepEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.movement.MovementUtils;
import club.tifality.utils.server.ServerUtils;
import club.tifality.utils.timer.TimerUtil;
import club.tifality.utils.Wrapper;

@ModuleInfo(label = "Flight", category = ModuleCategory.MOVEMENT)
public final class Flight extends Module {

    private static final int MAX_ENDER_PEARL_SCAN_DIST = 20;

    private static final long PEARL_DISABLE_DURATION = 2000L;

    private static final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(
            new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);

    private final EnumProperty<FlightMode> flightModeProperty = new EnumProperty<>("Mode", FlightMode.MOTION);
    private final Property<Boolean> viewBobbingProperty = new Property<>("View Bobbing", true);
    private final Property<Boolean> pearlFlyProperty = new Property<>("Pearl Exploit", true);
    private final Property<Boolean> toggleAuraProperty = new Property<>("Toggle Aura", true);
    private final Property<Boolean> timerProperty = new Property<>("Timer", true);
    private final DoubleProperty speedProperty = new DoubleProperty("Speed", 2.5, 0.1, 5.0, 0.05);

    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil disabledTimer = new TimerUtil();

    private long estimatedTimeUntilLanded;
    private double distWhenThrown;
    private boolean killauraWasEnabled;
    private KillAura aura;
    private boolean pearlAirBourne;
    private boolean isThrowing;
    private boolean hasLanded;
    private int pearlSlot;

    @Listener
    public void onStepEvent(StepEvent event) {
        event.setStepHeight(0.0F);
    }

    @Listener(Priority.HIGH)
    public void MoveEntityEvent(MoveEntityEvent event) {
        switch (flightModeProperty.getValue()) {
            case MOTION:
                if (pearlFlyProperty.getValue() && !hasLanded) {
                    event.setCancelled();
                    return;
                }

                if (MovementUtils.isMoving())
                    MovementUtils.setSpeed(event, speedProperty.getValue());
                break;
        }
    }

    @Listener(Priority.HIGH)
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        if (event.isPre()) {
            if (pearlFlyProperty.getValue()) {
                if (disabledTimer.hasElapsed(PEARL_DISABLE_DURATION)) {
                    toggle();
                    return;
                }

                if (!pearlAirBourne && !hasLanded) {
                    final int pearlStackSlot = findPearlsInHotBar();

                    if (pearlStackSlot == -1) {
                        Tifality.getInstance().getNotificationManager().add(new Notification(
                                "Pearl Fly",
                                "You must have pearls on your hotbar",
                                NotificationType.ERROR));
                        toggle();
                        return;
                    }

                    if (!isThrowing) {
                        final double dist = MovementUtils.estimateDistFromGround(MAX_ENDER_PEARL_SCAN_DIST);

                        if (dist < MAX_ENDER_PEARL_SCAN_DIST) {
                            final boolean needSwitch = Wrapper.getPlayer().inventory.currentItem != pearlStackSlot;

                            if (needSwitch)
                                Wrapper.sendPacketDirect(new C09PacketHeldItemChange(pearlStackSlot));

                            distWhenThrown = dist;
                            pearlSlot = pearlStackSlot;
                            isThrowing = true;
                            event.setPitch(90.0F);
                        }
                    } else {
                        if (pearlStackSlot != pearlSlot) {
                            toggle();
                            return;
                        }

                        estimatedTimeUntilLanded = (long) (((distWhenThrown / 20) / 1.5) * 1000);
                        Wrapper.sendPacketDirect(BLOCK_PLACEMENT);
                        final int physicalHeldItem = Wrapper.getPlayer().inventory.currentItem;
                        if (pearlSlot != physicalHeldItem)
                            Wrapper.sendPacketDirect(new C09PacketHeldItemChange(physicalHeldItem));
                        isThrowing = false;
                        pearlAirBourne = true;
                        disabledTimer.reset();
                    }
                }

                if (pearlAirBourne && disabledTimer.hasElapsed(estimatedTimeUntilLanded + ServerUtils.getPingToCurrentServer() * 2)) {
                    Tifality.getInstance().getNotificationManager().add(new Notification(
                            "Pearl Fly",
                            String.format("You can now fly for %ss", PEARL_DISABLE_DURATION / 1000),
                            PEARL_DISABLE_DURATION,
                            NotificationType.SUCCESS));
                    hasLanded = true;
                    disabledTimer.reset();
                    pearlAirBourne = false;
                }
            }

            final EntityPlayerSP player = Wrapper.getPlayer();

            if (viewBobbingProperty.getValue())
                player.cameraYaw = 0.105F;

            switch (flightModeProperty.getValue()) {
                case MOTION:
                    if (Wrapper.getGameSettings().keyBindJump.isKeyDown()) {
                        player.motionY = 1.0F;
                    } else if (Wrapper.getGameSettings().keyBindSneak.isKeyDown()) {
                        player.motionY = -1.0F;
                    } else {
                        Wrapper.getPlayer().motionY = 0.0D;
                    }
                    break;
            }
        }
    }

    public Flight() {
        setSuffixListener(flightModeProperty);
    }

    @Override
    public void onEnable() {
        Step step = ModuleManager.getInstance(Step.class);
        step.cancelStep = true;
        isThrowing = false;
        hasLanded = false;
        pearlAirBourne = false;
        pearlSlot = -1;

        disabledTimer.reset();
        timer.reset();

        if (aura == null)
            aura = ModuleManager.getInstance(KillAura.class);

        if (toggleAuraProperty.getValue() && (killauraWasEnabled = aura.isEnabled()))
            aura.toggle();
    }

    private int findPearlsInHotBar() {
        for (int i = 36; i < 45; i++) {
            ItemStack stack = Wrapper.getStackInSlot(i);

            if (stack != null && stack.getItem() instanceof ItemEnderPearl)
                return i - 36;
        }

        return -1;
    }

    @Override
    public void onDisable() {
        Step step = ModuleManager.getInstance(Step.class);
        step.cancelStep = false;
        Wrapper.getTimer().timerSpeed = 1.0F;

        if (toggleAuraProperty.getValue() && (killauraWasEnabled && !aura.isEnabled()))
            aura.toggle();
    }

    private enum FlightMode {
        MOTION
    }
}
