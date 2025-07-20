package club.tifality.module.impl.movement;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketReceiveEvent;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;
import club.tifality.module.ModuleManager;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.movement.MovementUtils;
import club.tifality.utils.movement.PlayerInfoCache;
import club.tifality.utils.server.ServerUtils;
import club.tifality.utils.Wrapper;

@ModuleInfo(label = "Long Jump", category = ModuleCategory.MOVEMENT)
public final class LongJump extends Module {

    private final EnumProperty<LongJumpMode> longJumpModeProperty = new EnumProperty<>("Mode", LongJumpMode.WATCHDOG);
    private final Property<Boolean> waitProperty = new Property<>("Wait", true, this::isWatchdog);
    private final DoubleProperty boostProperty = new DoubleProperty("Boost", 0.5, this::isWatchdog, 0.0, 5, 0.1);
    private final DoubleProperty consistencyProperty = new DoubleProperty("Consistency", 0.5, this::isWatchdog, 0.0, 1, 0.01);
    private final DoubleProperty timerProperty = new DoubleProperty("Timer", 1.3, this::isWatchdog, 1.0, 1.5, 0.01);

    private double moveSpeed;
    private int groundTicks;
    private boolean damaged;

    @Listener
    public void onPacketReceive(PacketReceiveEvent event) {
        final Packet<?> packet = event.getPacket();

        if (isWatchdog() && waitProperty.getValue()) {
            if (ServerUtils.isOnHypixel() && packet instanceof S27PacketExplosion) {
                damaged = true;
                Tifality.getInstance().getNotificationManager().add(new Notification("Fly Bypass", "You will now be able to fly for 0.95s", 950L, NotificationType.SUCCESS));
            } else if (packet instanceof S12PacketEntityVelocity) {
                final S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;

                if (velocityPacket.getEntityID() == Wrapper.getPlayer().getEntityId()) {
                    damaged = true;
                }
            }
        }
    }
    /*@EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        final Packet<?> packet = event.getPacket();

        if (isWatchdog() && waitProperty.getValue()) {
            if (ServerUtils.isOnHypixel() && packet instanceof S27PacketExplosion) {
                damaged = true;
                RadiumClient.getInstance().getNotificationManager().add(new Notification("Fly Bypass", "You will now be able to fly for 0.95s", 950L, NotificationType.SUCCESS));
            } else if (packet instanceof S12PacketEntityVelocity) {
                final S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;

                if (velocityPacket.getEntityID() == Wrapper.getPlayer().getEntityId()) {
                    damaged = true;
                }
            }
        }
    };*/
    private int stage;
    @Listener
    public void onMoveEntityEvent(MoveEntityEvent event) {
        if (waitProperty.getValue() && !damaged) {
            event.setCancelled();
            return;
        }

        if (MovementUtils.isMoving()) {
            final double baseMoveSpeed = PlayerInfoCache.getBaseMoveSpeed();
            final double lastDist = PlayerInfoCache.getLastDist();

            switch (stage) {
                case 0:
                    if (MovementUtils.isOnGround()) {
                        moveSpeed = baseMoveSpeed * MovementUtils.MAX_DIST;
                        if (MovementUtils.isOnIce())
                            moveSpeed *= MovementUtils.ICE_MOD;
                        event.setY(Wrapper.getPlayer().motionY = MovementUtils.getJumpHeight());
                    }
                    break;
                case 1:
                    moveSpeed *= 1 + ((boostProperty.getValue() / 10) - (MovementUtils.getSpeedModifier() * 0.1));
                    break;
                case 2:
                    final double difference = (consistencyProperty.getValue() + MovementUtils.getJumpBoostModifier() * 0.05) * (lastDist - baseMoveSpeed);
                    moveSpeed = lastDist - difference;
                    break;
                default:
                    moveSpeed = PlayerInfoCache.getFriction(moveSpeed);
                    break;
            }

            MovementUtils.setSpeed(event, Math.max(moveSpeed, baseMoveSpeed));
            stage++;
        }
    }
    /*@EventLink
    public final Listener<MoveEntityEvent> onMoveEntityEvent = e -> {
        if (waitProperty.getValue() && !damaged) {
            e.setCancelled();
            return;
        }

        if (MovementUtils.isMoving()) {
            final double baseMoveSpeed = PlayerInfoCache.getBaseMoveSpeed();
            final double lastDist = PlayerInfoCache.getLastDist();

            switch (stage) {
                case 0:
                    if (MovementUtils.isOnGround()) {
                        moveSpeed = baseMoveSpeed * MovementUtils.MAX_DIST;
                        if (MovementUtils.isOnIce())
                            moveSpeed *= MovementUtils.ICE_MOD;
                        e.setY(Wrapper.getPlayer().motionY = MovementUtils.getJumpHeight());
                    }
                    break;
                case 1:
                    moveSpeed *= 1 + ((boostProperty.getValue() / 10) - (MovementUtils.getSpeedModifier() * 0.1));
                    break;
                case 2:
                    final double difference = (consistencyProperty.getValue() + MovementUtils.getJumpBoostModifier() * 0.05) * (lastDist - baseMoveSpeed);
                    moveSpeed = lastDist - difference;
                    break;
                default:
                    moveSpeed = PlayerInfoCache.getFriction(moveSpeed);
                    break;
            }

            MovementUtils.setSpeed(e, Math.max(moveSpeed, baseMoveSpeed));
            stage++;
        }
    };*/
    private int flyingTicks;
    @Listener
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        if (event.isPre()) {
            if (waitProperty.getValue()) {
                if (waitProperty.getValue() && !damaged) {
                    return;
                }

                if (Wrapper.getPlayer().fallDistance >= 1.0F) {
                    toggle();
                    return;
                }

                if (stage > 4) {
                    Wrapper.getTimer().timerSpeed = timerProperty.getValue().floatValue();

                    flyingTicks++;

//                    if (flyingTicks % 2 == 0) {
//                        Wrapper.getPlayer().motionY = 0.0;
//                        e.setPosY(e.getPosY() - 0.003F);
//                    }
                }
            }

            if (MovementUtils.isMoving() && MovementUtils.isOnGround() && ++groundTicks >= 1) {
                toggle();
            }
        }
    }
    /*@EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = e -> {
        if (e.isPre()) {
            if (waitProperty.getValue()) {
                if (waitProperty.getValue() && !damaged) {
                    return;
                }

                if (Wrapper.getPlayer().fallDistance >= 1.0F) {
                    toggle();
                    return;
                }

                if (stage > 4) {
                    Wrapper.getTimer().timerSpeed = timerProperty.getValue().floatValue();

                    flyingTicks++;

//                    if (flyingTicks % 2 == 0) {
//                        Wrapper.getPlayer().motionY = 0.0;
//                        e.setPosY(e.getPosY() - 0.003F);
//                    }
                }
            }

            if (MovementUtils.isMoving() && MovementUtils.isOnGround() && ++groundTicks >= 1) {
                toggle();
            }
        }
    };*/

    public LongJump() {
        setSuffixListener(longJumpModeProperty);
    }

    @Override
    public void onEnable() {
        Step step = ModuleManager.getInstance(Step.class);
        step.cancelStep = true;
        groundTicks = 0;
        stage = 0;
        damaged = false;
        flyingTicks = 0;
    }

    @Override
    public void onDisable() {
        Wrapper.getTimer().timerSpeed = 1.0F;
        Wrapper.getPlayer().motionX = 0;
        Wrapper.getPlayer().motionZ = 0;
        Step step = ModuleManager.getInstance(Step.class);
        step.cancelStep = false;
    }

    private boolean isWatchdog() {
        return longJumpModeProperty.getValue() == LongJumpMode.WATCHDOG;
    }

    private enum LongJumpMode {
        NCP, WATCHDOG
    }

}
