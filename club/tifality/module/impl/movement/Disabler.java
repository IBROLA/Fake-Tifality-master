package club.tifality.module.impl.movement;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.Wrapper;

import java.util.LinkedList;

@ModuleInfo(label="Disabler", category= ModuleCategory.MOVEMENT)
public class Disabler extends Module {
    public final EnumProperty<ModeValue> modeValue = new EnumProperty<>("Mode", ModeValue.AAC);
    private final LinkedList<Packet<?>> packetQueue = new LinkedList<>();
    private C03PacketPlayer.C06PacketPlayerPosLook aac5QueuedPacket = null;
    private double aac5LastPosX = 0.0;
    private int aac5Same = 0;
    private int aac5Status = 0;
    private int aac5SameReach = 0;

    @Listener
    public void onUpdate(UpdatePositionEvent e) {
        switch (this.modeValue.get()) {
            case AAC: {
                if (mc.thePlayer.onGround) {
                    Wrapper.addChatMessage("JUMP INTO AIR AND TOGGLE THIS MODULE");
                    this.toggle();
                    return;
                }
                mc.gameSettings.keyBindForward.pressed = this.aac5Status != 1;
                mc.thePlayer.motionZ = 0.0;
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.motionX = 0.0;
                if (this.aac5Status != 1) break;
                if (this.aac5QueuedPacket != null) {
                    mc.thePlayer.sendQueue.sendPacketNoEvent(this.aac5QueuedPacket);
                    double dist = 0.13;
                    double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                    double x = -Math.sin(yaw) * dist;
                    double z = Math.cos(yaw) * dist;
                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                }
                mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.MAX_VALUE, mc.thePlayer.posZ, true));
                this.aac5QueuedPacket = new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false);
                break;
            }
            case COMBAT: {
                if (mc.thePlayer.ticksExisted % 180 != 0) break;
                while (this.packetQueue.size() > 25) {
                    mc.thePlayer.sendQueue.sendPacketNoEvent(this.packetQueue.poll());
                }
                break;
            }
            case MOVEMENT: {
                if (mc.thePlayer.ticksExisted % 180 != 0) break;
                while (this.packetQueue.size() > 22) {
                    mc.thePlayer.sendQueue.sendPacketNoEvent(this.packetQueue.poll());
                }
                break;
            }
        }
    }

    @Listener
    public void onPacket(PacketSendEvent e) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        switch (this.modeValue.get()) {
            case AAC: {
                if (!(e.getPacket() instanceof S08PacketPlayerPosLook)) break;
                S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook)e.getPacket();
                if (this.aac5Status == 0) {
                    mc.thePlayer.setPosition(packet.getX(), packet.getY(), packet.getZ());
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.getYaw(), packet.getPitch(), false));
                    if (mc.thePlayer.posX == this.aac5LastPosX) {
                        ++this.aac5Same;
                        if (this.aac5Same >= 5) {
                            this.aac5Status = 1;
                            mc.timer.timerSpeed = 0.65f;
                            this.aac5Same = 0;
                            return;
                        }
                    }
                    double x = 0.12;
                    double y = Math.toRadians(mc.thePlayer.rotationYaw);
                    mc.thePlayer.setPosition(mc.thePlayer.posX + -Math.sin(y) * x, mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(y) * x);
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                    this.aac5LastPosX = mc.thePlayer.posX;
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.MAX_VALUE, mc.thePlayer.posZ, true));
                    break;
                }
                if (!(mc.timer.timerSpeed <= 1.65f)) break;
                ++this.aac5Same;
                if (this.aac5Same < this.aac5SameReach) break;
                this.aac5Same = 0;
                this.aac5SameReach = (int)((float)this.aac5SameReach + 13.0f);
                mc.timer.timerSpeed += 0.4f;
                break;
            }
            case COMBAT: {
                if (mc.thePlayer.ticksExisted < 8) {
                    this.packetQueue.clear();
                }
                if (!(e.getPacket() instanceof C00PacketKeepAlive)) break;
                this.packetQueue.add(e.getPacket());
                e.setCancelled();
                break;
            }
            case MOVEMENT: {
                if (mc.thePlayer.ticksExisted == 0) {
                    this.packetQueue.clear();
                }
                if (e.getPacket() instanceof C03PacketPlayer) {
                    double yPos = (double)Math.round(mc.thePlayer.posY / 0.015625) * 0.015625;
                    mc.thePlayer.setPosition(mc.thePlayer.posX, yPos, mc.thePlayer.posZ);
                    if (mc.thePlayer.ticksExisted % 45 != 0) break;
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.0E159, mc.thePlayer.posZ, false));
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                    break;
                }
                if (e.getPacket() instanceof S08PacketPlayerPosLook) {
                    double z;
                    double y;
                    S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook)e.getPacket();
                    double x = packet.getX() - mc.thePlayer.posX;
                    double diff = Math.sqrt(x * x + (y = packet.getY() - mc.thePlayer.posY) * y + (z = packet.getZ() - mc.thePlayer.posZ) * z);
                    if (!(diff <= 8.0)) break;
                    e.setCancelled();
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), true));
                    break;
                }
                if (!(e.getPacket() instanceof C0FPacketConfirmTransaction)) break;
                for (int i = 0; i < 4; ++i) {
                    this.packetQueue.add(e.getPacket());
                }
                e.setCancelled();
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (this.modeValue.get() == ModeValue.AAC) {
            if (mc.thePlayer.onGround) {
                Wrapper.addChatMessage("JUMP INTO AIR AND TOGGLE THIS MODULE");
                this.toggle();
                return;
            }
            mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, Double.MAX_VALUE, mc.thePlayer.posZ, true));
            this.aac5LastPosX = 0.0;
            this.aac5QueuedPacket = null;
            this.aac5Status = 0;
            this.aac5Same = 0;
            this.aac5SameReach = 5;
        }
        this.packetQueue.clear();
    }

    public enum ModeValue {
        AAC,
        COMBAT,
        MOVEMENT;

    }
}

