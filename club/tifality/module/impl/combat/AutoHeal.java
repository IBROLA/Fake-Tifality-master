package club.tifality.module.impl.combat;

import club.tifality.gui.notification.client.NotificationPublisher;
import club.tifality.gui.notification.client.NotificationType;
import club.tifality.gui.notification.dev.DevNotifications;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketSendEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.tuple.Pair;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.Representation;
import club.tifality.utils.movement.MovementUtils;

import java.util.Random;

@ModuleInfo(label="AutoHeal", category= ModuleCategory.COMBAT)
public class AutoHeal extends Module {
    private final DoubleProperty percent = new DoubleProperty("Health Percent", 75.0, 1.0, 100.0, 1.0, Representation.PERCENTAGE);
    private final DoubleProperty min = new DoubleProperty("Min Delay", 75.0, 1.0, 5000.0, 5.0, Representation.MILLISECONDS);
    private final DoubleProperty max = new DoubleProperty("Max Delay", 125.0, 1.0, 5000.0, 5.0, Representation.MILLISECONDS);
    private final DoubleProperty regenSec = new DoubleProperty("Regen Sec", 4.5, 0.0, 10.0, 0.5, Representation.MILLISECONDS);
    private final Property<Boolean> groundCheck = new Property<>("Ground Check", true);
    private final Property<Boolean> voidCheck = new Property<>("Void Check", true);
    private final Property<Boolean> waitRegen = new Property<>("Wai tRegen", true);
    private final Property<Boolean> invCheck = new Property<>("Inv Check", true);
    private final Property<Boolean> absorpCheck = new Property<>("Absorp Check", true);
    final TimerUtil timer = new TimerUtil();
    int delay;
    boolean isDisable;

    @Override
    public void onEnable() {
        this.timer.reset();
        this.isDisable = false;
        this.delay = MathHelper.getRandomIntegerInRange(new Random(), this.min.get().intValue(), this.max.get().intValue());
    }

    @Listener
    public void onPacket(PacketSendEvent e) {
        if (e.getPacket() instanceof S02PacketChat && ((S02PacketChat)e.getPacket()).getChatComponent().getFormattedText().contains("won the game! ")) {
            NotificationPublisher.queue("Heal", "Auto Healed", NotificationType.INFO, 2000);
            DevNotifications.getManager().post("Temp Disable Heal");
            this.isDisable = true;
        }
    }

    @Listener
    public void onUpdate(UpdatePositionEvent event) {
        if (mc.thePlayer.ticksExisted <= 5 && this.isDisable) {
            this.isDisable = false;
            NotificationPublisher.queue("Heal", "Enable Heal due to World Changed or Player Respawned.", NotificationType.INFO, 3000);
            DevNotifications.getManager().post("Enable Heal due to World Changed or Player Respawned");
        }
        int absorp = MathHelper.ceiling_double_int(mc.thePlayer.getAbsorptionAmount());
        if (this.groundCheck.get() && !mc.thePlayer.onGround || this.voidCheck.get() && !MovementUtils.isBlockUnder() || this.invCheck.get() && mc.currentScreen instanceof GuiContainer || absorp != 0 && this.absorpCheck.get()) {
            return;
        }
        if (this.waitRegen.get() && mc.thePlayer.isPotionActive(Potion.regeneration) && mc.thePlayer.getActivePotionEffect(Potion.regeneration).getDuration() > this.regenSec.get() * 20.0) {
            return;
        }
        Pair<Integer, ItemStack> pair = this.getGAppleSlot();
        if (!this.isDisable && pair != null && (mc.thePlayer.getHealth() <= this.percent.get() / 100.0 * mc.thePlayer.getMaxHealth() || !mc.thePlayer.isPotionActive(Potion.absorption) || absorp == 0 && mc.thePlayer.getHealth() == 20.0f && mc.thePlayer.isPotionActive(Potion.absorption)) && this.timer.hasElapsed(this.delay)) {
            NotificationPublisher.queue("Heal", "Auto Healed", NotificationType.INFO, 2000);
            DevNotifications.getManager().post("Auto Healed");
            int lastSlot = mc.thePlayer.inventory.currentItem;
            int slot = pair.getLeft();
            mc.getNetHandler().sendPacket(new C09PacketHeldItemChange(slot));
            ItemStack stack = pair.getRight();
            mc.getNetHandler().sendPacket(new C08PacketPlayerBlockPlacement(stack));
            for (int i = 0; i < 32; ++i) {
                mc.getNetHandler().sendPacket(new C03PacketPlayer());
            }
            mc.getNetHandler().sendPacket(new C09PacketHeldItemChange(lastSlot));
            mc.thePlayer.inventory.currentItem = lastSlot;
            mc.playerController.updateController();
            this.delay = MathHelper.getRandomIntegerInRange(new Random(), this.min.get().intValue(), this.max.get().intValue());
            this.timer.reset();
        }
    }

    private Pair<Integer, ItemStack> getGAppleSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || stack.getItem() != Items.golden_apple) continue;
            return Pair.of(i, stack);
        }
        return null;
    }

    public String getTag() {
        return String.format("%.2f HP", this.percent.get() / 100.0 * (double)mc.thePlayer.getMaxHealth());
    }
}

