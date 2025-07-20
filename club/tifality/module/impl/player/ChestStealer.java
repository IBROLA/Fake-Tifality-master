package club.tifality.module.impl.player;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketReceiveEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.Representation;
import club.tifality.utils.Wrapper;
import club.tifality.utils.inventory.InventoryUtils;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;

@ModuleInfo(label="ChestStealer", category= ModuleCategory.PLAYER)
public final class ChestStealer extends Module {
    private final DoubleProperty clickDelayProperty = new DoubleProperty("Delay", 100.0, 0.0, 100.0, 10.0, Representation.MILLISECONDS);
    private final DoubleProperty closeDelayProperty = new DoubleProperty("Close Delay", 100.0, 0.0, 100.0, 10.0, Representation.MILLISECONDS);
    private final Property<Boolean> chestCheckValue = new Property<>("Chest Check", false);
    public final EnumProperty<Mode> modeValue = new EnumProperty<>("Mode", Mode.OPENINV);
    private final TimerUtil timer = new TimerUtil();

    @Listener
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S2DPacketOpenWindow) {
            this.timer.reset();
        }
    }

    @Listener
    public void onUpdatePositionEvent(UpdatePositionEvent e) {
        GuiChest chest;
        if (e.isPre() && Wrapper.getCurrentScreen() instanceof GuiChest && ((chest = (GuiChest)Wrapper.getCurrentScreen()).getLowerChestInventory().getDisplayName().getUnformattedText().contains("Chest") || !this.chestCheckValue.get().booleanValue())) {
            if (this.isInventoryFull() || this.isChestEmpty(chest)) {
                if (this.timer.hasElapsed(this.closeDelayProperty.getValue().longValue())) {
                    Wrapper.getPlayer().closeScreen();
                }
                return;
            }
            for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
                if (!this.timer.hasElapsed(this.clickDelayProperty.getValue().longValue()) || !InventoryUtils.isValid(chest.getLowerChestInventory().getStackInSlot(i))) continue;
                InventoryUtils.windowClick(chest.inventorySlots.windowId, i, 0, InventoryUtils.ClickType.SHIFT_CLICK);
                this.timer.reset();
                return;
            }
        }
    }

    private boolean isChestEmpty(GuiChest chest) {
        for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); ++i) {
            if (!InventoryUtils.isValid(chest.getLowerChestInventory().getStackInSlot(i))) continue;
            return false;
        }
        return true;
    }

    private boolean isInventoryFull() {
        for (int i = 9; i < 45; ++i) {
            if (Wrapper.getPlayer().inventoryContainer.getSlot(i).getHasStack()) continue;
            return false;
        }
        return true;
    }

    public enum Mode {
        OPENINV,
        BACKGROUND;
    }
}

