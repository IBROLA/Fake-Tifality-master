package club.tifality.module.impl.render;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.Render3DEvent;
import club.tifality.manager.event.impl.render.RenderNameTagEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.MultiSelectEnumProperty;
import club.tifality.utils.PlayerUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.ESPUtil;
import club.tifality.utils.render.RenderingUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ModuleInfo(label = "NameTag", category = ModuleCategory.RENDER)
public final class NameTag extends Module {
    private final EnumProperty<TagFont> tagFont = new EnumProperty<>("Font", TagFont.SFBold);
    private final MultiSelectEnumProperty<Option> options = new MultiSelectEnumProperty<>("Option", Option.Distance, Option.Background);
    private final EnumProperty<HealthType> healthType = new EnumProperty<>("Health Type", HealthType.Bar, () -> options.isSelected(Option.Health));
    private final DoubleProperty alpha = new DoubleProperty("Alpha", 100, () -> options.isSelected(Option.Background), 50, 255, 1);
    private final List<Player> validEntities = new CopyOnWriteArrayList<>();

    @Override
    public void onDisable() {
        this.validEntities.clear();
    }

    private Player getPlayerByEntity(EntityLivingBase entity) {
        return this.validEntities.stream().filter(player -> player.entity.equals(entity)).findFirst().orElse(null);
    }

    @Listener
    public void onNameTagRender(RenderNameTagEvent event) {
        event.setCancelled();
    }

    @Listener
    public void onRender2D(Render2DEvent event) {
        this.validEntities.forEach(Player::render);
    }

    @Listener
    private void onRender3D(Render3DEvent event) {
        mc.theWorld.getLoadedEntityList().stream() //
                .filter(EntityPlayer.class::isInstance) //
                .filter(entity -> !entity.isInvisible()) //
                .filter(Entity::isEntityAlive) //
                .map(EntityLivingBase.class::cast) //
                .filter(entity -> !this.validEntities.contains(getPlayerByEntity(entity))) //
                .forEach(entity -> this.validEntities.add(new Player(entity))); //

        this.validEntities.forEach(player -> {
            if (!player.entity.isEntityAlive() || !ESPUtil.isInView(player.entity)) {
                this.validEntities.remove(player);
            }

            if (!mc.theWorld.getLoadedEntityList().contains(player.entity))
                this.validEntities.remove(player);

            final float x = (float) (player.entity.lastTickPosX + (player.entity.posX - player.entity.lastTickPosX) * event.getPartialTicks() - RenderManager.renderPosX), //
                    y = (float) (player.entity.lastTickPosY + 2.3 + (player.entity.posY + 2.3 - (player.entity.lastTickPosY + 2.3)) * event.getPartialTicks() - RenderManager.renderPosY), //
                    z = (float) (player.entity.lastTickPosZ + (player.entity.posZ - player.entity.lastTickPosZ) * event.getPartialTicks() - RenderManager.renderPosZ);
            player.positions = player.convertTo2D(x, y, z);
        });
    }

    private class Player {

        private final EntityLivingBase entity;
        private double[] positions = {0, 0, 0};

        public Player(EntityLivingBase entity) {
            this.entity = entity;
        }

