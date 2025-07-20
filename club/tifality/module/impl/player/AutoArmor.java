package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.Representation;
import club.tifality.utils.inventory.ArmorComparator;
import club.tifality.utils.inventory.ArmorPiece;
import club.tifality.utils.inventory.InventoryUtils;
import club.tifality.utils.timer.TimeUtils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ModuleInfo(label="AutoArmor", category= ModuleCategory.PLAYER)
public class AutoArmor extends Module {
    public static final ArmorComparator ARMOR_COMPARATOR = new ArmorComparator();
    private final DoubleProperty maxDelayValue = new DoubleProperty("Max Delay", 100.0, 0.0, 400.0, 5.0, Representation.MILLISECONDS);
    private final DoubleProperty minDelayValue = new DoubleProperty("Min Delay", 100.0, 0.0, 400.0, 5.0, Representation.MILLISECONDS);
    private final DoubleProperty itemDelayValue = new DoubleProperty("Item Delay", 0.0, 0.0, 500.0, 5.0, Representation.MILLISECONDS);
    private final Property<Boolean> invOpenValue = new Property<>("Open Inventory", false);
    private long delay;

    @Listener
    public void onRender3D(UpdatePositionEvent event) {
        if (!InventoryUtils.CLICK_TIMER.hasTimePassed(this.delay) || mc.thePlayer.openContainer != null && mc.thePlayer.openContainer.windowId != 0) {
            return;
        }
        Map<Integer, List<ArmorPiece>> armorPieces = IntStream.range(0, 36).filter(i -> {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            return itemStack != null && itemStack.getItem() instanceof ItemArmor && (i < 9 || (double)(System.currentTimeMillis() - itemStack.getItemDelay()) >= this.itemDelayValue.get());
        }).mapToObj(i -> new ArmorPiece(mc.thePlayer.inventory.getStackInSlot(i), i)).collect(Collectors.groupingBy(ArmorPiece::getArmorType));
        ArmorPiece[] bestArmor = new ArmorPiece[4];
        for (Map.Entry<Integer, List<ArmorPiece>> armorEntry : armorPieces.entrySet()) {
            bestArmor[armorEntry.getKey()] = armorEntry.getValue().stream().max(ARMOR_COMPARATOR).orElse(null);
        }
        for (int i2 = 0; i2 < 4; ++i2) {
            int armorSlot;
            ArmorPiece oldArmor;
            ArmorPiece armorPiece = bestArmor[i2];
            if (armorPiece == null || (oldArmor = new ArmorPiece(mc.thePlayer.inventory.armorItemInSlot(armorSlot = 3 - i2), -1)).getItemStack() != null && oldArmor.getItemStack().getItem() instanceof ItemArmor && ARMOR_COMPARATOR.compare(oldArmor, armorPiece) >= 0) continue;
            if (oldArmor.getItemStack() != null && this.move(8 - armorSlot, true)) {
                return;
            }
            if (mc.thePlayer.inventory.armorItemInSlot(armorSlot) != null || !this.move(armorPiece.getSlot(), false)) continue;
            return;
        }
    }

    private boolean move(int item, boolean isArmorSlot) {
        if (!isArmorSlot && item < 9 && !(mc.currentScreen instanceof GuiInventory)) {
            mc.getNetHandler().sendPacket(new C09PacketHeldItemChange(item));
            mc.getNetHandler().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(item).getStack()));
            mc.getNetHandler().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            this.delay = TimeUtils.randomDelay(this.minDelayValue.get().intValue(), this.maxDelayValue.get().intValue());
            return true;
        }
        if ((!this.invOpenValue.get() || mc.currentScreen instanceof GuiInventory) && item != -1) {
            boolean openInventory = !(mc.currentScreen instanceof GuiInventory);
            if (openInventory) {
                mc.getNetHandler().sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            }
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, isArmorSlot ? item : (item < 9 ? item + 36 : item), 0, 1, mc.thePlayer);
            this.delay = TimeUtils.randomDelay(this.minDelayValue.get().intValue(), this.maxDelayValue.get().intValue());
            if (openInventory) {
                mc.getNetHandler().sendPacket(new C0DPacketCloseWindow());
            }
            return true;
        }
        return false;
    }
}

