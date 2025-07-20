package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.EnumProperty;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;

@ModuleInfo(label="SpeedMine", category= ModuleCategory.PLAYER)
public class SpeedMine extends Module {
    public final EnumProperty<ModeValue> modeValue = new EnumProperty<>("Mode", ModeValue.HYPIXEL);
    private boolean bzs = false;
    private float bzx = 0.0f;
    public BlockPos blockPos;
    public EnumFacing facing;
    public static float speed;
    public static int delay;

    @Listener
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (this.modeValue.get() == ModeValue.HYPIXEL && packet instanceof C07PacketPlayerDigging && mc.playerController != null) {
            C07PacketPlayerDigging c07PacketPlayerDigging = (C07PacketPlayerDigging)packet;
            if (c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                this.bzs = true;
                this.blockPos = c07PacketPlayerDigging.getPosition();
                this.facing = c07PacketPlayerDigging.getFacing();
                this.bzx = 0.0f;
            } else if (c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK || c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                this.bzs = false;
                this.blockPos = null;
                this.facing = null;
            }
        }
        if (this.modeValue.get() == ModeValue.POTION) {
            mc.thePlayer.addPotionEffect(new PotionEffect(Potion.digSpeed.getId(), 100, mc.thePlayer.getCurrentEquippedItem() == null ? 1 : 0));
        }
    }

    @Listener
    public void onUpdate(UpdatePositionEvent event) {
        if (this.modeValue.get() == ModeValue.HYPIXEL) {
            if (mc.playerController.extendedReach()) {
                mc.playerController.blockHitDelay = 0;
            } else if (this.bzs) {
                Block block = mc.theWorld.getBlockState(this.blockPos).getBlock();
                this.bzx = (float)((double)this.bzx + (double)block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, this.blockPos) * 1.4);
                if (this.bzx >= 1.0f) {
                    mc.theWorld.setBlockState(this.blockPos, Blocks.air.getDefaultState(), 11);
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.blockPos, this.facing));
                    this.bzx = 0.0f;
                    this.bzs = false;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        mc.thePlayer.removePotionEffect(Potion.digSpeed.getId());
    }

    public enum ModeValue {
        HYPIXEL,
        POTION;

    }
}

