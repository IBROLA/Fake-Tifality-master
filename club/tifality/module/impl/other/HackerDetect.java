package club.tifality.module.impl.other;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import com.mojang.realmsclient.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import club.tifality.gui.notification.client.NotificationPublisher;
import club.tifality.gui.notification.client.NotificationType;
import club.tifality.gui.notification.dev.DevNotifications;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.utils.timer.TimerUtil;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(label="HackerDetect", category= ModuleCategory.OTHER)
public class HackerDetect extends Module {
    private final Property<Boolean> friendValue = new Property<>("Friend", false);
    private final Property<Boolean> pingSpoofValue = new Property<>("Ping Spoof", true);
    private final Property<Boolean> criticalsValue = new Property<>("Criticals", true);
    private final Property<Boolean> invalidPitchValue = new Property<>("Invalid Pitch", true);
    private final Property<Boolean> noslowdownC = new Property<>("No Slowdown", true);
    private final Property<Boolean> highjumpC = new Property<>("High Jump", true);
    private final Property<Boolean> omnisprintC = new Property<>("Omni Sprint", true);
    private final Property<Boolean> longjumpC = new Property<>("Long Jump", true);
    private final Property<Boolean> speedC = new Property<>("Speed", true);
    private final Property<Boolean> stepC = new Property<>("Step", true);
    private final Property<Boolean> velocityC = new Property<>("Velocity", true);
    private final Property<Boolean> killauraC = new Property<>("Kill Aura", true);
    private final Property<Boolean> flyValue = new Property<>("Fly", true);
    private final List<Pair<EntityPlayer, String>> data = new ArrayList<>();
    public static final ArrayList<EntityPlayer> hackers = new ArrayList<>();
    private final ArrayList<String> hacker = new ArrayList<>();
    TimerUtil time = new TimerUtil();
    double motionvlY;
    double speedvl;
    double NoKBvl;
    double auravl;
    double noslowvl;

    @Override
    public void onEnable() {
        super.onEnable();
        hackers.clear();
        this.data.clear();
        this.hacker.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        hackers.clear();
    }

    public boolean isHacker(EntityLivingBase ent) {
        for (EntityPlayer hacker : hackers) {
            if (!ent.getName().equals(hacker.getName())) continue;
            return true;
        }
        return false;
    }

    private boolean checkGround(double y) {
        return y % 0.015625 == 0.0;
    }

    public static double getBPS(Entity entityIn) {
        double xDist = entityIn.posX - entityIn.prevPosX;
        double zDist = entityIn.posZ - entityIn.prevPosZ;
        double bps = Math.sqrt(xDist * xDist + zDist * zDist) * 20.0;
        return (double)((int)bps) + bps - (double)((int)bps);
    }

    public static double SpeedBs(Entity entity) {
        double xDif = entity.posX - entity.prevPosX;
        double zDif = entity.posZ - entity.prevPosZ;
        double lastDist = Math.sqrt(xDif * xDif + zDif * zDif) * 20.0;
        return Math.round(lastDist);
    }

    public static float[] getFacePosEntityRemote(EntityLivingBase facing, Entity en) {
        if (en == null) {
            return new float[]{facing.rotationYawHead, facing.rotationPitch};
        }
        return getFacePosRemote(new Vec3(facing.posX, facing.posY + (double)en.getEyeHeight(), facing.posZ), new Vec3(en.posX, en.posY + (double)en.getEyeHeight(), en.posZ));
    }

