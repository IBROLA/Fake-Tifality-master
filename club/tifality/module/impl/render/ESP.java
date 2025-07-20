package club.tifality.module.impl.render;

import club.tifality.Tifality;
import club.tifality.manager.event.impl.render.RenderNameTagEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.player.StreamerMode;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.ESPUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.Render3DEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.impl.other.HackerDetect;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.PlayerUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.RenderingUtils;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@ModuleInfo(label="ESP", category= ModuleCategory.RENDER)
public final class ESP extends Module {
    private final Property<Boolean> armor = new Property<>("Armor", true);
    private final Property<Boolean> health = new Property<>("Health", true);
    private final Property<Boolean> box = new Property<>("Box", true);
    private final Property<Boolean> item = new Property<>("Item", true);
    //private final Property<Boolean> nameTag = new Property<>("Name Tag", true);
    private final Property<Boolean> invis = new Property<>("Invisible", true);
    private final Property<Boolean> customName = new Property<>("Custom Name", false, item::get);
    public final EnumProperty<BoxMode> boxStyle = new EnumProperty<>("Box Mode", BoxMode.Corner);
    private final DoubleProperty width2d = new DoubleProperty("Box Width", 0.5, this.box::get, 0.1f, 1.0, 0.1f);
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0#", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
    private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final List<Vec3> positions = new ArrayList<>();

