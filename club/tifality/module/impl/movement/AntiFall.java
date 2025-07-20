package club.tifality.module.impl.movement;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import club.tifality.module.Module;
import club.tifality.property.impl.DoubleProperty;

@ModuleInfo(label="AntiFall", category= ModuleCategory.MOVEMENT)
public class AntiFall extends Module {
    private final DoubleProperty fallDist = new DoubleProperty("Fall Dist", 3.0, 1.0, 6.0, 0.5);
    private final TimerUtil timer = new TimerUtil();
    private boolean saveMe;

    @Listener
    public void onMove(MoveEntityEvent event) {
        if (this.saveMe && this.timer.hasTimePassed(150L) || mc.thePlayer.isCollidedVertically) {
            this.saveMe = false;
            this.timer.reset();
        }
        if (mc.thePlayer.fallDistance > fallDist.get() && !this.isBlockUnder()) {
            if (!this.saveMe) {
                this.saveMe = true;
                this.timer.reset();
            }
            mc.thePlayer.fallDistance = 0.0f;
            mc.thePlayer.sendQueue.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 12.0, mc.thePlayer.posZ, false));
        }
    }

    private boolean isBlockUnder() {
        if (mc.thePlayer.posY < 0.0) {
            return false;
        }
        for (int off = 0; off < (int)mc.thePlayer.posY + 2; off += 2) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, -off, 0.0);
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) continue;
            return true;
        }
        return false;
    }
}

