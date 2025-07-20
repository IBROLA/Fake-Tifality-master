package club.tifality.utils.inventory;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import club.tifality.manager.event.impl.player.WindowClickEvent;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import club.tifality.utils.timer.TimerUtil;
import club.tifality.utils.Wrapper;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class InventoryUtils {
    public static final TimerUtil CLICK_TIMER = new TimerUtil();
    public static final int INCLUDE_ARMOR_BEGIN = 5;
    public static final int EXCLUDE_ARMOR_BEGIN = 9;
    public static final int ONLY_HOT_BAR_BEGIN = 36;
    public static final List<Block> BLOCK_BLACKLIST = Arrays.asList(Blocks.enchanting_table,
            Blocks.chest,
            Blocks.ender_chest,
            Blocks.trapped_chest,
            Blocks.anvil,
            Blocks.sand,
            Blocks.web,
            Blocks.torch,
            Blocks.crafting_table,
            Blocks.furnace,
            Blocks.waterlily,
            Blocks.dispenser,
            Blocks.stone_pressure_plate,
            Blocks.wooden_pressure_plate,
            Blocks.noteblock,
            Blocks.dropper,
            Blocks.tnt,
            Blocks.standing_banner,
            Blocks.wall_banner,
            Blocks.redstone_torch);
    public static final int END = 45;
    private static final List<Integer> BAD_EFFECTS_IDS = Arrays.asList(Potion.poison.id,
            Potion.weakness.id,
            Potion.wither.id,
            Potion.blindness.id,
            Potion.digSlowdown.id,
            Potion.harm.id);

    public static int findAutoBlockBlock() {
        ItemBlock itemBlock;
        Block block;
        ItemStack itemStack;
        int i;
        for (i = 36; i < 45; ++i) {
            itemStack = Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack == null || !(itemStack.getItem() instanceof ItemBlock) || itemStack.stackSize <= 0 || !(block = (itemBlock = (ItemBlock)itemStack.getItem()).getBlock()).isFullCube() || BLOCK_BLACKLIST.contains(block)) continue;
            return i;
        }
        for (i = 36; i < 45; ++i) {
            itemStack = Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack == null || !(itemStack.getItem() instanceof ItemBlock) || itemStack.stackSize <= 0 || BLOCK_BLACKLIST.contains(block = (itemBlock = (ItemBlock)itemStack.getItem()).getBlock())) continue;
            return i;
        }
        return -1;
    }

    private InventoryUtils() {
    }

    public static void openInventory() {
        Wrapper.sendPacketDirect(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
    }

    public static void closeInventory() {
        Wrapper.sendPacketDirect(new C0DPacketCloseWindow(Wrapper.getPlayer().inventoryContainer.windowId));
    }

    public static int getDepthStriderLevel() {
        return EnchantmentHelper.getDepthStriderModifier(Wrapper.getPlayer());
    }

    public static void windowClick(int windowId, int slotId, int mouseButtonClicked, ClickType mode) {
        Wrapper.getPlayerController().windowClick(windowId, slotId, mouseButtonClicked, mode.ordinal(), Wrapper.getPlayer());
    }

    public static void windowClick(int slotId, int mouseButtonClicked, ClickType mode) {
        Wrapper.getPlayerController().windowClick(Wrapper.getPlayer().inventoryContainer.windowId, slotId, mouseButtonClicked, mode.ordinal(), Wrapper.getPlayer());
    }

    public static double getDamageReduction(ItemStack stack) {
        double reduction = 0.0;
        ItemArmor armor = (ItemArmor)stack.getItem();
        reduction += armor.damageReduceAmount;
        if (stack.isItemEnchanted()) {
            reduction += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25;
        }
        return reduction;
    }

    public static boolean isValid(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() instanceof ItemBlock) {
            return InventoryUtils.isGoodBlockStack(stack);
        }
        if (stack.getItem() instanceof ItemSword) {
            return InventoryUtils.isBestSword(stack);
        }
        if (stack.getItem() instanceof ItemTool) {
            return InventoryUtils.isBestTool(stack);
        }
        if (stack.getItem() instanceof ItemArmor) {
            return InventoryUtils.isBestArmor(stack);
        }
        if (stack.getItem() instanceof ItemPotion) {
            return InventoryUtils.isBuffPotion(stack);
        }
        if (stack.getItem() instanceof ItemFood) {
            return InventoryUtils.isGoodFood(stack);
        }
        if (stack.getItem() instanceof ItemBow) {
            return InventoryUtils.isBestBow(stack);
        }
        return InventoryUtils.isGoodItem(stack);
    }

    public static boolean isBestBow(ItemStack itemStack) {
        AtomicDouble bestBowDmg = new AtomicDouble(-1.0);
        AtomicReference<Object> bestBow = new AtomicReference<Object>(null);
        Wrapper.forEachInventorySlot(9, 45, (slot, stack) -> {
            double damage;
            if (stack.getItem() instanceof ItemBow && (damage = InventoryUtils.getBowDamage(stack)) > bestBowDmg.get()) {
                bestBow.set(stack);
                bestBowDmg.set(damage);
            }
        });
        return itemStack == bestBow.get() || InventoryUtils.getBowDamage(itemStack) > bestBowDmg.get();
    }

    public static double getBowDamage(ItemStack stack) {
        double damage = 0.0;
        if (stack.getItem() instanceof ItemBow && stack.isItemEnchanted()) {
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
        }
        return damage;
    }

    public static boolean isGoodItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBucket && ((ItemBucket)item).isFull != Blocks.flowing_water) {
            return false;
        }
        return !(item instanceof ItemExpBottle) && !(item instanceof ItemFishingRod) && !(item instanceof ItemEgg) && !(item instanceof ItemSnowball) && !(item instanceof ItemSkull) && !(item instanceof ItemBucket);
    }

    public static boolean isBuffPotion(ItemStack stack) {
        ItemPotion potion = (ItemPotion)stack.getItem();
        List<PotionEffect> effects = potion.getEffects(stack);
        if (effects.size() < 1) {
            return false;
        }
        for (PotionEffect effect : effects) {
            if (!BAD_EFFECTS_IDS.contains(effect.getPotionID())) continue;
            return false;
        }
        return true;
    }

    public static boolean isGoodFood(ItemStack stack) {
        ItemFood food = (ItemFood)stack.getItem();
        if (food instanceof ItemAppleGold) {
            return true;
        }
        return food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3f;
    }

    public static boolean isBestSword(ItemStack itemStack) {
        AtomicDouble damage = new AtomicDouble(0.0);
        AtomicReference<Object> bestStack = new AtomicReference<Object>(null);
        Wrapper.forEachInventorySlot(9, 45, (slot, stack) -> {
            double newDamage;
            if (stack.getItem() instanceof ItemSword && (newDamage = InventoryUtils.getItemDamage(stack)) > damage.get()) {
                damage.set(newDamage);
                bestStack.set(stack);
            }
        });
        return bestStack.get() == itemStack || damage.get() < InventoryUtils.getItemDamage(itemStack);
    }

    public static int getToolType(ItemStack stack) {
        ItemTool tool = (ItemTool)stack.getItem();
        if (tool instanceof ItemPickaxe) {
            return 0;
        }
        if (tool instanceof ItemAxe) {
            return 1;
        }
        if (tool instanceof ItemSpade) {
            return 2;
        }
        return -1;
    }

    public static boolean isBestTool(ItemStack itemStack) {
        int type2 = InventoryUtils.getToolType(itemStack);
        AtomicReference<Tool> bestTool = new AtomicReference<Tool>(new Tool(-1, -1.0, null));
        Wrapper.forEachInventorySlot(9, 45, (slot, stack) -> {
            double efficiency;
            if (stack.getItem() instanceof ItemTool && type2 == InventoryUtils.getToolType(stack) && (efficiency = (double)InventoryUtils.getToolEfficiency(stack)) > ((Tool)bestTool.get()).getEfficiency()) {
                bestTool.set(new Tool(slot, efficiency, stack));
            }
        });
        return bestTool.get().getStack() == itemStack || bestTool.get().getEfficiency() < (double)InventoryUtils.getToolEfficiency(itemStack);
    }

    public static float getToolEfficiency(ItemStack itemStack) {
        ItemTool tool = (ItemTool)itemStack.getItem();
        float efficiency = tool.getEfficiencyOnProperMaterial();
        int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
        if (efficiency > 1.0f && lvl > 0) {
            efficiency += (float)(lvl * lvl + 1);
        }
        return efficiency;
    }

    public static boolean isBestArmor(ItemStack itemStack) {
        ItemArmor itemArmor = (ItemArmor)itemStack.getItem();
        AtomicDouble reduction = new AtomicDouble(0.0);
        AtomicReference<Object> bestStack = new AtomicReference<Object>(null);
        Wrapper.forEachInventorySlot(5, 45, (slot, stack) -> {
            if (stack.getItem() instanceof ItemArmor) {
                double newReduction;
                ItemArmor stackArmor = (ItemArmor)stack.getItem();
                if (stackArmor.armorType == itemArmor.armorType && (newReduction = InventoryUtils.getDamageReduction(stack)) > reduction.get()) {
                    reduction.set(newReduction);
                    bestStack.set(stack);
                }
            }
        });
        return bestStack.get() == itemStack || reduction.get() < InventoryUtils.getDamageReduction(itemStack);
    }

    public static boolean isGoodBlockStack(ItemStack stack) {
        if (stack.stackSize < 1) {
            return false;
        }
        return InventoryUtils.isValidBlock(Block.getBlockFromItem(stack.getItem()), true);
    }

    public static boolean isValidBlock(Block block, boolean toPlace) {
        if (block instanceof BlockContainer || !block.isFullBlock() || !block.isFullCube() || toPlace && block instanceof BlockFalling) {
            return false;
        }
        Material material = block.getMaterial();
        return !material.isLiquid() && material.isSolid();
    }

    public static double getItemDamage(ItemStack stack) {
        double damage = 0.0;
        Multimap<String, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers();
        for (String attributeName : attributeModifierMap.keySet()) {
            if (!attributeName.equals("generic.attackDamage")) continue;
            Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
            if (!attributeModifiers.hasNext()) break;
            damage += attributeModifiers.next().getAmount();
            break;
        }
        if (stack.isItemEnchanted()) {
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            damage += (double)EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
        }
        return damage;
    }

    @Listener
    public void onClick(WindowClickEvent event) {
        CLICK_TIMER.reset();
    }

    @Listener
    public void onPacket(PacketSendEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            CLICK_TIMER.reset();
        }
    }

    private static class Tool {
        private final int slot;
        private final double efficiency;
        private final ItemStack stack;

        public Tool(int slot, double efficiency, ItemStack stack) {
            this.slot = slot;
            this.efficiency = efficiency;
            this.stack = stack;
        }

        public int getSlot() {
            return this.slot;
        }

        public double getEfficiency() {
            return this.efficiency;
        }

        public ItemStack getStack() {
            return this.stack;
        }
    }

    public enum ClickType {
        CLICK,
        SHIFT_CLICK,
        SWAP_WITH_HOT_BAR_SLOT,
        PLACEHOLDER,
        DROP_ITEM;

    }
}

