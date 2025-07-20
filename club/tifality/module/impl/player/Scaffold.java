package club.tifality.module.impl.player;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;
import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.manager.event.impl.player.SafeWalkEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.Wrapper;
import club.tifality.utils.inventory.InventoryUtils;
import club.tifality.utils.movement.MovementUtils;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.LockedResolution;
import club.tifality.utils.render.RenderingUtils;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.impl.movement.Speed;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ModuleInfo(label="Scaffold", category= ModuleCategory.MOVEMENT)
public final class Scaffold extends Module {
    private final Property<Boolean> swingProperty = new Property<>("Swing", false);
    private final Property<Boolean> safeWalkProperty = new Property<>("Safe Walk", false);
    public final Property<Boolean> keepYValue = new Property<>("Keep Y", false);
    public final Property<Boolean> sprintValue = new Property<>("Sprint", true);
    private final Property<Boolean> towerProperty = new Property<>("Tower", true);
    public final EnumProperty<TowerMode> towerMode = new EnumProperty<>("Tower mode", TowerMode.Taco, this.towerProperty::get);
    private final DoubleProperty jumpMotionValue = new DoubleProperty("Jump Motion", 0.3681288957595825, () -> this.towerMode.get() == TowerMode.Jump, 0.3681288957595825, (double)0.79f, 5.0E-4);
    private final Property<Boolean> baseMoveSpeed = new Property<>("Base Move Speed", true);
    private final Property<Boolean> moveTowerProperty = new Property<>("Move Tower", true, this.towerProperty::get);
    private final DoubleProperty maxAngleChangeProperty = new DoubleProperty("Turn Speed", 45.0, 1.0, 180.0, 1.0);
    private final DoubleProperty timerValue = new DoubleProperty("Timer", 1.0, 0.9f, 1.5, 0.05f);
    private final DoubleProperty modifierSpeed = new DoubleProperty("Modifier Speed", 1.0, 0.8f, 1.5, 0.05f);
    private final DoubleProperty baseSpeedValue = new DoubleProperty("Move Speed", 0.22f, this.baseMoveSpeed::get, 0.0, (double)0.28f, 0.01);
    private final Property<Boolean> pickerValue = new Property<>("Picker", true);
    public final EnumProperty<ModeValue> counterModeValue = new EnumProperty<>("Counter mode", ModeValue.NUMBER);
    private final Property<Boolean> noBob = new Property<>("No bob", false);
    private final DoubleProperty blockSlotProperty = new DoubleProperty("Block Slot", 9.0, 1.0, 9.0, 1.0);
    private int blockCount;
    private int originalHotBarSlot;
    private int bestBlockStack;
    private BlockData data;
    private float[] angles;
    private static final List<Block> blacklistedBlocks = Arrays.asList(Blocks.air,
            Blocks.water,
            Blocks.flowing_water,
            Blocks.lava,
            Blocks.flowing_lava,
            Blocks.ender_chest,
            Blocks.enchanting_table,
            Blocks.stone_button,
            Blocks.wooden_button,
            Blocks.crafting_table,
            Blocks.beacon,
            Blocks.furnace,
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.iron_bars,
            Blocks.cactus,
            Blocks.ladder);
    private final TimerUtil sigmaTimer = new TimerUtil();
    double oldY = 0.0;
    private int sigmaY = 0;
    private double jumpGround = 0.0;

    @Listener
    public void onSafeWalkEvent(SafeWalkEvent event) {
        event.setCancelled(this.safeWalkProperty.getValue());
    }

