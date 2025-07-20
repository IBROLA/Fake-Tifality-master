package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.utils.render.Colors;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.RenderingUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleInfo(label = "TargetHUD", category = ModuleCategory.RENDER)
public class TargetHUD extends Module {
    @Listener
    public void onEvent(Render2DEvent event) {
        ScaledResolution scaledRes = new ScaledResolution(mc);
        float width = (float)scaledRes.getScaledWidth();
        float height = (float)scaledRes.getScaledHeight();
        EntityLivingBase target = (mc.currentScreen instanceof GuiChat) ? mc.thePlayer : KillAura.getInstance().getTarget();
        if (target != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(width / 2.0f + 10.0f - 2.0f, height - 90.0f - 66.0f, 0.0f);
            RenderingUtils.targetHudRect(0.0, -2.0, (Wrapper.getTestFont().getWidth(target.getName()) > 70.0f) ? ((double)(124.0f + Wrapper.getTestFont().getWidth(target.getName()) - 70.0f)) : 124.0, 38.0, 1.0);
            RenderingUtils.targetHudRect1(0.0, -2.0, 124.0, 38.0, 1.0);
            Wrapper.getTestFont().drawString(target.getName(), 42.0f, 0.5f, -1, true);
            float health = target.getHealth();
            float totalHealth = target.getHealth() + target.getAbsorptionAmount();
            float[] fractions = { 0.0f, 0.5f, 1.0f };
            Color[] colors = { Color.RED, Color.YELLOW, Color.GREEN };
            float progress = health / target.getMaxHealth();
            Color customColor = (health >= 0.0f) ? Colors.blendColors(fractions, colors, progress).brighter() : Color.RED;
            double width2 = 0.0;
            width2 = Colors.getIncremental(width2, 5.0);
            if (width2 < 50.0) {
                width2 = 50.0;
            }
            double healthLocation = width2 * progress;
            RenderingUtils.rectangle(42.5, 10.3, 53.0 + width2 + 0.5, 13.5, customColor.darker().darker().darker().darker().getRGB());
            RenderingUtils.rectangle(42.5, 10.3, 53.0 + healthLocation + 0.5, 13.5, customColor.getRGB());
            if (target.getAbsorptionAmount() > 0.0f) {
                RenderingUtils.rectangle(97.5 - target.getAbsorptionAmount(), 10.3, 103.5, 13.5, new Color(137, 112, 9).getRGB());
            }
            RenderingUtils.drawRectBordered(42.0, 9.800000190734863, 54.0 + width2, 14.0, 0.5, Colors.getColor(0, 0), Colors.getColor(0));
            for (int dist = 1; dist < 10; ++dist) {
                double dThing = width2 / 8.5 * dist;
                RenderingUtils.rectangle(43.5 + dThing, 9.8, 43.5 + dThing + 0.5, 14.0, Colors.getColor(0));
            }
            GlStateManager.scale(0.5, 0.5, 0.5);
            int dist = (int)mc.thePlayer.getDistanceToEntity(target);
            String str = "HP: " + (int)totalHealth + " | Dist: " + dist;
            mc.fontRendererObj.drawString(str, 85.6f, 32.0f, -1, true);
            GlStateManager.scale(2.0, 2.0, 2.0);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            if (target instanceof EntityPlayer) {
                this.drawArmor(28, 19);
            }
            GlStateManager.scale(0.31, 0.31, 0.31);
            GlStateManager.translate(73.0f, 102.0f, 40.0f);
            this.model(target.rotationYaw, target.rotationPitch, target);
            GlStateManager.popMatrix();
        }
    }

    private void model(float yaw, float pitch, EntityLivingBase entityLivingBase) {
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        float renderYawOffset = entityLivingBase.renderYawOffset;
        float rotationYaw = entityLivingBase.rotationYaw;
        float rotationPitch = entityLivingBase.rotationPitch;
        float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float)(-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - yaw / yaw * 0.4f;
        entityLivingBase.rotationYaw = yaw - yaw / yaw * 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }

    private void drawArmor(int x, int y) {
        GL11.glPushMatrix();
        List<ItemStack> stuff = new ArrayList<>();
        int split = -3;
        for (int index = 3; index >= 0; --index) {
            ItemStack armorer = mc.thePlayer.inventory.armorInventory[index];
            if (armorer != null) {
                stuff.add(armorer);
            }
        }
        if (mc.thePlayer.getCurrentEquippedItem() != null) {
            stuff.add(mc.thePlayer.getCurrentEquippedItem());
        }
        for (ItemStack everything : stuff) {
            if (mc.theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                split += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            mc.getRenderItem().zLevel = -150.0f;
            mc.getRenderItem().renderItemIntoGUI(everything, split + x, y);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, everything, split + x, y);
            RenderingUtils.renderEnchantText(everything, split + x, y);
            mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            everything.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
    }
}
