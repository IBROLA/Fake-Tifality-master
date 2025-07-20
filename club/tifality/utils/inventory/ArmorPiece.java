package club.tifality.utils.inventory;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ArmorPiece {
    private final ItemStack itemStack;
    private final int slot;

    public ArmorPiece(ItemStack itemStack, int slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public int getArmorType() {
        return ((ItemArmor)this.itemStack.getItem()).armorType;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }
}

