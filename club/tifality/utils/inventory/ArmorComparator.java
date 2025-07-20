package club.tifality.utils.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ArmorComparator implements Comparator<ArmorPiece> {
    private static final Enchantment[] DAMAGE_REDUCTION_ENCHANTMENTS = new Enchantment[]{Enchantment.protection, Enchantment.projectileProtection, Enchantment.fireProtection, Enchantment.blastProtection};
    private static final float[] ENCHANTMENT_FACTORS = new float[]{1.5f, 0.4f, 0.39f, 0.38f};
    private static final float[] ENCHANTMENT_DAMAGE_REDUCTION_FACTOR = new float[]{0.04f, 0.08f, 0.15f, 0.08f};
    private static final Enchantment[] OTHER_ENCHANTMENTS = new Enchantment[]{Enchantment.featherFalling, Enchantment.thorns, Enchantment.respiration, Enchantment.aquaAffinity, Enchantment.unbreaking};
    private static final float[] OTHER_ENCHANTMENT_FACTORS = new float[]{3.0f, 1.0f, 0.1f, 0.05f, 0.01f};

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public int compare(ArmorPiece o1, ArmorPiece o2) {
        int compare = Double.compare(ArmorComparator.round(this.getThresholdedDamageReduction(o2.getItemStack()), 3), ArmorComparator.round(this.getThresholdedDamageReduction(o1.getItemStack()), 3));
        if (compare == 0) {
            int otherEnchantmentCmp = Double.compare(ArmorComparator.round(this.getEnchantmentThreshold(o1.getItemStack()), 3), ArmorComparator.round(this.getEnchantmentThreshold(o2.getItemStack()), 3));
            if (otherEnchantmentCmp == 0) {
                int enchantmentCountCmp = Integer.compare(ArmorComparator.getEnchantmentCount(o1.getItemStack()), ArmorComparator.getEnchantmentCount(o2.getItemStack()));
                if (enchantmentCountCmp != 0) {
                    return enchantmentCountCmp;
                }
                ItemArmor o1a = (ItemArmor)o1.getItemStack().getItem();
                ItemArmor o2a = (ItemArmor)o2.getItemStack().getItem();
                int durabilityCmp = Integer.compare(o1a.getArmorMaterial().getDurability(o1a.armorType), o2a.getArmorMaterial().getDurability(o2a.armorType));
                if (durabilityCmp != 0) {
                    return durabilityCmp;
                }
                int echantabilityCount = Integer.compare(o1a.getArmorMaterial().getEnchantability(), o2a.getArmorMaterial().getEnchantability());
                if (echantabilityCount != 0) {
                    return echantabilityCount;
                }
                return Integer.compare(o2.getItemStack().getItemDamage(), o1.getItemStack().getItemDamage());
            }
            return otherEnchantmentCmp;
        }
        return compare;
    }

    private float getThresholdedDamageReduction(ItemStack itemStack) {
        ItemArmor item = (ItemArmor)itemStack.getItem();
        return this.getDamageReduction(item.getArmorMaterial().getDamageReductionAmount(item.armorType), 0) * (1.0f - this.getThresholdedEnchantmentDamageReduction(itemStack));
    }

    private float getDamageReduction(int defensePoints, int toughness) {
        return 1.0f - Math.min(20.0f, Math.max((float)defensePoints / 5.0f, (float)defensePoints - 1.0f / (2.0f + (float)toughness / 4.0f))) / 25.0f;
    }

    private float getThresholdedEnchantmentDamageReduction(ItemStack itemStack) {
        float sum = 0.0f;
        for (int i = 0; i < DAMAGE_REDUCTION_ENCHANTMENTS.length; ++i) {
            sum += (float)ArmorComparator.getEnchantment(itemStack, DAMAGE_REDUCTION_ENCHANTMENTS[i]) * ENCHANTMENT_FACTORS[i] * ENCHANTMENT_DAMAGE_REDUCTION_FACTOR[i];
        }
        return sum;
    }

    private float getEnchantmentThreshold(ItemStack itemStack) {
        float sum = 0.0f;
        for (int i = 0; i < OTHER_ENCHANTMENTS.length; ++i) {
            sum += (float)ArmorComparator.getEnchantment(itemStack, OTHER_ENCHANTMENTS[i]) * OTHER_ENCHANTMENT_FACTORS[i];
        }
        return sum;
    }

    public static int getEnchantment(ItemStack itemStack, Enchantment enchantment) {
        if (itemStack == null || itemStack.getEnchantmentTagList() == null || itemStack.getEnchantmentTagList().hasNoTags()) {
            return 0;
        }
        for (int i = 0; i < itemStack.getEnchantmentTagList().tagCount(); ++i) {
            NBTTagCompound tagCompound = itemStack.getEnchantmentTagList().getCompoundTagAt(i);
            if ((!tagCompound.hasKey("ench") || tagCompound.getShort("ench") != enchantment.effectId) && (!tagCompound.hasKey("id") || tagCompound.getShort("id") != enchantment.effectId)) continue;
            return tagCompound.getShort("lvl");
        }
        return 0;
    }

    public static int getEnchantmentCount(ItemStack itemStack) {
        if (itemStack == null || itemStack.getEnchantmentTagList() == null || itemStack.getEnchantmentTagList().hasNoTags()) {
            return 0;
        }
        int c = 0;
        for (int i = 0; i < itemStack.getEnchantmentTagList().tagCount(); ++i) {
            NBTTagCompound tagCompound = itemStack.getEnchantmentTagList().getCompoundTagAt(i);
            if (!tagCompound.hasKey("ench") && !tagCompound.hasKey("id")) continue;
            ++c;
        }
        return c;
    }
}

