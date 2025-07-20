package club.tifality.module.impl.movement;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.world.BlockCollisionEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.utils.Wrapper;

@ModuleInfo(label = "Jesus", category = ModuleCategory.MOVEMENT)
public final class Jesus extends Module {

    private boolean onLiquid;

    @Listener
    public void onUpdatePosition(UpdatePositionEvent event) {
        if (event.isPre() && onLiquid && Wrapper.getPlayer().ticksExisted % 2 == 0) {
            event.setPosY(event.getPosY() + 0.000001F);
            onLiquid = false;
        }
    }

    @Listener
    public void onBlockCollision(BlockCollisionEvent event) {
        if (event.getBlock() instanceof BlockLiquid && !Wrapper.getPlayer().isSneaking()) {
            final BlockPos blockPos = event.getBlockPos();
            final double x = blockPos.getX();
            final double y = blockPos.getY();
            final double z = blockPos.getZ();
            onLiquid = true;
            event.setBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1 - 0.000001F, z + 1));
        }
    }
    /*@EventLink
    private final Listener<UpdatePositionEvent> onUpdatePosition = event -> {
        if (event.isPre() && onLiquid && Wrapper.getPlayer().ticksExisted % 2 == 0) {
            event.setPosY(event.getPosY() + 0.000001F);
            onLiquid = false;
        }
    };

    @EventLink
    private final Listener<BlockCollisionEvent> onBlockCollision = event -> {
        if (event.getBlock() instanceof BlockLiquid && !Wrapper.getPlayer().isSneaking()) {
            final BlockPos blockPos = event.getBlockPos();
            final double x = blockPos.getX();
            final double y = blockPos.getY();
            final double z = blockPos.getZ();
            onLiquid = true;
            event.setBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1 - 0.000001F, z + 1));
        }
    };*/
}