        void render() {
            GL11.glPushMatrix();
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final float x = (float) (this.positions[0] / scaledResolution.getScaleFactor()), //
                    y = (float) (this.positions[1] / scaledResolution.getScaleFactor()), //
                    z = (float) (this.positions[2] / scaledResolution.getScaleFactor());

            final String health = options.isSelected(Option.Health) ? healthType.get() == HealthType.Value ? " " + (int) (this.entity.getHealth() + this.entity.getAbsorptionAmount()) : "" : "";//
            final String distance = options.isSelected(Option.Distance) ? " " + (int) mc.thePlayer.getDistanceToEntity(this.entity) + "m" : "";
            String formattedName = this.entity.getDisplayName().getFormattedText();

            GL11.glTranslatef(x, y, z);

            /*float amp = 1;
            switch (mc.gameSettings.guiScale) {
                case 0:
                    amp = 0.5F;
                    break;
                case 1:
                    amp = 2.0F;
                    break;
                case 3:
                    amp = 0.6666666666666667F;
            }*/


            if (this.positions[2] < 0.0 || this.positions[2] >= 1.0) {
                GlStateManager.popMatrix();
                return;
            }

            ScaledResolution res = new ScaledResolution(mc);
            double scale2 = res.getScaleFactor() / Math.pow(res.getScaleFactor(), 2.0);
            GL11.glScaled(scale2, scale2, scale2);

            GlStateManager.disableDepth();
            String content = (Tifality.getInstance().getFriendManager().isFriend(entity.getName()) ? EnumChatFormatting.AQUA + "[FRIEND] " : PlayerUtils.isTeamMate((EntityPlayer) entity) ? EnumChatFormatting.GREEN + "[TEAM] " : ModuleManager.getInstance(KillAura.class).getTarget() == entity ? EnumChatFormatting.RED + "[TARGET] " : "") + EnumChatFormatting.RESET + formattedName + EnumChatFormatting.GRAY + distance;
            final float rectLength = Math.abs(-(getStringWidth(content) / 2) - 3 - (getStringWidth(content) / 2 + 4)), maxHealth = (int) (this.entity.getMaxHealth() + this.entity.getAbsorptionAmount()), amplifier = 100 / maxHealth, percent = (int) ((this.entity.getHealth() + this.entity.getAbsorptionAmount()) * amplifier), space = rectLength / 100; // @on
            int n = options.isSelected(Option.Health) && healthType.get() == HealthType.Value ? 5 : 0;
            final float contentWidth = getStringWidth(content) / 2F;

            /*final ScaledResolution resolution = new ScaledResolution(mc);

            float width = resolution.getScaledWidth() / 2F;
            float height = resolution.getScaledHeight() / 2F;

            float sizePerPixelX = Wrapper.getSFBold20Font().getWidth(content) / 2F * 0.5F;
            float sizePerPixelY = 15 * 0.45F;

            float xBnd1 = width / amp + sizePerPixelX;
            float xBnd2 = width / amp - sizePerPixelX;
            float yBnd1 = height / amp - sizePerPixelY;
            float yBnd2 = height / amp + sizePerPixelY;

            if (mc.gameSettings.thirdPersonView == 0 && this.positions[0] >= xBnd2 * 2 && this.positions[0] <= xBnd1 * 2 && this.positions[1] >= yBnd1 * 2 && this.positions[1] <= yBnd2 * 2) {
                SFBOLD_20.drawString("Middle click to teleport!", -(SFBOLD_20.stringWidth("Middle click to teleport") / 2F), -getYOffset() - 18, 0xffffffff, true);

                if (Mouse.isButtonDown(2)) {
                    if (tpTimer.delay(1000)) {
                        String command = mc.isSingleplayer() ? "/tp" : ".tp";
                        mc.thePlayer.sendChatMessage(command + " " + entity.getName());
                        tpTimer.reset();
                    }
                }
            }*/

            if (options.isSelected(Option.Background)) {
                RenderingUtils.drawRect(-contentWidth - 2 - n, -8.0F - getYOffset(), getStringWidth(content + health) / 2F + //
                                (healthType.get() == HealthType.Bar ? 2 : getStringWidth(health) - 3 - n), //
                        (options.isSelected(Option.Health) ? healthType.get() == HealthType.Bar ? 6 : 5 : 5) - getYOffset(), //
                        new Color(ModuleManager.getInstance(KillAura.class).getTarget() == entity ? 100 : 0, Tifality.getInstance().getFriendManager().isFriend(entity.getName()) || PlayerUtils.isTeamMate((EntityPlayer) entity) ? 90 : 0,
                                Tifality.getInstance().getFriendManager().isFriend(entity.getName()) ? 120 : //
                                PlayerUtils.isTeamMate((EntityPlayer) entity) ? 15 : 0, alpha.get().intValue()).getRGB());
            }

            if (options.isSelected(Option.Armor)) {
                renderArmor((EntityPlayer) this.entity);
            }


            drawString(content, -contentWidth - n, -getYOffset() - 5, 16777215);
            drawString(health, contentWidth - n, -getYOffset() - 5, getHealthColor());

            if (options.isSelected(Option.Health) && healthType.get() == HealthType.Bar) {
                Gui.drawRect(-contentWidth - 2, 5 - getYOffset(), -contentWidth - 5 + percent * space, 6 - getYOffset(), getHealthColor());
            }

            GlStateManager.enableDepth();
            GL11.glPopMatrix();
        }

