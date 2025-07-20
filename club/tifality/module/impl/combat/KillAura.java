package club.tifality.module.impl.combat;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;
import club.tifality.manager.event.impl.entity.EntitySwingEvent;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.utils.InventoryUtils;
import club.tifality.utils.PlayerUtils;
import club.tifality.utils.RotationUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.RenderingUtils;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;
import club.tifality.gui.csgo.SkeetUI;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.manager.event.impl.render.Render3DEvent;
import club.tifality.module.Module;
import club.tifality.module.impl.player.Scaffold;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.MultiSelectEnumProperty;
import club.tifality.property.impl.Representation;

import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModuleInfo(label="KillAura", category= ModuleCategory.COMBAT)
public final class KillAura extends Module {
    private static final C07PacketPlayerDigging PLAYER_DIGGING = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
    private static final C08PacketPlayerBlockPlacement BLOCK_PLACEMENT = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);
    public static int waitTicks;
    private final EnumProperty<AuraMode> auraModeProperty = new EnumProperty<>("Mode", AuraMode.SWITCH);
    private final EnumProperty<SortingMethod> sortingMethodProperty = new EnumProperty<>("Priority", SortingMethod.HEALTH);
    private final EnumProperty<AttackMethod> attackMethodProperty = new EnumProperty<>("Attack Mode", AttackMethod.POST);
    private final DoubleProperty minApsProperty = new DoubleProperty("Min APS", 8.0, () -> this.auraModeProperty.getValue() != AuraMode.TICK, 1.0, 20.0, 1.0);
    private final DoubleProperty maxApsProperty = new DoubleProperty("Max APS", 10.0, () -> this.auraModeProperty.getValue() != AuraMode.TICK, 1.0, 20.0, 1.0);
    private final DoubleProperty rangeProperty = new DoubleProperty("Range", 4.3, 3.0, 8.0, 0.1, Representation.DISTANCE);
    private final Property<Boolean> indicatorValue = new Property<>("Indicator", false);
    private final EnumProperty<RenderMode> indicatorModeValue = new EnumProperty<>("Render Mode", RenderMode.Normal, this.indicatorValue::get);
    public final Property<Integer> indicatorColorValue = new Property<>("Color", new Color(200, 255, 100, 75).getRGB(), this.indicatorValue::get);
    private final Property<Boolean> autoblockProperty = new Property<>("Auto block", true);
    private final EnumProperty<BlockMode> blockModeModeProperty = new EnumProperty<>("Block Mode", BlockMode.NCP, this.autoblockProperty::get);
    private final Property<Boolean> ncpRotationsProperty = new Property<>("Reduce", true);
    private final DoubleProperty blockRangeProperty = new DoubleProperty("Block Range", 8.0, this.autoblockProperty::getValue, 3.0, 16.0, 0.1, Representation.DISTANCE);
    private final DoubleProperty maxAngleChangeProperty = new DoubleProperty("Angle Step", 45.0, () -> !this.ncpRotationsProperty.getValue(), 1.0, 180.0, 1.0);
    private final Property<Boolean> lockViewProperty = new Property<>("Silent", true);
    private final Property<Boolean> keepSprintProperty = new Property<>("Keep Sprint", true);
    private final Property<Boolean> rayTraceProperty = new Property<>("Ray Trace", false);
    private final Property<Boolean> forceUpdateProperty = new Property<>("Force Update", false);
    private final Property<Boolean> invCheck = new Property<>("Inv Check", false);
    private final MultiSelectEnumProperty<Targets> targetsProperty = new MultiSelectEnumProperty<>("Target", Targets.PLAYERS);
    private final TimerUtil attackTimer = new TimerUtil();
    private final Map<Integer, Long> playerSwingDelayMap = new HashMap<>();
    private EntityLivingBase target;
    private boolean blocking;
    private boolean entityInBlockRange;
    private Scaffold scaffold;

    @Listener
    public void onPacketSendEvent(PacketSendEvent event) {
        if (event.getPacket() instanceof C0APacketAnimation) {
            this.attackTimer.reset();
        }
    }

    @Listener
    public void onEntitySwing(EntitySwingEvent event) {
        this.playerSwingDelayMap.put(event.getEntityId(), System.currentTimeMillis());
    }

    @Listener
    private void onRender3D(Render3DEvent event) {
        //Color color = new Color(200, 255, 100, 75);
        if (this.indicatorValue.get() && this.indicatorModeValue.get() == RenderMode.Box && this.getTarget() != null) {
            RenderingUtils.drawPlatform(this.getTarget(), this.getTarget().hurtTime > 3 ? new Color(this.indicatorColorValue.get(), true) : new Color(235, 40, 40, 75));
        }
        if (this.indicatorValue.get() && this.indicatorModeValue.get() == RenderMode.Normal && this.getTarget() != null) {
            RenderingUtils.drawAuraMark(this.getTarget(), this.getTarget().hurtTime > 3 ? new Color(this.indicatorColorValue.get(), true) : new Color(235, 40, 40, 75));
        }
    }

    @Listener(Priority.LOW)
    public void onUpdatePositionEvent(UpdatePositionEvent event) {
        if (event.isPre()) {
            this.entityInBlockRange = false;
            EntityLivingBase optimalTarget = null;
            List<EntityLivingBase> entities = Wrapper.getLivingEntities(this::isValid);
            if (entities.size() > 1) {
                entities.sort((this.sortingMethodProperty.getValue()).getSorter());
            }
            for (EntityLivingBase entity : entities) {
                float dist = Wrapper.getPlayer().getDistanceToEntity(entity);
                if (!this.entityInBlockRange && (double)dist < this.blockRangeProperty.getValue()) {
                    this.entityInBlockRange = true;
                }
                if (!(dist < this.rangeProperty.getValue())) continue;
                optimalTarget = entity;
                break;
            }
            this.target = optimalTarget;
            if (this.blocking) {
                this.blocking = false;
                if (this.isHoldingSword()) {
                    switch (this.blockModeModeProperty.get()) {
                        case WATCHDOG: {
                            Wrapper.sendPacketDirect(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            break;
                        }
                        case NCP: {
                            Wrapper.sendPacketDirect(PLAYER_DIGGING);
                            break;
                        }
                    }
                }
            }
            if (this.isOccupied()) {
                return;
            }
            if (optimalTarget != null) {
                if (Wrapper.getTimer().timerSpeed > 1.0f) {
                    Wrapper.getTimer().timerSpeed = 1.0f;
                }
                float[] rotations = this.ncpRotationsProperty.getValue() ? getRotations(this.target) : getRotations(optimalTarget, event.getPrevYaw(), event.getPrevPitch(), this.maxAngleChangeProperty.getValue().floatValue());
                float yaw = rotations[0];
                float pitch = rotations[1];
                event.setYaw(yaw);
                event.setPitch(pitch);
                if (!this.lockViewProperty.getValue()) {
                    Wrapper.getPlayer().rotationYaw = yaw;
                    Wrapper.getPlayer().rotationPitch = pitch;
                }
                if (this.forceUpdateProperty.getValue()) {
                    Wrapper.sendPacketDirect(new C03PacketPlayer.C06PacketPlayerPosLook(event.getPosX(), event.getPosY(), event.getPosZ(), event.getYaw(), event.getPitch(), event.isOnGround()));
                }
                if (this.attackMethodProperty.getValue() == AttackMethod.PRE && this.checkWaitTicks()) {
                    this.tryAttack(event);
                }
            }
        } else {
            if (this.isOccupied()) {
                return;
            }
            if (this.target != null && this.attackMethodProperty.getValue() == AttackMethod.POST && this.checkWaitTicks()) {
                this.tryAttack(event);
            }
            if (this.entityInBlockRange && this.autoblockProperty.getValue() && this.isHoldingSword()) {
                Wrapper.getPlayer().setItemInUse(Wrapper.getPlayer().getCurrentEquippedItem(), Wrapper.getPlayer().getCurrentEquippedItem().getMaxItemUseDuration());
                if (!this.blocking) {
                    switch (this.blockModeModeProperty.get()) {
                        case WATCHDOG: {
                            Wrapper.sendPacketDirect(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                            break;
                        }
                        case NCP: {
                            Wrapper.sendPacketDirect(BLOCK_PLACEMENT);
                            break;
                        }
                    }
                    this.blocking = true;
                }
            }
        }
    }

    public KillAura() {
        this.setSuffixListener(this.auraModeProperty);
    }

    public static boolean isBlocking() {
        return getInstance().isEnabled() && getInstance().autoblockProperty.getValue() && getInstance().entityInBlockRange;
    }

    public static KillAura getInstance() {
        return ModuleManager.getInstance(KillAura.class);
    }

    public static double getEffectiveHealth(EntityLivingBase entity) {
        return (double)entity.getHealth() * 20.0 / (double)entity.getTotalArmorValue();
    }

    private static float[] getRotations(Entity entity) {
        float pitch;
        EntityPlayerSP player = Wrapper.getPlayer();
        double xDist = entity.posX - player.posX;
        double zDist = entity.posZ - player.posZ;
        double yDist = entity.posY - player.posY;
        double dist = StrictMath.sqrt(xDist * xDist + zDist * zDist);
        AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.1f, 0.1f, 0.1f);
        double playerEyePos = player.posY + (double)player.getEyeHeight();
        boolean close = dist < 3.0 && Math.abs(yDist) < 3.0;
        boolean closet = dist < 1.0 && Math.abs(yDist) < 1.0;
        if (close && playerEyePos > entityBB.minY) {
            pitch = closet && playerEyePos > entityBB.minY ? 90.0f : 60.0f;
        } else {
            yDist = playerEyePos > entityBB.maxY ? entityBB.maxY - playerEyePos : (playerEyePos < entityBB.minY ? entityBB.minY - playerEyePos : 0.0);
            pitch = (float)(-(StrictMath.atan2(yDist, dist) * 57.29577951308232));
        }
        float yaw = (float)(StrictMath.atan2(zDist, xDist) * 57.29577951308232) - 90.0f;
        if (close && closet) {
            int inc = dist < 1.0 ? 180 : 90;
            yaw = Math.round(yaw / (float)inc) * inc;
        }
        return new float[]{yaw, pitch};
    }

    @Override
    public void onEnable() {
        if (this.scaffold == null) {
            this.scaffold = ModuleManager.getInstance(Scaffold.class);
        }
    }

    private boolean isInMenu() {
        GuiScreen currentScreen = Wrapper.getCurrentScreen();
        return currentScreen != null && !(currentScreen instanceof SkeetUI);
    }

    private boolean isOccupied() {
        return this.isInMenu() && this.invCheck.get() || this.scaffold.isEnabled();
    }

    public EntityLivingBase getTarget() {
        return this.target;
    }

    @Override
    public void onDisable() {
        if (this.blocking) {
            this.blocking = false;
            Wrapper.sendPacketDirect(PLAYER_DIGGING);
        }
        this.target = null;
        this.entityInBlockRange = false;
    }

    private boolean checkWaitTicks() {
        if (waitTicks > 0) {
            --waitTicks;
            return false;
        }
        return true;
    }

    private void tryAttack(UpdatePositionEvent event) {
        if (this.isUsingItem()) {
            return;
        }
        switch (this.auraModeProperty.getValue()) {
            case TICK: {
                if (!this.attackTimer.hasElapsed(480L) || !this.ncpRotationsProperty.getValue() && !isLookingAtEntity(event.getYaw(), event.getPitch(), this.target, this.rangeProperty.getValue(), this.rayTraceProperty.getValue())) break;
                Wrapper.getPlayer().swingItem();
                Wrapper.sendPacket(new C02PacketUseEntity(this.target, C02PacketUseEntity.Action.ATTACK));
                InventoryUtils.windowClick(36, 8, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                Wrapper.sendPacket(new C02PacketUseEntity(this.target, C02PacketUseEntity.Action.ATTACK));
                InventoryUtils.windowClick(44, 0, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                if (this.keepSprintProperty.getValue() || !Wrapper.getPlayer().isSprinting()) break;
                Wrapper.getPlayer().motionX *= 0.6;
                Wrapper.getPlayer().motionZ *= 0.6;
                Wrapper.getPlayer().setSprinting(false);
                break;
            }
            case SWITCH: {
                int min = this.minApsProperty.getValue().intValue();
                int max = this.maxApsProperty.getValue().intValue();
                int cps = min == max ? min : RandomUtils.nextInt(min, max);
                if (!this.attackTimer.hasElapsed(1000L / (long)cps) || !this.ncpRotationsProperty.getValue() && !isLookingAtEntity(event.getYaw(), event.getPitch(), this.target, this.rangeProperty.getValue(), this.rayTraceProperty.getValue())) break;
                Wrapper.getPlayer().swingItem();
                Wrapper.sendPacket(new C02PacketUseEntity(this.target, C02PacketUseEntity.Action.ATTACK));
                if (this.keepSprintProperty.getValue() || !Wrapper.getPlayer().isSprinting()) break;
                Wrapper.getPlayer().motionX *= 0.6;
                Wrapper.getPlayer().motionZ *= 0.6;
                Wrapper.getPlayer().setSprinting(false);
                break;
            }
        }
    }

    private boolean isUsingItem() {
        return Wrapper.getPlayer().isUsingItem() && !this.isHoldingSword();
    }

    private boolean isHoldingSword() {
        return Wrapper.getPlayer().getCurrentEquippedItem() != null && Wrapper.getPlayer().getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    private boolean isValid(EntityLivingBase entity) {
        if (!entity.isEntityAlive()) {
            return false;
        }
        if (entity.isInvisible() && !this.targetsProperty.isSelected(Targets.INVISIBLE)) {
            return false;
        }
        if (entity instanceof EntityOtherPlayerMP) {
            EntityPlayer player = (EntityPlayer)entity;
            if (!this.targetsProperty.isSelected(Targets.PLAYERS)) {
                return false;
            }
            AntiBot antiBotInstance = ModuleManager.getInstance(AntiBot.class);
            if (antiBotInstance.isEnabled() && !PlayerUtils.checkPing(player)) {
                return false;
            }
            if (!this.targetsProperty.isSelected(Targets.TEAMMATES) && PlayerUtils.isTeamMate(player)) {
                return false;
            }
            if (!this.targetsProperty.isSelected(Targets.FRIENDS) && Tifality.getInstance().getFriendManager().isFriend(player)) {
                return false;
            }
        } else if (entity instanceof EntityMob) {
            if (!this.targetsProperty.isSelected(Targets.MOBS)) {
                return false;
            }
        } else if (entity instanceof EntityAnimal) {
            if (!this.targetsProperty.isSelected(Targets.ANIMAL)) {
                return false;
            }
        } else if (entity instanceof EntityVillager) {
            if (!this.targetsProperty.isSelected(Targets.VILLAGER)) {
                return false;
            }
        } else {
            return false;
        }
        return (double)Wrapper.getPlayer().getDistanceToEntity(entity) < Math.max(this.blockRangeProperty.getValue(), this.rangeProperty.getValue());
    }

    private static boolean isLookingAtEntity(float yaw, float pitch, Entity entity, double range, boolean rayTrace) {
        EntityPlayerSP entityPlayerSP = Wrapper.getPlayer();
        Vec3 src = Wrapper.getPlayer().getPositionEyes(1.0f);
        Vec3 rotationVec = Entity.getVectorForRotation(pitch, yaw);
        Vec3 dest = src.addVector(rotationVec.xCoord * range, rotationVec.yCoord * range, rotationVec.zCoord * range);
        MovingObjectPosition obj = Wrapper.getWorld().rayTraceBlocks(src, dest, false, false, true);
        if (obj == null) {
            return false;
        }
        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (rayTrace) {
                return false;
            }
            if ((double)entityPlayerSP.getDistanceToEntity(entity) > 3.0) {
                return false;
            }
        }
        return entity.getEntityBoundingBox().expand(0.1f, 0.1f, 0.1f).calculateIntercept(src, dest) != null;
    }

    private static float[] getRotations(Entity entity, float prevYaw, float prevPitch, float aimSpeed) {
        EntityPlayerSP player = Wrapper.getPlayer();
        double xDist = entity.posX - player.posX;
        double zDist = entity.posZ - player.posZ;
        AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.1f, 0.1f, 0.1f);
        double playerEyePos = player.posY + (double)player.getEyeHeight();
        double yDist = playerEyePos > entityBB.maxY ? entityBB.maxY - playerEyePos : (playerEyePos < entityBB.minY ? entityBB.minY - playerEyePos : 0.0);
        double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
        float yaw = interpolateRotation(prevYaw, (float)(StrictMath.atan2(zDist, xDist) * 57.29577951308232) - 90.0f, aimSpeed);
        float pitch = interpolateRotation(prevPitch, (float)(-(StrictMath.atan2(yDist, fDist) * 57.29577951308232)), aimSpeed);
        return new float[]{yaw, pitch};
    }

    private static float interpolateRotation(float prev, float now, float maxTurn) {
        float angle = MathHelper.wrapAngleTo180_float(now - prev);
        if (angle > maxTurn) {
            angle = maxTurn;
        }
        if (angle < -maxTurn) {
            angle = -maxTurn;
        }
        return prev + angle;
    }

    private static class HurtTimeSorting implements Comparator<EntityLivingBase> {
        private HurtTimeSorting() {
        }

        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Integer.compare(20 - o2.hurtResistantTime, 20 - o1.hurtResistantTime);
        }
    }

    private static class HealthSorting implements Comparator<EntityLivingBase> {
        private HealthSorting() {
        }

        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(getEffectiveHealth(o1), getEffectiveHealth(o2));
        }
    }

    private static class DistanceSorting implements Comparator<EntityLivingBase> {
        private DistanceSorting() {
        }

        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(o1.getDistanceToEntity(Wrapper.getPlayer()), o2.getDistanceToEntity(Wrapper.getPlayer()));
        }
    }

    private static class CombinedSorting implements Comparator<EntityLivingBase> {
        private CombinedSorting() {
        }

        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            int t1 = 0;
            SortingMethod[] arrayOfSortingMethod = SortingMethod.values();
            int i = arrayOfSortingMethod.length;
            for (int b = 0; b < i; b = (int)((byte)(b + 1))) {
                SortingMethod sortingMethod = arrayOfSortingMethod[b];
                Comparator<EntityLivingBase> sorter = sortingMethod.getSorter();
                if (sorter == this) continue;
                t1 += sorter.compare(o1, o2);
            }
            return t1;
        }
    }

    private static class AngleSorting implements Comparator<EntityLivingBase> {
        private AngleSorting() {
        }

        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            float yaw = Wrapper.getPlayer().currentEvent.getYaw();
            return Double.compare(Math.abs(RotationUtils.getYawToEntity(o1) - yaw), Math.abs(RotationUtils.getYawToEntity(o2) - yaw));
        }
    }

    private enum SortingMethod {
        DISTANCE(new DistanceSorting()),
        HEALTH(new HealthSorting()),
        HURT_TIME(new HurtTimeSorting()),
        ANGLE(new AngleSorting()),
        COMBINED(new CombinedSorting());

        private final Comparator<EntityLivingBase> sorter;

        SortingMethod(Comparator<EntityLivingBase> sorter) {
            this.sorter = sorter;
        }

        public Comparator<EntityLivingBase> getSorter() {
            return this.sorter;
        }
    }

    private enum Targets {
        PLAYERS,
        TEAMMATES,
        FRIENDS,
        VILLAGER,
        MOBS,
        ANIMAL,
        INVISIBLE;
    }

    private enum RenderMode {
        Box,
        Normal;
    }

    private enum AttackMethod {
        PRE,
        POST;
    }

    private enum AuraMode {
        SWITCH,
        TICK;
    }

    private enum BlockMode {
        WATCHDOG,
        NCP;
    }
}