    @Listener
    public void onUpdate(UpdatePositionEvent event) {
        EntityPlayer player;
        if (mc.thePlayer.ticksExisted <= 105) {
            hackers.clear();
            return;
        }
        if (mc.theWorld == null) {
            return;
        }
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (!(entity instanceof EntityPlayer)) continue;
            if (entity instanceof EntityOtherPlayerMP) {
                player = (EntityOtherPlayerMP)entity;
                if (this.getSpeed(player) > this.getBaseMoveSpeed() + 0.85 && !player.onGround && !this.isInLiquid(player)) {
                    this.informPlayer(player, "Motion/Moving too fast");
                }
                if (this.time.hasElapsed(1003L)) {
                    this.time.reset();
                }
                if (!player.isBlocking()) {
                    this.time.reset();
                }
                if (player.isBlocking() && (double) player.moveForward >= 0.9 && this.time.hasElapsed(200L)) {
                    this.informPlayer(player, "No Slow Down");
                }
                if (this.flyValue.get() && player.motionY == 0.0 && this.getSpeed(player) > 0.2775 && mc.thePlayer.ticksExisted % 2 == 0 && ((EntityOtherPlayerMP)player).posY - ((EntityOtherPlayerMP)player).lastTickPosY > 0.02 && !this.isInLiquid(player)) {
                    this.informPlayer(player, "Flight");
                }
            }
            if ((player = (EntityPlayer)entity) instanceof EntityPlayerSP || !player.isEntityAlive() || mc.playerController.isSpectator() || player.isSpectator() || Tifality.getInstance().getFriendManager().isFriend((EntityPlayer)entity) && this.friendValue.get()) continue;
            if (this.criticalsValue.get() && player.fallDistance > 0.0f && !this.checkGround(player.posY) && player.ridingEntity == null && (player.posY % 1.0 == 0.0 || player.posY % 0.5 == 0.0) && (double)player.fallDistance < 0.06251 && !player.isPotionActive(Potion.blindness)) {
                this.informPlayer(player, "Criticals(Invalid Pos)");
                hackers.add(player);
            }
            if (this.pingSpoofValue.get()) {
                try {
                    NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(player.getName());
                    if (info != null && info.getResponseTime() > 600) {
                        this.informPlayer(player, String.format("PingSpoof(%dms)", info.getResponseTime()));
                        hackers.add(player);
                    }
                } catch (Throwable info) {
                    // empty catch block
                }
            }
            if (!this.invalidPitchValue.get() || !(player.rotationPitch > 90.0f) && !(player.rotationPitch < -90.0f)) continue;
            this.informPlayer(player, "Invalid Pitch");
            hackers.add(player);
        }
        for (Entity entity : mc.theWorld.playerEntities) {
            double lastY;
            double y;
            double yDiff;
            player = (EntityPlayer)entity;
            if (player instanceof EntityPlayerSP || player == mc.thePlayer || player.ticksExisted < 105 || hackers.contains(player) || player.capabilities.isFlying || player.capabilities.isCreativeMode) continue;
            double playerSpeed = getBPS(player);
            if (this.killauraC.get().booleanValue() && player.swingProgress < 2.0f && player.swingProgress != 0.0f) {
                float[] rots = getFacePosEntityRemote(mc.thePlayer, player);
                boolean highYawRate = false;
                if (Math.abs(player.rotationYaw - player.prevRotationYaw) > 40.0f) {
                    highYawRate = true;
                }
                if (Math.abs(player.rotationYaw - rots[0]) < 2.0f) {
                    if (highYawRate) {
                        this.auravl += 1.0;
                        if (this.auravl >= 30.0) {
                            DevNotifications.getManager().post("§f" + player.getName() + "§f is using Kill Aura");
                            NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Kill Aura", NotificationType.WARNING, 5000);
                            this.auravl = 0.0;
                            hackers.add(player);
                        }
                    }
                    this.auravl += 1.0;
                    if (this.auravl >= 30.0) {
                        DevNotifications.getManager().post("§f" + player.getName() + "§f is using Kill Aura");
                        NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Kill Aura", NotificationType.WARNING, 5000);
                        this.auravl = 0.0;
                        hackers.add(player);
                    }
                }
            }
            if (this.noslowdownC.get().booleanValue() && player.isBlocking() && SpeedBs(player) >= 6.0) {
                this.noslowvl += 1.0;
                if (this.noslowvl >= 30.0) {
                    DevNotifications.getManager().post("§f" + player.getName() + "§f is using No Slow");
                    NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using No Slow", NotificationType.WARNING, 5000);
                    this.noslowvl = 0.0;
                    hackers.add(player);
                }
            }
            if (this.highjumpC.get() && player.motionY > 1.0) {
                this.motionvlY += 1.0;
                if (this.motionvlY >= 25.0) {
                    DevNotifications.getManager().post("§f" + player.getName() + "§f is using High Jump");
                    NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using High Jump", NotificationType.WARNING, 5000);
                    this.motionvlY = 0.0;
                    hackers.add(player);
                }
            }
            if (this.omnisprintC.get() && player.isSprinting() && (player.moveForward < 0.0f || player.moveForward == 0.0f && player.moveStrafing != 0.0f)) {
                DevNotifications.getManager().post("§f" + player.getName() + "§f is using Omni Sprint");
                NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Omni Sprint", NotificationType.WARNING, 5000);
                hackers.add(player);
            }
            if (this.longjumpC.get() && !mc.theWorld.getCollidingBoundingBoxes(player, mc.thePlayer.getEntityBoundingBox().offset(0.0, player.motionY, 0.0)).isEmpty() && player.motionY > 0.0 && playerSpeed > 10.0) {
                DevNotifications.getManager().post("§f" + player.getName() + "§f is using Long Jump");
                NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Long Jump", NotificationType.WARNING, 5000);
                hackers.add(player);
            }
            double xDif = player.posX - player.prevPosX;
            double zDif = player.posZ - player.prevPosZ;
            double lastDist = Math.sqrt(xDif * xDif + zDif * zDif) * 20.0;
            if (this.speedC.get().booleanValue() && Math.round(lastDist) > 15L) {
                this.speedvl += 1.0;
                if (this.speedvl >= 150.0) {
                    DevNotifications.getManager().post("§f" + player.getName() + "§f is using Speed");
                    NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Speed", NotificationType.WARNING, 5000);
                    this.speedvl = 0.0;
                    hackers.add(player);
                }
            }
            double d = yDiff = (y = (double)Math.abs((int)player.posY)) > (lastY = (double)Math.abs((int)player.lastTickPosY)) ? y - lastY : lastY - y;
            if (this.stepC.get().booleanValue() && yDiff > 0.0 && mc.thePlayer.onGround && player.motionY == -0.0784000015258789) {
                DevNotifications.getManager().post("§f" + player.getName() + "§f is using Step");
                NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Step", NotificationType.WARNING, 5000);
                this.speedvl = 0.0;
                hackers.add(player);
            }
            if (!this.velocityC.get().booleanValue()) continue;
            if (player.hurtResistantTime > 6 && player.hurtResistantTime < 12 && player.lastTickPosX == player.posX && player.posZ == player.lastTickPosZ && !mc.theWorld.checkBlockCollision(player.getEntityBoundingBox().expand(0.05, 0.0, 0.05))) {
                this.NoKBvl += 1.0;
                if (this.NoKBvl >= 50.0) {
                    DevNotifications.getManager().post("§f" + player.getName() + "§f is using Velocity");
                    NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Velocity", NotificationType.WARNING, 5000);
                    this.NoKBvl = 0.0;
                    hackers.add(player);
                }
            }
            if (player.hurtResistantTime <= 6 || player.hurtResistantTime >= 12 || player.lastTickPosY != player.posY) continue;
            this.NoKBvl += 1.0;
            if (!(this.NoKBvl >= 40.0)) continue;
            DevNotifications.getManager().post("§f" + player.getName() + "§f is using Velocity");
            NotificationPublisher.queue("Hacker Detected!", "§f" + player.getName() + "§f is using Velocity", NotificationType.WARNING, 5000);
            this.NoKBvl = 0.0;
            hackers.add(player);
        }
    }

    private void informPlayer(EntityPlayer player, String hakk) {
        for (Pair<EntityPlayer, String> pair : this.data) {
            if (pair.first() != player || !pair.second().equalsIgnoreCase(hakk)) continue;
            return;
        }
        DevNotifications.getManager().post(String.format("§f%s is using %s", player.getName(), hakk, player.getHealth(), player.getMaxHealth()));
        NotificationPublisher.queue("Hacker Detected!", String.format("§f%s is using %s", player.getName(), hakk, player.getHealth(), player.getMaxHealth()), NotificationType.WARNING, 5000);
        this.data.add(Pair.of(player, hakk));
    }

    private double getSpeed(EntityPlayer player) {
        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.2875;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (double)(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    private boolean isInLiquid(Entity e) {
        for (int x = MathHelper.floor_double(e.getEntityBoundingBox().minY); x < MathHelper.floor_double(e.getEntityBoundingBox().maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(e.getEntityBoundingBox().minZ); z < MathHelper.floor_double(e.getEntityBoundingBox().maxZ) + 1; ++z) {
                BlockPos pos = new BlockPos(x, (int)e.getEntityBoundingBox().minY, z);
                Block block = Minecraft.getMinecraft().theWorld.getBlockState(pos).getBlock();
                if (block == null || block instanceof BlockAir) continue;
                return block instanceof BlockLiquid;
            }
        }
        return false;
    }

    private static float[] getFacePosRemote(Vec3 src, Vec3 dest) {
        double diffX = dest.xCoord - src.xCoord;
        double diffY = dest.yCoord - src.yCoord;
        double diffZ = dest.zCoord - src.zCoord;
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-Math.atan2(diffY, dist) * 180.0 / Math.PI);
        return new float[]{MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch)};
    }
}

