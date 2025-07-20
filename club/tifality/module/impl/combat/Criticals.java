package club.tifality.module.impl.combat;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import club.tifality.manager.event.impl.player.AttackEvent;
import club.tifality.module.impl.movement.Flight;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C03PacketPlayer;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.Representation;
import club.tifality.utils.RandomUtils;
import club.tifality.utils.timer.TimerUtil;

@ModuleInfo(label="Criticals", category= ModuleCategory.COMBAT)
public final class Criticals extends Module {
    private final DoubleProperty delayProperty = new DoubleProperty("Delay", 500.0, () -> this.criticalsModeProperty.getValue() != CriticalsMode.GROUND, 0.0, 1000.0, 10.0, Representation.MILLISECONDS);
    private final DoubleProperty hurtTimeProperty = new DoubleProperty("Hurt Time", 10.0, () -> this.criticalsModeProperty.getValue() != CriticalsMode.GROUND, 0.0, 20.0, 1.0);
    public final EnumProperty<CriticalsMode> criticalsModeProperty = new EnumProperty<>("Mode", CriticalsMode.WATCHDOG);
    private final TimerUtil timer = new TimerUtil();

    @Listener
    public void onAttackEvent(AttackEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer target = (EntityPlayer)event.getEntity();
            if (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder() || mc.thePlayer.isInWeb || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.ridingEntity != null || (double)target.hurtTime > this.hurtTimeProperty.get() || !this.timer.hasElapsed(this.delayProperty.get().longValue()) || ModuleManager.getInstance(Flight.class).isEnabled()) {
                return;
            }
            if (this.criticalsModeProperty.get() == CriticalsMode.WATCHDOG) {
                double random = RandomUtils.getRandom(4.0E-7, 4.0E-5);
                double[] arrayOfDouble = new double[]{0.007017625 + random, 0.007349825 + random, 0.006102874 + random};
                for (double value : arrayOfDouble) {
                    mc.getNetHandler().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + value, mc.thePlayer.posZ, false));
                }
                mc.thePlayer.onCriticalHit(target);
            }
            if (this.criticalsModeProperty.get() == CriticalsMode.TACO) {
                for (int i = 0; i <= 2; ++i) {
                    mc.getNetHandler().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.101 - (double)i * 0.02, mc.thePlayer.posZ, false));
                }
            }
        }
    }

    @Listener
    public void onPacketSendEvent(PacketSendEvent event) {
        if (this.criticalsModeProperty.get() == CriticalsMode.GROUND && event.getPacket() instanceof C03PacketPlayer) {
            ((C03PacketPlayer)event.getPacket()).onGround = false;
        }
    }

    public Criticals() {
        this.setSuffixListener(this.criticalsModeProperty);
    }

    @Override
    public void onEnable() {
        this.timer.reset();
        if (this.criticalsModeProperty.get() == CriticalsMode.GROUND) {
            mc.thePlayer.jump();
        }
    }

    public enum CriticalsMode {
        TACO(new double[]{0.0}),
        WATCHDOG(new double[]{0.0}),
        GROUND(new double[]{0.0});

        private final double[] offsets;

        CriticalsMode(double[] offsets) {
            this.offsets = offsets;
        }
    }
}