    @Listener(value= Priority.HIGH)
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        mc.getTimer().timerSpeed = this.timerValue.get().floatValue();
        if (this.noBob.get()) {
            mc.thePlayer.distanceWalkedModified = 0.0f;
        }
        if (event.isPre()) {
            this.updateBlockCount();
            this.data = null;
            int n = this.bestBlockStack = this.pickerValue.get() ? findBestBlockStack() : InventoryUtils.findAutoBlockBlock();
            if (this.bestBlockStack != -1) {
                BlockPos blockUnder;
                BlockData data;
                if (this.bestBlockStack < 36) {
                    int blockSlot;
                    boolean override = true;
                    for (blockSlot = 44; blockSlot >= 36; --blockSlot) {
                        ItemStack stack = Wrapper.getStackInSlot(blockSlot);
                        if (InventoryUtils.isValid(stack)) continue;
                        InventoryUtils.windowClick(this.bestBlockStack, blockSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                        this.bestBlockStack = blockSlot;
                        override = false;
                        break;
                    }
                    if (override) {
                        blockSlot = this.blockSlotProperty.getValue().intValue() - 1;
                        InventoryUtils.windowClick(this.bestBlockStack, blockSlot, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                        this.bestBlockStack = blockSlot + 36;
                    }
                }
                if ((data = this.getBlockData(blockUnder = this.getBlockUnder())) == null) {
                    data = this.getBlockData(blockUnder.offset(EnumFacing.DOWN));
                }
                if (data != null && this.bestBlockStack >= 36) {
                    if (validateReplaceable(data) && data.hitVec != null) {
                        this.angles = this.getRotations(event, data.hitVec, this.maxAngleChangeProperty.get().floatValue());
                    } else {
                        data = null;
                    }
                }
                if (this.angles != null) {
                    if (this.towerProperty.getValue() && Wrapper.getGameSettings().keyBindJump.isKeyDown()) {
                        this.tower();
                    }
                    event.setYaw(this.angles[0]);
                    event.setPitch(this.angles[1]);
                }
                this.data = data;
            }
        } else if (this.data != null && this.bestBlockStack != -1 && this.bestBlockStack >= 36) {
            int hotBarSlot = this.bestBlockStack - 36;
            if (Wrapper.getPlayer().inventory.currentItem != hotBarSlot) {
                Wrapper.getPlayer().inventory.currentItem = hotBarSlot;
            }
            assert (this.data.hitVec != null);
            if (Wrapper.getPlayerController().onPlayerRightClick(Wrapper.getPlayer(), Wrapper.getWorld(), Wrapper.getPlayer().getCurrentEquippedItem(), this.data.pos, this.data.face, this.data.hitVec)) {
                if (this.towerProperty.getValue() && Wrapper.getGameSettings().keyBindJump.isKeyDown() && MovementUtils.isMoving()) {
                    if (this.towerMode.get() == TowerMode.Taco) {
                        double n = event.getPosY() % 1.0;
                        double n2 = this.down(event.getPosY());
                        List<Double> list = Arrays.asList(0.41999998688698, 0.7531999805212);
                        if (n > 0.419 && n < 0.753) {
                            event.setPosY(n2 + list.get(0));
                        } else if (n > 0.753) {
                            event.setPosY(n2 + list.get(1));
                        } else {
                            event.setPosY(n2);
                            event.setOnGround(true);
                        }
                        if (!MovementUtils.isMove()) {
                            mc.thePlayer.motionZ = 0.0;
                            mc.thePlayer.motionX = 0.0;
                            event.setPosX(event.getPosX() + (mc.thePlayer.ticksExisted % 2 == 0 ? ThreadLocalRandom.current().nextDouble(0.06, 0.0625) : -ThreadLocalRandom.current().nextDouble(0.06, 0.0625)));
                            event.setPosZ(event.getPosZ() + (mc.thePlayer.ticksExisted % 2 != 0 ? ThreadLocalRandom.current().nextDouble(0.06, 0.0625) : -ThreadLocalRandom.current().nextDouble(0.06, 0.0625)));
                        }
                    }
                    event.setPitch(90.0f);
                }
                if (this.swingProperty.getValue()) {
                    Wrapper.getPlayer().swingItem();
                } else {
                    Wrapper.sendPacket(new C0APacketAnimation());
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionX *= this.modifierSpeed.get();
                    mc.thePlayer.motionZ *= this.modifierSpeed.get();
                }
            }
        }
        if (this.keepYValue.get()) {
            if (!MovementUtils.isMove() && mc.gameSettings.keyBindJump.isKeyDown() || mc.thePlayer.isCollidedVertically || mc.thePlayer.onGround) {
                this.sigmaY = MathHelper.floor_double(mc.thePlayer.posY);
            }
        } else {
            this.sigmaY = MathHelper.floor_double(mc.thePlayer.posY);
        }
        if (this.keepYValue.get() && this.oldY >= mc.thePlayer.posY) {
            mc.thePlayer.jump();
        }
    }

    @Listener
    private void onMoveEntityEvent(MoveEntityEvent event) {
        if (this.baseMoveSpeed.get() && MovementUtils.isMoving() && !this.baseSpeedValue.get().equals(0.0) && !Tifality.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
            MovementUtils.setMotion(event, this.baseSpeedValue.get());
        }
    }

    private static int findBestBlockStack() {
        int bestSlot = -1;
        int blockCount = -1;
        for (int i = 44; i >= 9; --i) {
            ItemStack stack = Wrapper.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemBlock) || !InventoryUtils.isGoodBlockStack(stack) || stack.stackSize <= blockCount) continue;
            bestSlot = i;
            blockCount = stack.stackSize;
        }
        return bestSlot;
    }

    private BlockPos getBlockUnder() {
        return this.keepYValue.get() ? new BlockPos(mc.thePlayer.posX, (double)this.sigmaY - 1.0, mc.thePlayer.posZ) : (mc.thePlayer.posY == mc.thePlayer.posY + 0.5 ? new BlockPos(mc.thePlayer) : new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down());
    }

    private float[] getRotations(UpdatePositionEvent ev, Vec3 hitVec, float aimSpeed) {
        EntityPlayerSP entity = Wrapper.getPlayer();
        double x = hitVec.xCoord - entity.posX;
        double y = hitVec.yCoord - (entity.posY + (double)entity.getEyeHeight());
        double z = hitVec.zCoord - entity.posZ;
        double fDist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = interpolateRotation(ev.getPrevYaw(), (float)(StrictMath.atan2(z, x) * 180.0 / Math.PI) - 90.0f, aimSpeed);
        float pitch = interpolateRotation(ev.getPrevPitch(), (float)(-(StrictMath.atan2(y, fDist) * 180.0 / Math.PI)), aimSpeed);
        return new float[]{yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f)};
    }

    private static float interpolateRotation(float prev, float now, float maxTurn) {
        float speed = MathHelper.wrapAngleTo180_float(now - prev);
        if (speed > maxTurn) {
            speed = maxTurn;
        }
        if (speed < -maxTurn) {
            speed = -maxTurn;
        }
        return prev + speed;
    }

    private static boolean validateReplaceable(BlockData data) {
        BlockPos pos = data.pos.offset(data.face);
        WorldClient world = Wrapper.getWorld();
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    private boolean isPosSolid(Block block) {
        return !blacklistedBlocks.contains(block) && (block.getMaterial().isSolid() || !block.isTranslucent() || block.isVisuallyOpaque() || block instanceof BlockLadder || block instanceof BlockCarpet || block instanceof BlockSnow || block instanceof BlockSkull) && !block.getMaterial().isLiquid() && !(block instanceof BlockContainer);
    }

    private BlockData getBlockData(BlockPos pos) {
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock())) {
            return new BlockData(pos.add(0, -1, 0), EnumFacing.UP);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock())) {
            return new BlockData(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock())) {
            return new BlockData(pos.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock())) {
            return new BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH);
        }
        BlockPos add = pos.add(0, 0, 0);
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(1, 0, 0)).getBlock())) {
            return new BlockData(add.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(0, 0, -1)).getBlock())) {
            return new BlockData(add.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(0, 0, 1)).getBlock())) {
            return new BlockData(add.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add2 = pos.add(1, 0, 0);
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add2.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(1, 0, 0)).getBlock())) {
            return new BlockData(add2.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(0, 0, -1)).getBlock())) {
            return new BlockData(add2.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(0, 0, 1)).getBlock())) {
            return new BlockData(add2.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add3 = pos.add(0, 0, -1);
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add3.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(1, 0, 0)).getBlock())) {
            return new BlockData(add3.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(0, 0, -1)).getBlock())) {
            return new BlockData(add3.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(0, 0, 1)).getBlock())) {
            return new BlockData(add3.add(0, 0, 1), EnumFacing.NORTH);
        }
        BlockPos add4 = pos.add(0, 0, 1);
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(-1, 0, 0)).getBlock())) {
            return new BlockData(add4.add(-1, 0, 0), EnumFacing.EAST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(1, 0, 0)).getBlock())) {
            return new BlockData(add4.add(1, 0, 0), EnumFacing.WEST);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(0, 0, -1)).getBlock())) {
            return new BlockData(add4.add(0, 0, -1), EnumFacing.SOUTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(0, 0, 1)).getBlock())) {
            return new BlockData(add4.add(0, 0, 1), EnumFacing.NORTH);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(1, 1, 0)).getBlock())) {
            return new BlockData(add.add(1, 1, 0), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add.add(-1, 2, -1)).getBlock())) {
            return new BlockData(add.add(-1, 2, -1), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(-2, 1, 0)).getBlock())) {
            return new BlockData(add2.add(-2, 1, 0), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add2.add(0, 2, 1)).getBlock())) {
            return new BlockData(add2.add(0, 2, 1), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(0, 1, 2)).getBlock())) {
            return new BlockData(add3.add(0, 1, 2), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add3.add(1, 2, 0)).getBlock())) {
            return new BlockData(add3.add(1, 2, 0), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(0, 1, -2)).getBlock())) {
            return new BlockData(add4.add(0, 1, -2), EnumFacing.DOWN);
        }
        if (this.isPosSolid(mc.theWorld.getBlockState(add4.add(-1, 2, 0)).getBlock())) {
            return new BlockData(add4.add(-1, 2, 0), EnumFacing.DOWN);
        }
        return null;
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        this.blockCount = 0;
        this.originalHotBarSlot = Wrapper.getPlayer().inventory.currentItem;
        this.oldY = mc.thePlayer.posY;
        if (this.keepYValue.get()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
            this.sigmaTimer.reset();
        }
    }

    @Override
    public void onDisable() {
        this.angles = null;
        Wrapper.getPlayer().inventory.currentItem = this.originalHotBarSlot;
    }

    public boolean isRotating() {
        return this.angles != null;
    }

    private void updateBlockCount() {
        this.blockCount = 0;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = Wrapper.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemBlock) || !InventoryUtils.isGoodBlockStack(stack)) continue;
            this.blockCount += stack.stackSize;
        }
    }

    @Listener
    public void onRender2DEvent(Render2DEvent event) {
        int c = Colors.getColor(255, 0, 0, 255);
        if (this.blockCount >= 64 && 128 > this.blockCount) {
            c = Colors.getColor(255, 255, 0, 255);
        } else if (this.blockCount >= 128) {
            c = Colors.getColor(0, 255, 0, 255);
        }
        if (this.counterModeValue.get() == ModeValue.RECT) {
            LockedResolution resolution = event.getResolution();
            float x = (float)resolution.getWidth() / 2.0f;
            float y = (float)resolution.getHeight() / 2.0f + 15.0f;
            float percentage = Math.min(1.0f, (float)this.blockCount / 128.0f);
            float width = 80.0f;
            float half = width / 2.0f;
            int color = RenderingUtils.getColorFromPercentage(percentage);
            Gui.drawRect(x - half - 0.5f, y - 2.0f, x + half + 0.5f, y + 2.0f, 0x78000000);
            Gui.drawGradientRect(x - half, y - 1.5f, x - half + width * percentage, y + 1.5f, color, new Color(color).darker().getRGB());
        } else {
            ScaledResolution res = new ScaledResolution(mc);
            String info = "" + this.blockCount;
            GlStateManager.enableBlend();
            RenderingUtils.drawOutlinedString(info, (float)res.getScaledWidth() / 2.0f - (float)mc.fontRendererObj.getStringWidth(info) / 2.0f, (float)res.getScaledHeight() / 2.0f - 25.0f, 1.0f, c, new Color(0, 0, 0, 255).getRGB());
            GlStateManager.disableBlend();
        }
    }

    private void tower() {
        if (this.towerMode.get() == TowerMode.Jump) {
            this.fakeJump();
            mc.thePlayer.motionY = this.jumpMotionValue.get();
        }
        if (this.towerMode.get() == TowerMode.Motion && mc.thePlayer.ticksExisted % (this.moveTowerProperty.get() && MovementUtils.isMove() ? 1 : 8) == 0) {
            if (mc.thePlayer.onGround) {
                this.fakeJump();
                this.jumpGround = mc.thePlayer.posY;
                mc.thePlayer.motionY = 0.3681289;
            }
            if (mc.thePlayer.posY > this.jumpGround + 1.0) {
                this.fakeJump();
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
                mc.thePlayer.motionY = 0.3681289;
                this.jumpGround = mc.thePlayer.posY;
            }
            if (!MovementUtils.isMove()) {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionZ = 0.0;
                mc.thePlayer.jumpMovementFactor = 0.0f;
            }
        }
        if (this.towerMode.get() == TowerMode.Taco) {
            if (!MovementUtils.isMoving()) {
                mc.thePlayer.motionX = 0.0;
                mc.thePlayer.motionZ = 0.0;
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.setPosition((double)this.down(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, (double)this.down(mc.thePlayer.posZ) + 0.5);
                }
            }
            if (MovementUtils.isOnGround(0.76) && !MovementUtils.isOnGround(0.75) && mc.thePlayer.motionY > 0.23 && mc.thePlayer.motionY < 0.25) {
                mc.thePlayer.motionY = (double)Math.round(mc.thePlayer.posY) - mc.thePlayer.posY;
            }
            if (MovementUtils.isOnGround(1.0E-4)) {
                mc.thePlayer.motionY = MovementUtils.getJumpBoostModifier(0.41999998688698, true);
            } else if (mc.thePlayer.posY >= (double)Math.round(mc.thePlayer.posY) - 1.0E-4 && mc.thePlayer.posY <= (double)Math.round(mc.thePlayer.posY) + 1.0E-4 && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.thePlayer.motionY = 0.0;
            }
        }
    }

    private void fakeJump() {
        mc.thePlayer.isAirBorne = true;
        mc.thePlayer.triggerAchievement(StatList.jumpStat);
    }

    private int down(double n) {
        int n2 = (int)n;
        try {
            if (n < (double)n2) {
                return n2 - 1;
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return n2;
    }

    public enum Rotation {
        New,
        Old,
        Dev;
    }

    public enum TowerMode {
        Taco,
        Motion,
        Jump;
    }

    public enum ModeValue {
        NUMBER,
        RECT;
    }

    private static class BlockData {
        private final BlockPos pos;
        private final EnumFacing face;
        private final Vec3 hitVec;

        public BlockData(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
            this.hitVec = this.getHitVec();
        }

        private Vec3 getHitVec() {
            Vec3i directionVec = this.face.getDirectionVec();
            double x = (double)directionVec.getX() * 0.5;
            double z = (double)directionVec.getZ() * 0.5;
            if (this.face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }
            Vec3 hitVec = new Vec3(this.pos).addVector(x + z, (double)directionVec.getY() * 0.5, x + z);
            Vec3 src = Wrapper.getPlayer().getPositionEyes(1.0f);
            MovingObjectPosition obj = Wrapper.getWorld().rayTraceBlocks(src, hitVec, false, false, true);
            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return null;
            }
            if (this.face != EnumFacing.DOWN && this.face != EnumFacing.UP) {
                obj.hitVec = obj.hitVec.addVector(0.0, -0.2, 0.0);
            }
            return obj.hitVec;
        }
    }
}