        private void drawString(String string, float x, float y, int color) {
            if (tagFont.get() == TagFont.SFBold) {
                Wrapper.getSFBold20Font().drawString(string, x, y, color);
            } else {
                mc.fontRendererObj.drawString(string, x, y, color);
            }
        }

        private float getStringWidth(String string) {
            if (tagFont.get() == TagFont.SFBold) {
                return Wrapper.getSFBold20Font().getWidth(string);
            } else {
                return mc.fontRendererObj.getStringWidth(string);
            }
        }

        private float getYOffset() {
            final float distanceToEntity = mc.thePlayer.getDistanceToEntity(this.entity);

            if (healthType.get() == HealthType.Bar) {
                return (float) Math.max(getDistance() * (distanceToEntity >= 110 ? 0.058 : 0.032 + 4 / distanceToEntity), 1);
            } else {
                return (float) Math.max(getDistance() * (distanceToEntity >= 110 ? 0.046 : 0.02 + 4 / distanceToEntity), 1);
            }
        }

        private int getHealthColor() {
            final float f = this.entity.getHealth(), // @off
                    f1 = this.entity.getMaxHealth(),
                    f2 = Math.max(0.0F, Math.min(f, f1) / f1); // @on
            return Color.HSBtoRGB(f2 / 3.0F, 1, 1) | 0xFF000000;
        }

        private int getDistance() {
            final int diffX = (int) Math.abs(mc.thePlayer.posX - this.entity.posX), // @off
                    diffY = (int) Math.abs(mc.thePlayer.posY - this.entity.posY),
                    diffZ = (int) Math.abs(mc.thePlayer.posZ - this.entity.posZ); // @on
            return (int) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        }

        private double[] convertTo2D(double x, double y, double z) {
            final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
            final IntBuffer viewport = BufferUtils.createIntBuffer(16);
            final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
            final FloatBuffer projection = BufferUtils.createFloatBuffer(16);

            GL11.glGetFloat(2982, modelView);
            GL11.glGetFloat(2983, projection);
            GL11.glGetInteger(2978, viewport);

            final boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);
            return result ? new double[]{(double) screenCoords.get(0), (double) ((float) Display.getHeight() - screenCoords.get(1)), (double) screenCoords.get(2)} : null;
        }

        private void renderArmor(EntityPlayer player) {
            ItemStack[] renderStack = player.inventory.armorInventory;
            ItemStack armourStack;
            int xOffset = 0;

            for (ItemStack aRenderStack : renderStack) {
                armourStack = aRenderStack;

                if (armourStack != null) xOffset -= 8;
            }

            if (player.getCurrentEquippedItem() != null) {
                xOffset -= 8;

                final ItemStack stock = player.getCurrentEquippedItem().copy();

                if (stock.hasEffect() && (stock.getItem() instanceof ItemTool || stock.getItem() instanceof ItemArmor))
                    stock.stackSize = 1;

                renderItemStack(stock, xOffset, -25 - getYOffset() * 1.5f);
                xOffset += 16;
            }

            renderStack = player.inventory.armorInventory;

            for (int index = 3; index >= 0; index--) {
                armourStack = renderStack[index];

                if (armourStack != null) {
                    renderItemStack(armourStack, xOffset, -25 - getYOffset() * 1.5f);
                    xOffset += 16;
                }
            }

            GlStateManager.color(1, 1, 1, 1);
        }

        private void renderItemStack(final ItemStack stack, int x, float y) {
            GlStateManager.pushMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.clear(256);
            RenderHelper.enableStandardItemLighting();

            mc.getRenderItem().zLevel = -150.0F;

            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(0.7, 0.7, 0.7);
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
            mc.getRenderItem().renderItemOverlaysCR(Wrapper.getSFBold18Font(), stack, x, (int) y);
            mc.getRenderItem().zLevel = 0.0f;

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableCull();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();
            final float s = 0.5F;
            GlStateManager.scale(s, s, s);
            GlStateManager.disableDepth();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.popMatrix();
        }

    }

    private enum TagFont {
        SFBold,
        Vanilla;
    }

    private enum Option {
        Background,
        Distance,
        Health,
        Armor;
    }

    private enum HealthType {
        Bar,
        Value;
    }
}
