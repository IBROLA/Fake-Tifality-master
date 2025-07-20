package club.tifality.utils.movement;

import club.tifality.manager.event.impl.player.MoveEntityEvent;
import club.tifality.module.impl.movement.NoSlowdown;
import club.tifality.utils.InventoryUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.server.HypixelGameUtils;
import club.tifality.utils.server.ServerUtils;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import org.lwjgl.input.Keyboard;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.impl.movement.TargetStrafe;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MovementUtils {

    private static final List<Double> frictionValues = Arrays.asList(0.0, 0.0, 0.0);

    private static final double AIR_FRICTION = 0.98F;
    private static final double WATER_FRICTION = 0.89F;
    private static final double LAVA_FRICTION = 0.535F;

    private MovementUtils() {
    }

    public static final double BUNNY_SLOPE = 0.72;
    public static final double SPRINTING_MOD = 1.3F;
    public static final double SNEAK_MOD = 0.3F;
    public static final double ICE_MOD = 2.5F;
    public static final double VANILLA_JUMP_HEIGHT = 0.42F;
    public static final double WALK_SPEED = 0.221F;
    private static final double SWIM_MOD = 0.115F / WALK_SPEED;
    private static final double[] DEPTH_STRIDER_VALUES = {
        1.0F,
        0.1645F / SWIM_MOD / WALK_SPEED,
        0.1995F / SWIM_MOD / WALK_SPEED,
        1.0F / SWIM_MOD,
    };
    public static final double MAX_DIST = 1.85F;
    public static final double BUNNY_DIV_FRICTION = 160.0 - 1.0E-3;
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isBlockUnder() {
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

    public static int getJumpBoostModifier() {
        PotionEffect effect = Wrapper.getPlayer().getActivePotionEffect(Potion.jump.id);
        if (effect != null)
            return effect.getAmplifier() + 1;
        return 0;
    }

    public static double getJumpBoostModifier(double baseJumpHeight, boolean potionJumpHeight) {
        if (mc.thePlayer.isPotionActive(Potion.jump) && potionJumpHeight) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            baseJumpHeight += (float)(amplifier + 1) * 0.1f;
        }
        return baseJumpHeight;
    }

    public static int getSpeedEffect() {
        return mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;
    }

    public static float getSpeed() {
        return (float)Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    public static double getPosYForJumpTick(int tick) {
        switch (tick) {
            case 1:
                return 0.42F;
            case 2:
                return 0.7532F;
            case 3:
                return 1.00133597911214D;
            case 4:
                return 1.16610926093821D;
            case 5:
            case 6:
                return 1.24918707874468D;
            case 7:
                return 1.1707870772188D;
            case 8:
                return 1.0155550727022D;
            case 9:
                return 0.78502770378923D;
            case 10:
                return 0.48071087633169D;
            case 11:
                return 0.10408037809304D;
            default:
                return 0;
        }
    }

    public static int getSpeedModifier() {
        PotionEffect effect = Wrapper.getPlayer().getActivePotionEffect(Potion.moveSpeed.id);
        if (effect != null)
            return effect.getAmplifier() + 1;
        return 0;
    }


    private static boolean isMovingEnoughForSprint() {
        MovementInput movementInput = Wrapper.getPlayer().movementInput;
        return movementInput.moveForward > 0.8F || movementInput.moveForward < -0.8F ||
            movementInput.moveStrafe > 0.8F || movementInput.moveStrafe < -0.8F;
    }

    public static float getMovementDirection() {
        final EntityPlayerSP player = Wrapper.getPlayer();
        float forward = player.moveForward;
        float strafe = player.moveStrafing;

        float direction = 0.0f;
        if (forward < 0) {
            direction += 180;
            if (strafe > 0) {
                direction += 45;
            } else if (strafe < 0) {
                direction -= 45;
            }
        } else if (forward > 0) {
            if (strafe > 0) {
                direction -= 45;
            } else if (strafe < 0) {
                direction += 45;
            }
        } else {
            if (strafe > 0) {
                direction -= 90;
            } else if (strafe < 0) {
                direction += 90;
            }
        }

        direction += player.rotationYaw;

        return MathHelper.wrapAngleTo180_float(direction);
    }

    public static boolean isOnStairs() {
        return getBlockUnder(0.5) instanceof BlockStairs;
    }

    public static boolean isBlockAbove() {
        return Wrapper.getWorld()
            .checkBlockCollision(
                Wrapper.getPlayer().getEntityBoundingBox()
                    .addCoord(0.0D, 1.0D, 0.0D));
    }

    public static boolean isDistFromGround(double dist) {
        return Wrapper.getWorld()
                .checkBlockCollision(
                        Wrapper.getPlayer().getEntityBoundingBox()
                                .addCoord(0.0D, -dist, 0.0D));
    }

    public static double estimateDistFromGround(int maxIterations) {
        final int playerPosY = (int) Math.floor(Wrapper.getPlayer().posY);
        final int min = playerPosY - maxIterations;

        for (int i = playerPosY; i > min; i -= 2) {
            if (Wrapper.getWorld()
                    .checkBlockCollision(
                            Wrapper.getPlayer().getEntityBoundingBox()
                                    .addCoord(0.0D, -(playerPosY - i), 0.0D))) {
                return playerPosY - i;
            }
        }

        return maxIterations;
    }

    public static boolean fallDistDamage() {
        if (isBlockAbove() || !ServerUtils.isOnHypixel() || !HypixelGameUtils.hasGameStarted()) return false;
        final UpdatePositionEvent e = Wrapper.getPlayer().currentEvent;
        final double x = e.getPosX();
        final double y = e.getPosY();
        final double z = e.getPosZ();
        final double smallOffset = 0.0013F;
        final double offset = 0.0611F;
        final double packets = Math.ceil(getMinFallDist() / (offset - smallOffset));

        for (int i = 0; i < packets; i++) {
            Wrapper.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(
                x, y + offset, z,
                false));
            Wrapper.sendPacketDirect(new C03PacketPlayer.C04PacketPlayerPosition(
                x, y + smallOffset, z,
                false));
        }
        return true;
    }

    public static boolean isInLiquid() {
        return Wrapper.getPlayer().isInWater() || Wrapper.getPlayer().isInLava();
    }

    public static boolean isOverVoid() {
        for (double posY = Wrapper.getPlayer().posY; posY > 0.0; posY--) {
            if (!(Wrapper.getWorld().getBlockState(
                new BlockPos(Wrapper.getPlayer().posX, posY, Wrapper.getPlayer().posZ)).getBlock() instanceof BlockAir))
                return false;
        }

        return true;
    }

    public static double getJumpHeight() {
        double baseJumpHeight = VANILLA_JUMP_HEIGHT;
        if (isInLiquid()) {
            return WALK_SPEED * SWIM_MOD + 0.02F;
        } else if (Wrapper.getPlayer().isPotionActive(Potion.jump)) {
            return baseJumpHeight + (Wrapper.getPlayer().getActivePotionEffect(Potion.jump).getAmplifier() + 1.0F) * 0.1F;
        }
        return baseJumpHeight;
    }

    public static double getMinFallDist() {
        final boolean isSg = HypixelGameUtils.getGameMode() == HypixelGameUtils.GameMode.BLITZ_SG;
        double baseFallDist = isSg ? 4.0D : 3.0D;
        if (Wrapper.getPlayer().isPotionActive(Potion.jump))
            baseFallDist += Wrapper.getPlayer().getActivePotionEffect(Potion.jump).getAmplifier() + 1.0F;
        return baseFallDist;
    }

    public static double calculateFriction(double moveSpeed, double lastDist, double baseMoveSpeedRef) {
        frictionValues.set(0, lastDist - (lastDist / BUNNY_DIV_FRICTION));
        frictionValues.set(1, lastDist - ((moveSpeed - lastDist) / 33.3D));
        double materialFriction =
            Wrapper.getPlayer().isInWater() ?
                WATER_FRICTION :
                Wrapper.getPlayer().isInLava() ?
                    LAVA_FRICTION :
                    AIR_FRICTION;
        frictionValues.set(2, lastDist - (baseMoveSpeedRef * (1.0D - materialFriction)));
        return Collections.min(frictionValues);
    }

    public static boolean isOnIce() {
        final Block blockUnder = getBlockUnder(1);
        return blockUnder instanceof BlockIce || blockUnder instanceof BlockPackedIce;
    }

    public static Block getBlockUnder(double offset) {
        EntityPlayerSP player = Wrapper.getPlayer();
        return Wrapper.getWorld().getBlockState(
            new BlockPos(
                player.posX,
                player.posY - offset,
                player.posZ)).getBlock();
    }

    public static double getBlockHeight() {
        return Wrapper.getPlayer().posY - (int) Wrapper.getPlayer().posY;
    }

    public static boolean canSprint(boolean omni) {
        final EntityPlayerSP player = Wrapper.getPlayer();
        return (omni ? isMovingEnoughForSprint() : player.movementInput.moveForward >= 0.8F) &&
            !player.isCollidedHorizontally &&
            (player.getFoodStats().getFoodLevel() > 6 ||
                player.capabilities.allowFlying) &&
            !player.isSneaking() &&
            (!player.isUsingItem() || NoSlowdown.isNoSlowdownEnabled()) &&
            !player.isPotionActive(Potion.moveSlowdown.id);
    }

    public static void fakeJump() {
        mc.thePlayer.isAirBorne = true;
        mc.thePlayer.triggerAchievement(StatList.jumpStat);
    }

    public static double getBaseMoveSpeed() {
        final EntityPlayerSP player = Wrapper.getPlayer();
        double base = player.isSneaking() ? WALK_SPEED * MovementUtils.SNEAK_MOD : canSprint(true) ? WALK_SPEED * SPRINTING_MOD : WALK_SPEED;

        PotionEffect moveSpeed = player.getActivePotionEffect(Potion.moveSpeed.id);
        PotionEffect moveSlowness = player.getActivePotionEffect(Potion.moveSlowdown.id);

        if (moveSpeed != null)
            base *= 1.0 + 0.2 * (moveSpeed.getAmplifier() + 1);

        if (moveSlowness != null)
            base *= 1.0 + 0.2 * (moveSlowness.getAmplifier() + 1);


        if (player.isInWater()) {
            base *= SWIM_MOD;
            final int depthStriderLevel = InventoryUtils.getDepthStriderLevel();
            if (depthStriderLevel > 0) {
                base *= DEPTH_STRIDER_VALUES[depthStriderLevel];
            }
        } else if (player.isInLava()) {
            base *= SWIM_MOD;
        }
        return base;
    }

    public static double getBaseMoveSpeed(double basespeed) {
        double baseSpeed = basespeed;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (double)(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    public static double getBPS() {
        return mc.thePlayer != null && mc.thePlayer.ticksExisted >= 1
                ? getDistance(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosZ) * (20.0F * mc.getTimer().timerSpeed)
                : 0.0;
    }

    public static double getDistance(double x, double z) {
        double xSpeed = mc.thePlayer.posX - x;
        double zSpeed = mc.thePlayer.posZ - z;
        return MathHelper.sqrt_double(xSpeed * xSpeed + zSpeed * zSpeed);
    }

    public static double getDirection() {
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (mc.thePlayer.moveForward < 0.0f) {
            forward = -0.5f;
        } else if (mc.thePlayer.moveForward > 0.0f) {
            forward = 0.5f;
        }
        if (mc.thePlayer.moveStrafing > 0.0f) {
            rotationYaw -= 90.0f * forward;
        }
        if (mc.thePlayer.moveStrafing < 0.0f) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static void setSpeed(MoveEntityEvent e, double speed) {
        final EntityPlayerSP player = Wrapper.getPlayer();
        final TargetStrafe targetStrafe = TargetStrafe.getInstance();
        if (targetStrafe.isEnabled() &&
            (!targetStrafe.holdSpaceProperty.getValue() || Keyboard.isKeyDown(Keyboard.KEY_SPACE))) {
            if (targetStrafe.shouldStrafe()) {
                if (targetStrafe.shouldAdaptSpeed())
                    speed = Math.min(speed, targetStrafe.getAdaptedSpeed());
                targetStrafe.setSpeed(e, speed);
                return;
            }
        }
        setSpeed(e, speed, player.moveForward, player.moveStrafing, player.rotationYaw);
    }

    public static void setSpeed(MoveEntityEvent e, double speed, float forward, float strafing, float yaw) {
        if (forward == 0.0F && strafing == 0.0F)
            return;

        boolean reversed = forward < 0.0f;
        float strafingYaw = 90.0f *
            (forward > 0.0f ? 0.5f : reversed ? -0.5f : 1.0f);

        if (reversed)
            yaw += 180.0f;
        if (strafing > 0.0f)
            yaw -= strafingYaw;
        else if (strafing < 0.0f)
            yaw += strafingYaw;

        double x = StrictMath.cos(StrictMath.toRadians(yaw + 90.0f));
        double z = StrictMath.cos(StrictMath.toRadians(yaw));

        e.setX(x * speed);
        e.setZ(z * speed);
    }

    public static boolean isOnGround() {
//        List<AxisAlignedBB> collidingList = Wrapper.getWorld().getCollidingBoundingBoxes(Wrapper.getPlayer(), Wrapper.getPlayer().getEntityBoundingBox().offset(0.0, -(0.01 - MIN_DIF), 0.0));
//        return collidingList.size() > 0;
        return Wrapper.getPlayer().onGround && Wrapper.getPlayer().isCollidedVertically;
    }

    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -height, 0.0)).isEmpty();
    }

    public static boolean isMoving() {
        return Wrapper.getPlayer().movementInput.moveForward != 0.0F || Wrapper.getPlayer().movementInput.moveStrafe != 0.0F;
    }

    public static boolean isMove() {
        return MovementUtils.mc.thePlayer != null && (MovementUtils.mc.thePlayer.movementInput.moveForward != 0.0f || MovementUtils.mc.thePlayer.movementInput.moveStrafe != 0.0f);
    }


    public static void setMotion(double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            mc.thePlayer.motionX = forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw));
            mc.thePlayer.motionZ = forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw));
        }
    }

    public static void setMotion(MoveEntityEvent event, double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        double yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (double)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (double)(forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            double cos = Math.cos(Math.toRadians(yaw + 90.0));
            double sin = Math.sin(Math.toRadians(yaw + 90.0));
            event.setX(forward * speed * cos + strafe * speed * sin);
            event.setZ(forward * speed * sin - strafe * speed * cos);
        }
    }

    public static void setMotion(MoveEntityEvent event, double speed, double motion) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        double yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += forward > 0.0 ? -45 : 45;
                } else if (strafe < 0.0) {
                    yaw += forward > 0.0 ? 45 : -45;
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            double cos = Math.cos(Math.toRadians(yaw + 90.0));
            double sin = Math.sin(Math.toRadians(yaw + 90.0));
            event.setX((forward * speed * cos + strafe * speed * sin) * motion);
            event.setZ((forward * speed * sin - strafe * speed * cos) * motion);
        }
    }

    public static void setMotio(MoveEntityEvent e, double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        if (mc.thePlayer.moveStrafing > 0.0f) {
            rotationYaw = (float)((double)rotationYaw - 90.0 * forward);
        }
        if (mc.thePlayer.moveStrafing < 0.0f) {
            rotationYaw = (float)((double)rotationYaw + 90.0 * forward);
        }
        double yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            mc.thePlayer.motionX = 0.0;
            mc.thePlayer.motionZ = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += forward > 0.0 ? -44 : 44;
                } else if (strafe < 0.0) {
                    yaw += forward > 0.0 ? 44 : -44;
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            e.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0)));
            e.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0)));
        }
    }

    public static void bypassOffSet(UpdatePositionEvent event) {
        if (isMoving()) {
            List<Double> BypassOffset = Arrays.asList(0.125, 0.25, 0.375, 0.625, 0.75, 0.015625, 0.5, 0.0625, 0.875, 0.1875);
            double d3 = event.getPosY() % 1.0;
            BypassOffset.sort(Comparator.comparingDouble(PreY -> Math.abs(PreY - d3)));
            double acc = event.getPosY() - d3 + BypassOffset.get(0);
            if (Math.abs(BypassOffset.get(0) - d3) < 0.005) {
                event.setPosY(acc);
                event.setOnGround(true);
            } else {
                List<Double> BypassOffset2 = Arrays.asList(0.715, 0.945, 0.09, 0.155, 0.14, 0.045, 0.63, 0.31);
                double d3_ = event.getPosY() % 1.0;
                BypassOffset2.sort(Comparator.comparingDouble(PreY -> Math.abs(PreY - d3_)));
                acc = event.getPosY() - d3_ + BypassOffset2.get(0);
                if (Math.abs(BypassOffset2.get(0) - d3_) < 0.005) {
                    event.setPosY(acc);
                }
            }
        }
    }
}
