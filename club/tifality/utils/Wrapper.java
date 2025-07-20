package club.tifality.utils;

import club.tifality.utils.render.TTFUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.MinecraftFontRenderer;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Timer;
import club.tifality.gui.font.TrueTypeFontRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class Wrapper {
    private static final TrueTypeFontRenderer bigFontRenderer = new TrueTypeFontRenderer(TTFUtils.getFontFromLocation("font.ttf", 21), true, true);
    private static final TrueTypeFontRenderer fontRenderer = new TrueTypeFontRenderer(TTFUtils.getFontFromLocation("font.ttf", 20), true, true);
    private static final TrueTypeFontRenderer nameTagFontRenderer = new TrueTypeFontRenderer(TTFUtils.getFontFromLocation("font.ttf", 18), true, true);
    private static final TrueTypeFontRenderer sfBold12FontRenderer = new TrueTypeFontRenderer(TTFUtils.getFontFromLocation("SFBOLD.ttf", 12), true, true);
    private static final TrueTypeFontRenderer sfBold18FontRenderer = new TrueTypeFontRenderer(TTFUtils.getFontFromLocation("SFBOLD.ttf", 18), true, true);
    private static final TrueTypeFontRenderer sfBold20FontRenderer = new TrueTypeFontRenderer(TTFUtils.getFontFromLocation("SFBOLD.ttf", 20), true, true);
    private static final TrueTypeFontRenderer csgoFontRenderer = new TrueTypeFontRenderer(new Font("Tahoma", Font.BOLD, 11), true, false);
    private static final TrueTypeFontRenderer espBiggerFontRenderer = new TrueTypeFontRenderer(new Font("Tahoma Bold", Font.PLAIN, 12), true, false);
    private static final TrueTypeFontRenderer espFontRenderer = new TrueTypeFontRenderer(new Font("Tahoma", Font.PLAIN, 10), false, false);
    private static final TrueTypeFontRenderer testFontRenderer = new TrueTypeFontRenderer(new Font("Tahoma", Font.BOLD, 16), true, true);
    private static final TrueTypeFontRenderer testFontRenderer1 = new TrueTypeFontRenderer(new Font("Tahoma", Font.BOLD, 16), true, false);
    private static final TrueTypeFontRenderer verdana10 = new TrueTypeFontRenderer(new Font("Verdana", Font.PLAIN, 10), false, true);
    private static final TrueTypeFontRenderer verdana16 = new TrueTypeFontRenderer(new Font("Verdana", Font.PLAIN, 9), false, true);
    private static final TrueTypeFontRenderer titleFont = new TrueTypeFontRenderer(new Font("Tahoma", Font.PLAIN, 20), true, false);
    private static final TrueTypeFontRenderer infoFont = new TrueTypeFontRenderer(new Font("Tahoma", Font.BOLD, 16), true, false);

    public static TrueTypeFontRenderer getCSGOFontRenderer() {
        return csgoFontRenderer;
    }

    public static TrueTypeFontRenderer getNameTagFontRenderer() {
        return nameTagFontRenderer;
    }

    public static TrueTypeFontRenderer getSFBold12Font() {
        return sfBold12FontRenderer;
    }

    public static TrueTypeFontRenderer getSFBold18Font() {
        return sfBold18FontRenderer;
    }

    public static TrueTypeFontRenderer getSFBold20Font() {
        return sfBold20FontRenderer;
    }

    public static TrueTypeFontRenderer getFontRenderer() {
        return fontRenderer;
    }

    public static TrueTypeFontRenderer getBigFontRenderer() {
        return bigFontRenderer;
    }

    public static TrueTypeFontRenderer getTitleFont() {
        return titleFont;
    }

    public static TrueTypeFontRenderer getInfoFont() {
        return infoFont;
    }

    public static TrueTypeFontRenderer getVerdana10() {
        return verdana10;
    }

    public static TrueTypeFontRenderer getVerdana16() {
        return verdana16;
    }

    public static TrueTypeFontRenderer getTestFont() {
        return testFontRenderer;
    }

    public static TrueTypeFontRenderer getTestFont1() {
        return testFontRenderer1;
    }

    public static TrueTypeFontRenderer getEspFontRenderer() {
        return espFontRenderer;
    }

    public static TrueTypeFontRenderer getEspBiggerFontRenderer() {
        return espBiggerFontRenderer;
    }

    public static EntityRenderer getEntityRenderer() {
        return getMinecraft().entityRenderer;
    }

    public static Minecraft getMinecraft() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP getPlayer() {
        return getMinecraft().thePlayer;
    }

    public static WorldClient getWorld() {
        return getMinecraft().theWorld;
    }

    public static MinecraftFontRenderer getMinecraftFontRenderer() {
        return getMinecraft().fontRendererObj;
    }

    public static PlayerControllerMP getPlayerController() {
        return getMinecraft().playerController;
    }

    public static NetHandlerPlayClient getNetHandler() {
        return getMinecraft().getNetHandler();
    }

    public static GameSettings getGameSettings() {
        return getMinecraft().gameSettings;
    }

    public static boolean isInFirstPerson() {
        return getGameSettings().thirdPersonView == 0;
    }

    public static ItemStack getStackInSlot(int index) {
        return getPlayer().inventoryContainer.getSlot(index).getStack();
    }

    public static Timer getTimer() {
        return getMinecraft().getTimer();
    }

    public static Block getBlock(BlockPos pos) {
        return getWorld().getBlockState(pos).getBlock();
    }

    public static void addChatMessage(String message) {
        getPlayer().addChatMessage(new ChatComponentText("\2478[\247CT\2478]\2477 " + message));
    }

    public static GuiScreen getCurrentScreen() {
        return getMinecraft().currentScreen;
    }

    public static List<EntityPlayer> getLoadedPlayers() {
        return getWorld().playerEntities;
    }

    public static List<EntityLivingBase> getLivingEntities(Predicate<EntityLivingBase> validator) {
        List<EntityLivingBase> entities = new ArrayList<>();

        for (Entity entity : getWorld().loadedEntityList) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase e = (EntityLivingBase) entity;
                if (validator.test(e))
                    entities.add(e);
            }
        }

        return entities;
    }

    public static void forEachInventorySlot(int begin, int end, SlotConsumer consumer) {
        for (int i = begin; i < end; ++i) {
            ItemStack stack = Wrapper.getStackInSlot(i);
            if (stack == null) continue;
            consumer.accept(i, stack);
        }
    }

    public static void sendPacket(Packet<?> packet) {
        getNetHandler().sendPacket(packet);
    }

    public static void sendPacketDirect(Packet<?> packet) {
        getNetHandler().getNetworkManager().sendPacket(packet);
    }

    @FunctionalInterface
    public interface SlotConsumer {
        void accept(int slot, ItemStack stack);
    }
}
