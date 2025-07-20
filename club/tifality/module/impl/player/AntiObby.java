package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import net.minecraft.block.Block;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;

@ModuleInfo(label="AntiObby", category= ModuleCategory.PLAYER)
public final class AntiObby extends Module {
    @Listener
    public void onUpdate(UpdatePositionEvent e) {
        BlockPos downPos;
        BlockPos sand = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 3.0, mc.thePlayer.posZ));
        Block sandBlock = mc.theWorld.getBlockState(sand).getBlock();
        BlockPos forge = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ));
        Block forgeBlock = mc.theWorld.getBlockState(forge).getBlock();
        BlockPos obsidianPos = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ));
        Block obsidianBlock = mc.theWorld.getBlockState(obsidianPos).getBlock();
        if (obsidianBlock == Block.getBlockById(49)) {
            this.bestTool(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ());
            downPos = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ));
            mc.playerController.onPlayerDamageBlock(downPos, EnumFacing.UP);
        }
        if (forgeBlock == Block.getBlockById(61)) {
            this.bestTool(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ());
            downPos = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ));
            mc.playerController.onPlayerDamageBlock(downPos, EnumFacing.UP);
        }
        if (sandBlock == Block.getBlockById(12) || sandBlock == Block.getBlockById(13)) {
            this.bestTool(mc.objectMouseOver.getBlockPos().getX(), mc.objectMouseOver.getBlockPos().getY(), mc.objectMouseOver.getBlockPos().getZ());
            downPos = new BlockPos(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + 3.0, mc.thePlayer.posZ));
            mc.playerController.onPlayerDamageBlock(downPos, EnumFacing.UP);
        }
    }

    public void bestTool(int x, int y, int z) {
        int blockId = Block.getIdFromBlock(mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock());
        int bestSlot = 0;
        float f = -1.0f;
        for (int i1 = 36; i1 < 45; ++i1) {
            try {
                ItemStack curSlot = mc.thePlayer.inventoryContainer.getSlot(i1).getStack();
                if (!(curSlot.getItem() instanceof ItemTool) && !(curSlot.getItem() instanceof ItemSword) && !(curSlot.getItem() instanceof ItemShears) || !(curSlot.getStrVsBlock(Block.getBlockById(blockId)) > f)) continue;
                bestSlot = i1 - 36;
                f = curSlot.getStrVsBlock(Block.getBlockById(blockId));
                continue;
            } catch (Exception exception) {
                // empty catch block
            }
        }
        if (f != -1.0f) {
            mc.thePlayer.inventory.currentItem = bestSlot;
            mc.playerController.updateController();
        }
    }
}