    @Listener
    public void onRender3D(Render3DEvent event) {
        /*if (this.nameTag.get() && mc.theWorld != null) {
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (entity != null) {
                    if (!isValid(entity)) continue;
                    String name = ModuleManager.getInstance(StreamerMode.class).isEnabled() ? "Player" : entity.getName();

                    if (!entity.isDead && ESPUtil.isInView(entity)) {
                        float health = entity.getHealth();
                        float[] fractions = {0.0f, 0.5f, 1.0f};
                        Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN};
                        float healthProgress = health / entity.getMaxHealth();
                        Color healthColor = (health >= 0.0f) ? Colors.blendColors(fractions, colors, healthProgress).brighter() : Color.RED;

                        //Changing size
                        float scale = Math.max(0.02F, mc.thePlayer.getDistanceToEntity(entity) / 300);

                        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.renderPosX;
                        double y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.renderPosY) + scale * 6;
                        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.renderPosZ;

                        GL11.glPushMatrix();
                        GL11.glTranslated(x, y + 2.3, z);
                        GlStateManager.disableDepth();

                        GL11.glScalef(-scale, -scale, -scale);

                        GL11.glRotated(-mc.getRenderManager().playerViewY, 0.0D, 1.0D, 0.0D);
                        GL11.glRotated(mc.getRenderManager().playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0D : 1.0D, 0.0D, 0.0D);

                        float width = mc.fontRendererObj.getWidth(name) - 7;
                        float progress = Math.min(entity.getHealth(), entity.getMaxHealth()) / entity.getMaxHealth();

                        //RenderingUtils.drawRectBordered(-width / 2.0F - 5.0F, -1, width / 2.0F + 5.0F, 8, 0.5f, new Color(0, 0, 0, 120).getRGB(), new Color(0, 0, 0).getRGB());
                        Gui.drawRect(-width / 2.0F - 5.0F, -1, width / 2.0F + 5.0F, 8, new Color(0, 0, 0, 120).getRGB());
                        float healthLocation = width / 2.0F + 5.0F - -width / 2.0F + 5.0F;
                        Gui.drawRect(-width / 2.0F - 5.0F, 7, -width / 2.0F - 5.0F + healthLocation * 1, 8, healthColor.darker().getRGB());
                        Gui.drawRect(-width / 2.0F - 5.0F, 7, -width / 2.0F - 5.0F + healthLocation * progress, 8, healthColor.getRGB());
                        if (entity.getAbsorptionAmount() > 0.0f) {
                            Gui.drawRect(-width / 2.0F + 5.0F + healthLocation - (entity.getAbsorptionAmount() * 4), 7, -width / 2.0F - 5.0F + healthLocation * 1, 8, new Color(137, 112, 9).getRGB());
                        }
                        mc.fontRendererObj.drawStringWithShadow(name, -width / 2.0F - 3.0F, -1.0f, -1);

                        GlStateManager.enableDepth();
                        GL11.glPopMatrix();
                    }
                }
            }
        }*/
        if (this.boxStyle.get() == BoxMode.Box) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!(entity instanceof EntityItem) && !this.isValid(entity)) continue;
                updateView();
            }
        }
        if (this.boxStyle.get() == BoxMode.Corner) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!(entity instanceof EntityItem) && !this.isValid(entity)) continue;
                updateView();
            }
        }
    }

    /*@Listener
    public void onRenderNameTag(RenderNameTagEvent e) {
        if (this.nameTag.get()) {
            e.setCancelled();
        }
    }*/

    @Listener
    public void onRender2DEvent(Render2DEvent e) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (this.boxStyle.get() == BoxMode.Corner || this.boxStyle.get() == BoxMode.Box) {
            GlStateManager.pushMatrix();
            GL11.glDisable(2929);
            double twoScale = (double)sr.getScaleFactor() / Math.pow(sr.getScaleFactor(), 2.0);
            GlStateManager.scale(twoScale, twoScale, twoScale);
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!this.isValid(entity)) continue;
                this.updatePositions(entity);
                int maxLeft = Integer.MAX_VALUE;
                int maxRight = Integer.MIN_VALUE;
                int maxBottom = Integer.MIN_VALUE;
                int maxTop = Integer.MAX_VALUE;
                Iterator<Vec3> positions = this.positions.iterator();
                boolean canEntityBeSeen = false;
                while (positions.hasNext()) {
                    Vec3 screenPosition = WorldToScreen(positions.next());
                    if (screenPosition == null || !(screenPosition.zCoord >= 0.0) || !(screenPosition.zCoord < 1.0)) continue;
                    maxLeft = (int)Math.min(screenPosition.xCoord, maxLeft);
                    maxRight = (int)Math.max(screenPosition.xCoord, maxRight);
                    maxBottom = (int)Math.max(screenPosition.yCoord, maxBottom);
                    maxTop = (int)Math.min(screenPosition.yCoord, maxTop);
                    canEntityBeSeen = true;
                }
                if (!canEntityBeSeen) continue;

                if (this.health.get()) {
                    this.drawHealth((EntityLivingBase)entity, maxLeft, maxTop, maxRight, maxBottom);
                }
                if (this.armor.get()) {
                    this.drawArmor((EntityLivingBase)entity, maxLeft, maxTop, maxRight, maxBottom);
                }
                if (this.box.get()) {
                    this.drawBox(entity, maxLeft, maxTop, maxRight, maxBottom);
                }
                if (((EntityPlayer)entity).getCurrentEquippedItem() != null && this.item.get()) {
                    this.drawItem(entity, maxLeft, maxTop, maxRight, maxBottom);
                }
                //if (!this.nameTag.get()) continue;
                //this.drawName(entity, maxLeft, maxTop, maxRight, maxBottom);
            }
            GL11.glEnable(2929);
            GlStateManager.popMatrix();
        }
    }

    /*private void drawName(Entity e, int left, int top, int right, int bottom) {
        HackerDetect el = Tifality.getInstance().getModuleManager().getModule(HackerDetect.class);
        EntityPlayer ent = (EntityPlayer)e;
        String renderName = Hud.getPing(ent) + "ms " + ent.getName();
        TrueTypeFontRenderer font = Wrapper.getEspBiggerFontRenderer();
        float meme2 = (float)((double)(right - left) / 2.0 - (double)font.getWidth(renderName));
        float halfWidth = font.getWidth(renderName) / 2.0f;
        float xDif = right - left;
        float middle = (float)left + xDif / 2.0f;
        float textHeight = font.getHeight(renderName);
        float renderY = (float)top - textHeight - 2.0f;
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, -1.0f, 0.0f);

        float health = ((EntityPlayer) e).getHealth();
        float[] fractions = { 0.0f, 0.5f, 1.0f };
        Color[] colors = { Color.RED, Color.YELLOW, Color.GREEN };
        float progress = health / ((EntityPlayer) e).getMaxHealth();
        Color customColor = (health >= 0.0f) ? Colors.blendColors(fractions, colors, progress).brighter() : Color.RED;
        double width2 = 0.0;
        width2 = Colors.getIncremental(width2, 5.0);
        if (width2 < 50.0) {
            width2 = 50.0;
        }
        double healthLocation = width2 * progress;

        if (el.isEnabled() && el.isHacker(ent) || PlayerUtils.isTeamMate(ent) || Tifality.getInstance().getFriendManager().isFriend(ent.getName())) {
            RenderingUtils.drawRect(middle - halfWidth * 2.0f - 2.0f, renderY - 10.0f, middle + halfWidth * 2.0f + 2.0f, renderY + textHeight - 0.5f, new Color(0, 0, 0).getRGB());
            RenderingUtils.drawRect(middle - halfWidth * 2.0f - 1.0f, renderY - 9.0f, middle + halfWidth * 2.0f + 0.5f, renderY + textHeight - 1.5f, this.getColor(ent));
            RenderingUtils.drawRect(middle - halfWidth * 2.0f - 2.0f, renderY + textHeight - 1.5f, middle - halfWidth * 2.0f - 2.0f + (float)healthLocation * 1.875f, renderY + textHeight - 1f, customColor.getRGB());
        } else {
            RenderingUtils.drawRectBordered(middle - halfWidth * 2.0f - 2.0f, renderY - 10.0f, middle + halfWidth * 2.0f + 2.0f, renderY + textHeight - 0.5f, 1.0f, new Color(0, 0, 0, 120).getRGB(), new Color(0, 0, 0).getRGB());
            RenderingUtils.drawRect(middle - halfWidth * 2.0f - 2.0f, renderY + textHeight - 1.5f, middle - halfWidth * 2.0f - 2.0f + (float)healthLocation * 1.875f, renderY + textHeight - 1f, customColor.getRGB());
        }
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        RenderingUtils.drawOutlineString(Wrapper.getEspBiggerFontRenderer(), renderName, ((float)left + meme2) / 2.0f, ((float)top - font.getHeight(renderName) / 1.5f * 2.0f) / 2.0f - 4.0f, new Color(255, 255, 255).getRGB(), new Color(0, 0, 0, 210).getRGB());
        GlStateManager.popMatrix();
    }*/

    private void drawItem(Entity e, int left, int top, int right, int bottom) {
        EntityPlayer ent = (EntityPlayer)e;
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        ItemStack stack = ent.getCurrentEquippedItem();
        String customName = this.customName.get() ? ent.getCurrentEquippedItem().getDisplayName() : ent.getCurrentEquippedItem().getItem().getItemStackDisplayName(stack);
        float meme5 = (float)((double)(right - left) / 2.0 - (double)Wrapper.getSFBold12Font().getWidth(customName));
        RenderingUtils.drawOutlineString(Wrapper.getSFBold12Font(), customName, ((float)left + meme5) / 2.0f, ((float)bottom + Wrapper.getSFBold12Font().getHeight(customName) / 2.0f * 2.0f) / 2.0f + 1.0f, -1, new Color(0, 0, 0, 210).getRGB());
        GlStateManager.popMatrix();
        if (stack != null) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemIntoGUI(stack, (int)((float)left + meme5) + 29, (int)((float)bottom + Wrapper.getSFBold12Font().getHeight(customName) / 2.0f * 2.0f) + 15);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, (int)((float)left + meme5) + 29, (int)((float)bottom + Wrapper.getSFBold12Font().getHeight(customName) / 2.0f * 2.0f) + 15);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    private void drawBox(Entity e, int left, int top, int right, int bottom) {
        int line = 1;
        int bg = new Color(0, 0, 0).getRGB();
        if (this.boxStyle.get() == BoxMode.Corner) {
            RenderingUtils.drawNewRect(left + 1.0f + (float)line, (float)(top - line), (float)(left - line), top + (bottom - top) / 3.0f + (float)line, bg);
            RenderingUtils.drawNewRect(left + (right - left) / 3.0f + (float)line, (float)(top + line), (float)left, top - 1.0f - (float)line, bg);
            RenderingUtils.drawNewRect((float)(right + line), (float)(top - line), right - 1.0f - (float)line, top + (bottom - top) / 3.0f + (float)line, bg);
            RenderingUtils.drawNewRect((float)right, (float)(top + line), right - (right - left) / 3.0f - (float)line, top - 1.0f - (float)line, bg);
            RenderingUtils.drawNewRect(left + 1.0f + (float)line, bottom - 1.0f - (float)line, (float)(left - line), bottom - 1.0f - (bottom - top) / 3.0f - (float)line, bg);
            RenderingUtils.drawNewRect(left + (right - left) / 3.0f + (float)line, (float)(bottom + line), (float)(left - line), bottom - 1.0f - (float)line, bg);
            RenderingUtils.drawNewRect((float)(right + line), bottom - 1.0f + (float)line, right - 1.0f - (float)line, bottom - 1.0f - (bottom - top) / 3.0f - (float)line, bg);
            RenderingUtils.drawNewRect((float)(right + line), (float)(bottom + line), right - (right - left) / 3.0f - (float)line, bottom - 1.0f - (float)line, bg);
            RenderingUtils.drawNewRect((float)left + 1.0f, (float)top, (float)left, (float)top + (bottom - top) / 3.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)left + (right - left) / 3.0f, (float)top, (float)left, (float)top - 1.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)right, (float)top, (float)right - 1.0f, (float)top + (bottom - top) / 3.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)right, (float)top, (float)right - (right - left) / 3.0f, (float)top - 1.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)left + 1.0f, (float)bottom - 1.0f, (float)left, bottom - 1.0f - (bottom - top) / 3.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)left + (right - left) / 3.0f, (float)bottom, (float)left, (float)bottom - 1.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)right, (float)bottom - 1.0f, (float)right - 1.0f, bottom - 1.0f - (bottom - top) / 3.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)right, (float)bottom, (float)right - (right - left) / 3.0f, (float)bottom - 1.0f, this.getColor(e).getRGB());
        } else if (this.boxStyle.get() == BoxMode.Box) {
            RenderingUtils.drawNewRect((float)(right + line), (float)(top + line), (float)(left - line), top - 1.0f - (float)line, bg);
            RenderingUtils.drawNewRect((float)(right + line), (float)(bottom + line), (float)(left - line), bottom - 1.0f - (float)line, bg);
            RenderingUtils.drawNewRect(left + 1.0f + (float)line, (float)top, (float)(left - line), (float)bottom, bg);
            RenderingUtils.drawNewRect((float)(right + line), (float)top, right - 1.0f - (float)line, (float)bottom, bg);
            RenderingUtils.drawNewRect((float)right, (float)top, (float)left, (float)top - 1.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)right, (float)bottom, (float)left, (float)bottom - 1.0f, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect((float)left + 1.0f, (float)top, (float)left, (float)bottom, this.getColor(e).getRGB());
            RenderingUtils.drawNewRect(right, top, right - 1.0f, (float)bottom, this.getColor(e).getRGB());
        }
    }

    private void drawArmor(EntityLivingBase entityLivingBase, float left, float top, float right, float bottom) {
        float height = bottom + 1.0f - top;
        float currentArmor = entityLivingBase.getTotalArmorValue();
        float armorPercent = currentArmor / 20.0f;
        float MOVE = 2.0f;
        int line = 1;
        if (mc.thePlayer.getDistanceToEntity(entityLivingBase) > 16.0f) {
            return;
        }
        for (int i = 0; i < 4; ++i) {
            double h = (bottom - top) / 4.0f;
            ItemStack itemStack = entityLivingBase.getEquipmentInSlot(i + 1);
            double difference = (double)(top - bottom) + 0.5;
            if (itemStack == null) continue;
            RenderingUtils.drawNewRect(right + 2.0f + 1.0f + MOVE, top - 2.0f, right + 1.0f - 1.0f + MOVE, bottom + 1.0f, new Color(25, 25, 25, 150).getRGB());
            RenderingUtils.drawNewRect(right + 3.0f + MOVE, top + height * (1.0f - armorPercent) - 1.0f, right + 1.0f + MOVE, bottom, new Color(78, 206, 229).getRGB());
            RenderingUtils.drawNewRect(right + 3.0f + MOVE + (float)line, bottom + 1.0f, right + 3.0f + MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
            RenderingUtils.drawNewRect(right + 1.0f + MOVE, bottom + 1.0f, right + 1.0f + MOVE - (float)line, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
            RenderingUtils.drawNewRect(right + 1.0f + MOVE, top - 1.0f, right + 3.0f + MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
            RenderingUtils.drawNewRect(right + 1.0f + MOVE, bottom + 1.0f, right + 3.0f + MOVE, bottom, new Color(0, 0, 0, 255).getRGB());
            RenderingUtils.renderItemStack(itemStack, (int)(right + 6.0f + MOVE), (int)((double)(bottom + 30.0f) - (double)(i + 1) * h));
            float scale = 1.0f;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(itemStack.getMaxDamage() - itemStack.getItemDamage()), (right + 6.0f + MOVE + (16.0f - (float)mc.fontRendererObj.getStringWidth(String.valueOf(itemStack.getMaxDamage() - itemStack.getItemDamage())) * scale) / 2.0f) / scale, (float)((int)((double)(bottom + 30.0f) - (double)(i + 1) * h) + 16) / scale, -1);
            GlStateManager.popMatrix();
            if (!(-difference > 50.0)) continue;
            for (int j = 1; j < 4; ++j) {
                double dThing = difference / 4.0 * (double)j;
                RenderingUtils.rectangle(right + 2.0f, (double)bottom - 0.5 + dThing, (double)right + 6.0, (double)bottom - 0.5 + dThing - 1.0, Colors.getColor(0));
            }
        }
    }

    private void drawHealth(EntityLivingBase entityLivingBase, float left, float top, float right, float bottom) {
        float height = bottom + 1.0f - top;
        float currentHealth = entityLivingBase.getHealth();
        float maxHealth = entityLivingBase.getMaxHealth();
        float healthPercent = currentHealth / maxHealth;
        float MOVE = 2.0f;
        int line = 1;
        String healthStr = "§f" + this.decimalFormat.format(currentHealth) + "§c❤";
        float bottom1 = top + height * (1.0f - healthPercent) - 1.0f;
        float health = entityLivingBase.getHealth();
        float[] fractions = new float[]{0.0f, 0.5f, 1.0f};
        Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        float progress = health / entityLivingBase.getMaxHealth();
        Color customColor = health >= 0.0f ? Colors.blendColors(fractions, colors, progress).brighter() : Color.RED;
        mc.fontRendererObj.drawStringWithShadow(healthStr, left - 3.0f - MOVE - (float)mc.fontRendererObj.getStringWidth(healthStr), bottom1, -1);
        RenderingUtils.drawNewRect(left - 3.0f - MOVE, bottom, left - 1.0f - MOVE, top - 1.0f, new Color(25, 25, 25, 150).getRGB());
        RenderingUtils.drawNewRect(left - 3.0f - MOVE, bottom, left - 1.0f - MOVE, bottom1, customColor.getRGB());
        RenderingUtils.drawNewRect(left - 3.0f - MOVE, bottom + 1.0f, left - 3.0f - MOVE - (float)line, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
        RenderingUtils.drawNewRect(left - 1.0f - MOVE + (float)line, bottom + 1.0f, left - 1.0f - MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
        RenderingUtils.drawNewRect(left - 3.0f - MOVE, top - 1.0f, left - 1.0f - MOVE, top - 2.0f, new Color(0, 0, 0, 255).getRGB());
        RenderingUtils.drawNewRect(left - 3.0f - MOVE, bottom + 1.0f, left - 1.0f - MOVE, bottom, new Color(0, 0, 0, 255).getRGB());
        double difference = (double)(top - bottom) + 0.5;
        if (-difference > 50.0) {
            for (int j = 1; j < 10; ++j) {
                double dThing = difference / 10.0 * (double)j;
                RenderingUtils.rectangle((double)left - 5.5, (double)bottom - 0.5 + dThing, (double)left - 2.5, (double)bottom - 0.5 + dThing - 1.0, Colors.getColor(0));
            }
        }
    }

    public Color getColor(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
            if (Tifality.getInstance().getFriendManager().isFriend((EntityPlayer)entityLivingBase)) {
                return new Color(0, 0, 255);
            }
            if (PlayerUtils.isTeamMate((EntityPlayer)entityLivingBase)) {
                return Color.GREEN;
            }
        }
        return new Color(255, 255, 255);
    }

    private static Vec3 WorldToScreen(Vec3 position) {
        FloatBuffer screenPositions = BufferUtils.createFloatBuffer(3);
        boolean result2 = GLU.gluProject((float)position.xCoord, (float)position.yCoord, (float)position.zCoord, modelView, projection, viewport, screenPositions);
        if (result2) {
            return new Vec3(screenPositions.get(0), (float) Display.getHeight() - screenPositions.get(1), screenPositions.get(2));
        }
        return null;
    }

    public void updatePositions(Entity entity) {
        this.positions.clear();
        Vec3 position = RenderingUtils.getEntityRenderPosition(entity);
        double x = position.xCoord - entity.posX;
        double y = position.yCoord - entity.posY;
        double z = position.zCoord - entity.posZ;
        double height = entity instanceof EntityItem ? 0.5 : (double)entity.height + 0.1;
        double width = entity instanceof EntityItem ? 0.25 : (Double)this.width2d.get();
        AxisAlignedBB aabb = new AxisAlignedBB(entity.posX - width + x, entity.posY + y, entity.posZ - width + z, entity.posX + width + x, entity.posY + height + y, entity.posZ + width + z);
        this.positions.add(new Vec3(aabb.minX, aabb.minY, aabb.minZ));
        this.positions.add(new Vec3(aabb.minX, aabb.minY, aabb.maxZ));
        this.positions.add(new Vec3(aabb.minX, aabb.maxY, aabb.minZ));
        this.positions.add(new Vec3(aabb.minX, aabb.maxY, aabb.maxZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.minY, aabb.minZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.minY, aabb.maxZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.maxY, aabb.minZ));
        this.positions.add(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ));
    }

    private int getColor(EntityLivingBase ent) {
        HackerDetect el = Tifality.getInstance().getModuleManager().getModule(HackerDetect.class);
        if (Tifality.getInstance().getFriendManager().isFriend(ent.getName())) {
            return new Color(10, 10, 255).getRGB();
        }
        if (ent.getName().equals(mc.thePlayer.getName())) {
            return new Color(50, 255, 50).getRGB();
        }
        if (el.isEnabled() && el.isHacker(ent)) {
            return new Color(255, 0, 0).getRGB();
        }
        if (PlayerUtils.isTeamMate((EntityPlayer)ent)) {
            return new Color(0, 200, 0).getRGB();
        }
        return new Color(200, 0, 0, 50).getRGB();
    }

    private boolean isValid(Entity entity) {
        if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) {
            return false;
        }
        if (!this.invis.get() && entity.isInvisible()) {
            return false;
        }
        return entity instanceof EntityPlayer;
    }

    private static void updateView() {
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
    }

    public enum BoxMode {
        Box,
        Corner;
    }
}

