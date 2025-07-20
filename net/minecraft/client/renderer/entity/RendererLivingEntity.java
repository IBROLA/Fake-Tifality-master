package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.MinecraftFontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.src.Config;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.optifine.EmissiveTextures;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import club.tifality.Tifality;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.manager.event.impl.render.RenderNameTagEvent;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.module.impl.other.SilentView;
import club.tifality.module.impl.player.Scaffold;
import club.tifality.module.impl.render.Chams;
import club.tifality.module.impl.render.Hud;
import club.tifality.utils.render.RenderingUtils;

import java.nio.FloatBuffer;
import java.util.List;

public abstract class RendererLivingEntity<T extends EntityLivingBase> extends Render<T> {
    public static final boolean animateModelLiving = Boolean.getBoolean("animate.model.living");
    private static final Logger logger = LogManager.getLogger();
    private static final DynamicTexture field_177096_e = new DynamicTexture(16, 16);
    public static float NAME_TAG_RANGE = 64.0F;
    public static float NAME_TAG_RANGE_SNEAK = 32.0F;
    private static boolean unsetPolyOffset;

    static {
        int[] aint = field_177096_e.getTextureData();

        for (int i = 0; i < 256; ++i) {
            aint[i] = -1;
        }

        field_177096_e.updateDynamicTexture();
    }

    public ModelBase mainModel;
    public EntityLivingBase renderEntity;
    public float renderLimbSwing;
    public float renderLimbSwingAmount;
    public float renderAgeInTicks;
    public float renderHeadYaw;
    public float renderHeadPitch;
    public float renderScaleFactor;
    public float renderPartialTicks;
    protected FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);
    protected List<LayerRenderer<T>> layerRenderers = Lists.newArrayList();
    protected boolean renderOutlines = false;
    private boolean renderLayersPushMatrix;

    public RendererLivingEntity(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
        this.mainModel = modelBaseIn;
        this.shadowSize = shadowSizeIn;
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doe
     *
     * @param entityYaw The yaw rotation of the passed entity
     */
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Chams instance = Tifality.getInstance().getModuleManager().getModule(Chams.class);
        if (instance.isEnabled() && instance.getWallHake().get()) {
            GL11.glEnable(32823);
            GL11.glPolygonOffset(1.0f, -1000000.0f);
        }
        if (!Reflector.RenderLivingEvent_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Pre_Constructor, entity, this, x, y, z)) {
            if (RendererLivingEntity.animateModelLiving) {
                entity.limbSwingAmount = 1.0f;
            }
            GL11.glPushMatrix();
            GlStateManager.disableCull();
            this.mainModel.swingProgress = this.getSwingProgress(entity, partialTicks);
            this.mainModel.isRiding = entity.isRiding();
            if (Reflector.ForgeEntity_shouldRiderSit.exists()) {
                this.mainModel.isRiding = (entity.isRiding() && entity.ridingEntity != null && Reflector.callBoolean(entity.ridingEntity, Reflector.ForgeEntity_shouldRiderSit));
            }
            this.mainModel.isChild = entity.isChild();
            try {
                SilentView silentView = Tifality.getInstance().getModuleManager().getModule(SilentView.class);
                Scaffold scaffold = Tifality.getInstance().getModuleManager().getModule(Scaffold.class);
                EntityPlayerSP player = null;
                boolean showServerSideRotations = entity instanceof EntityPlayerSP && (player = (EntityPlayerSP)entity).currentEvent.isRotating();
                float headYaw;
                float bodyYaw;
                if (showServerSideRotations && silentView.isEnabled()) {
                    final UpdatePositionEvent event = player.currentEvent;
                    bodyYaw = RenderingUtils.interpolate(event.getPrevYaw(), event.getYaw(), partialTicks);
                    headYaw = RenderingUtils.interpolate(event.getPrevYaw(), event.getYaw(), partialTicks);
                } else {
                    bodyYaw = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                    headYaw = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                }
                float yawDif = headYaw - bodyYaw;
                if (this.mainModel.isRiding && entity.ridingEntity instanceof EntityLivingBase) {
                    EntityLivingBase entitylivingbase = (EntityLivingBase)entity.ridingEntity;
                    bodyYaw = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
                    yawDif = headYaw - bodyYaw;
                    float f3 = MathHelper.wrapAngleTo180_float(yawDif);
                    if (f3 < -85.0f) {
                        f3 = -85.0f;
                    }
                    if (f3 >= 85.0f) {
                        f3 = 85.0f;
                    }
                    bodyYaw = headYaw - f3;
                    if (f3 * f3 > 2500.0f) {
                        bodyYaw += f3 * 0.2f;
                    }
                    yawDif = headYaw - bodyYaw;
                }
                float pitch;
                if (showServerSideRotations && silentView.isEnabled()) {
                    final UpdatePositionEvent event = player.currentEvent;
                    pitch = RenderingUtils.interpolate(event.getPrevPitch(), event.getPitch(), partialTicks);
                } else {
                    pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                }
                this.renderLivingAt(entity, x, y, z);
                final float f4 = 0.0625f;
                float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
                float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0f - partialTicks);
                final float f7 = this.handleRotationFloat(entity, partialTicks);
                final float f8 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                final float f9 = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
                final float f10 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
                final float f11 = f10 - f9;
                final boolean flag = this.setDoRenderBrightness(entity, partialTicks);
                if (silentView.isEnabled() && SilentView.ghostSilentView.get() && (KillAura.getInstance().getTarget() != null || scaffold.isRotating()) && entity == Minecraft.getMinecraft().thePlayer) {
                    GlStateManager.pushMatrix();
                    this.rotateCorpse(entity, f8, this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), partialTicks);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.scale(-1.0f, -1.0f, 1.0f);
                    this.preRenderCallback(entity, partialTicks);
                    GlStateManager.translate(0.0f, -1.5078125f, 0.0f);
                    if (entity.isChild()) {
                        f6 *= 3.0f;
                    }
                    if (f5 > 1.0f) {
                        f5 = 1.0f;
                    }
                    GlStateManager.enableAlpha();
                    this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
                    this.mainModel.setRotationAngles(f6, f5, f8, f11, f8, f4, entity);
                    if (CustomEntityModels.isActive()) {
                        this.renderEntity = entity;
                        this.renderLimbSwing = f6;
                        this.renderLimbSwingAmount = f5;
                        this.renderAgeInTicks = f7;
                        this.renderHeadYaw = f11;
                        this.renderHeadPitch = pitch;
                        this.renderScaleFactor = f4;
                        this.renderPartialTicks = partialTicks;
                    }
                    GlStateManager.pushMatrix();
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 0.3f);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.alphaFunc(516, 0.003921569f);
                    this.renderModel(entity, f6, f5, f8, f11, f8, f4);
                    GlStateManager.disableBlend();
                    GlStateManager.alphaFunc(516, 0.1f);
                    GlStateManager.popMatrix();
                    GlStateManager.depthMask(true);
                    if (flag) {
                        this.unsetBrightness();
                    }
                    GlStateManager.depthMask(true);
                    if (!((EntityPlayer)entity).isSpectator()) {
                        this.renderLayers(entity, f6, f5, partialTicks, f8, f11, f8, f4);
                    }
                    GlStateManager.popMatrix();
                }
                this.rotateCorpse(entity, f7, bodyYaw, partialTicks);
                GlStateManager.enableRescaleNormal();
                GL11.glScalef(-1.0f, -1.0f, 1.0f);
                this.preRenderCallback(entity, partialTicks);
                GL11.glTranslatef(0.0f, -1.5078125f, 0.0f);
                if (entity.isChild()) {
                    f6 *= 3.0f;
                }
                if (f5 > 1.0f) {
                    f5 = 1.0f;
                }
                GlStateManager.enableAlpha();
                this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
                this.mainModel.setRotationAngles(f6, f5, f7, yawDif, pitch, 0.0625f, entity);
                if (this.renderOutlines) {
                    this.renderModel(entity, f6, f5, f7, yawDif, pitch, 0.0625f);
                    final boolean flag2 = this.setScoreTeamColor(entity);
                    this.renderModel(entity, f6, f5, f7, yawDif, pitch, 0.0625f);
                    if (flag2) {
                        this.unsetScoreTeamColor();
                    }
                } else if (silentView.isEnabled() && SilentView.ghostSilentView.get() && (KillAura.getInstance().getTarget() != null || scaffold.isRotating()) && entity == Minecraft.getMinecraft().thePlayer) {
                    GL11.glPushMatrix();
                    GL11.glPushAttrib(1048575);
                    GL11.glDisable(2929);
                    GL11.glDisable(3553);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glDisable(2896);
                    GL11.glPolygonMode(1032, 6914);
                    RenderingUtils.glColor(SilentView.color.get());
                    this.renderModel(entity, f6, f5, f7, yawDif, pitch, f4);
                    GL11.glEnable(2896);
                    GL11.glDisable(3042);
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glColor3d(1.0, 1.0, 1.0);
                    GL11.glPopAttrib();
                    GL11.glPopMatrix();
                } else {
                    this.renderModel(entity, f6, f5, f7, yawDif, pitch, f4);
                    if (flag) {
                        this.unsetBrightness();
                    }
                    GlStateManager.depthMask(true);
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator()) {
                        this.renderLayers(entity, f6, f5, partialTicks, f7, yawDif, pitch, 0.0625f);
                    }
                }
                if (CustomEntityModels.isActive()) {
                    this.renderEntity = null;
                }
                GlStateManager.disableRescaleNormal();
            } catch (Exception exception) {
                logger.error("Couldn't render entity", exception);
            }
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GL11.glPopMatrix();
            if (!this.renderOutlines) {
                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }
            if (Reflector.RenderLivingEvent_Post_Constructor.exists()) {
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Post_Constructor, entity, this, x, y, z);
            }
        }
        if (instance.isEnabled() && instance.getWallHake().get()) {
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    }

    public void renderName(T entity, double x, double y, double z) {
        if (!Reflector.RenderLivingEvent_Specials_Pre_Constructor.exists() || !Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Specials_Pre_Constructor, entity, this, x, y, z)) {
            if (this.canRenderName(entity)) {
                RenderNameTagEvent event = new RenderNameTagEvent(entity);
                Tifality.getInstance().getEventBus().post(event);
                if (!event.isCancelled()) {
                    double d0 = entity.getDistanceSqToEntity(this.renderManager.livingPlayer);
                    float f = entity.isSneaking() ? NAME_TAG_RANGE_SNEAK : NAME_TAG_RANGE;

                    if (d0 < (double) (f * f)) {
                        String s = entity.getDisplayName().getFormattedText();
                        float f1 = 0.02666667F;
                        GlStateManager.alphaFunc(516, 0.1F);

                        if (entity.isSneaking()) {
                            MinecraftFontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                            GL11.glPushMatrix();
                            GL11.glTranslatef((float) x, (float) y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), (float) z);
                            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                            GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                            GL11.glRotatef(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                            GL11.glScalef(-0.02666667F, -0.02666667F, 0.02666667F);
                            GL11.glTranslatef(0.0F, 9.374999F, 0.0F);
                            GlStateManager.disableLighting();
                            GlStateManager.depthMask(false);
                            GlStateManager.enableBlend();
                            GlStateManager.disableTexture2D();
                            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                            int i = fontrenderer.getStringWidth(s) / 2;
                            Tessellator tessellator = Tessellator.getInstance();
                            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                            worldrenderer.pos(-i - 1, -1.0D, 0.0D).color4f(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(-i - 1, 8.0D, 0.0D).color4f(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(i + 1, 8.0D, 0.0D).color4f(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(i + 1, -1.0D, 0.0D).color4f(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            tessellator.draw();
                            GlStateManager.enableTexture2D();
                            GlStateManager.depthMask(true);
                            fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2.0f, 0, 553648127);
                            GlStateManager.enableLighting();
                            GlStateManager.disableBlend();
                            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                            GL11.glPopMatrix();
                        } else {
                            this.renderOffsetLivingLabel(entity, x, y - (entity.isChild() ? (double) (entity.height / 2.0F) : 0.0D), z, s, 0.02666667F, d0);
                        }
                    }
                }
            }

            if (Reflector.RenderLivingEvent_Specials_Post_Constructor.exists()) {
                Reflector.postForgeBusEvent(Reflector.RenderLivingEvent_Specials_Post_Constructor, entity, this, Double.valueOf(x), Double.valueOf(y), Double.valueOf(z));
            }
        }
    }

    protected boolean canRenderName(T entity) {
        EntityPlayerSP entityplayersp = Minecraft.getMinecraft().thePlayer;

        if (entity instanceof EntityPlayer && entity != entityplayersp) {
            Team team = entity.getTeam();
            Team team1 = entityplayersp.getTeam();

            if (team != null) {
                Team.EnumVisible team$enumvisible = team.getNameTagVisibility();

                switch (team$enumvisible) {
                    case ALWAYS:
                        return true;

                    case NEVER:
                        return false;

                    case HIDE_FOR_OTHER_TEAMS:
                        return team1 == null || team.isSameTeam(team1);

                    case HIDE_FOR_OWN_TEAM:
                        return team1 == null || !team.isSameTeam(team1);

                    default:
                        return true;
                }
            }
        }

        return Minecraft.isGuiEnabled() && entity != this.renderManager.livingPlayer && !entity.isInvisibleToPlayer(entityplayersp) && entity.riddenByEntity == null;
    }

    public <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean addLayer(U layer) {
        return this.layerRenderers.add((LayerRenderer<T>) layer);
    }

    protected <V extends EntityLivingBase, U extends LayerRenderer<V>> boolean removeLayer(U layer) {
        return this.layerRenderers.remove(layer);
    }

    public ModelBase getMainModel() {
        return this.mainModel;
    }

    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    protected float interpolateRotation(float par1, float par2, float par3) {
        float f = par2 - par1;

        while (f < -180.0F) {
            f += 360.0F;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return par1 + par3 * f;
    }

    public void transformHeldFull3DItemLayer() {
    }

    protected boolean setScoreTeamColor(T entityLivingBaseIn) {
        int i = 16777215;

        if (entityLivingBaseIn instanceof EntityPlayer) {
            ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) entityLivingBaseIn.getTeam();

            if (scoreplayerteam != null) {
                String s = MinecraftFontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());

                if (s.length() >= 2) {
                    i = this.getFontRendererFromRenderManager().getColorCode(s.charAt(1));
                }
            }
        }

        float f1 = (float) (i >> 16 & 255) / 255.0F;
        float f2 = (float) (i >> 8 & 255) / 255.0F;
        float f = (float) (i & 255) / 255.0F;
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glColor4f(f1, f2, f, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void unsetScoreTeamColor() {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    /**
     * Renders the model in RenderLiving
     */
    protected void renderModel(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        boolean flag = !entitylivingbaseIn.isInvisible();
        boolean flag1 = !flag && !entitylivingbaseIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
        Hud hud = Tifality.getInstance().getModuleManager().getModule(Hud.class);
        Chams chams = Tifality.getInstance().getModuleManager().getModule(Chams.class);
        if (flag || flag1) {
            if (!this.bindEntityTexture(entitylivingbaseIn)) {
                return;
            }
            if (flag1) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 0.15f);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.alphaFunc(516, 0.003921569f);
            }
            if (chams.isEnabled() && (chams.getModeValue().isSelected(Chams.Mode.COLOR) || chams.getModeValue().isSelected(Chams.Mode.CSGO))) {
                GL11.glPushAttrib(1048575);
                GL11.glDisable(3008);
                GL11.glDisable(3553);
                if (!chams.getModeValue().isSelected(Chams.Mode.CSGO)) {
                    GL11.glDisable(2896);
                }
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glLineWidth(1.5f);
                GL11.glEnable(2960);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glEnable(10754);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
                RenderingUtils.glColor(chams.getRainbow().get() != false ? RenderingUtils.getRainbowFromEntity(((Double)hud.rainbowSpeed.get()).intValue(), ((Double)hud.rainbowWidth.get()).intValue(), (int)System.currentTimeMillis() / 15, false, ((Double)chams.getRainbowAlphaValue().get()).floatValue()) : chams.getInvisibleColorValue().get());
                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                RenderingUtils.glColor(chams.getRainbow().get() != false ? RenderingUtils.getRainbowFromEntity(((Double)hud.rainbowSpeed.get()).intValue(), ((Double)hud.rainbowWidth.get()).intValue(), (int)System.currentTimeMillis() / 15, false, ((Double)chams.getRainbowAlphaValue().get()).floatValue()) : chams.getVisibleColorValue().get());
                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
                GL11.glEnable(3042);
                if (!chams.getModeValue().isSelected(Chams.Mode.CSGO)) {
                    GL11.glEnable(2896);
                }
                GL11.glEnable(3553);
                GL11.glEnable(3008);
                GL11.glPopAttrib();
            } else {
                this.mainModel.render(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, p_77036_7_);
            }
            if (flag1) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1f);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }
    }

    protected boolean setDoRenderBrightness(T entityLivingBaseIn, float partialTicks) {
        return this.setBrightness(entityLivingBaseIn, partialTicks, true);
    }

    protected boolean setBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures) {
        float f = entitylivingbaseIn.getBrightness(partialTicks);
        int i = this.getColorMultiplier(entitylivingbaseIn, f, partialTicks);
        boolean flag = (i >> 24 & 0xFF) > 0;
        boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;
        if (!flag && !flag1) {
            return false;
        }
        if (!flag && !combineTextures) {
            return false;
        }
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        this.brightnessBuffer.position(0);
        if (flag1) {
            this.brightnessBuffer.put(1.0f);
            this.brightnessBuffer.put(0.0f);
            this.brightnessBuffer.put(0.0f);
            this.brightnessBuffer.put(0.3f);
        } else {
            float f1 = (float)(i >> 24 & 0xFF) / 255.0f;
            float f2 = (float)(i >> 16 & 0xFF) / 255.0f;
            float f3 = (float)(i >> 8 & 0xFF) / 255.0f;
            float f4 = (float)(i & 0xFF) / 255.0f;
            this.brightnessBuffer.put(f2);
            this.brightnessBuffer.put(f3);
            this.brightnessBuffer.put(f4);
            this.brightnessBuffer.put(1.0f - f1);
        }
        this.brightnessBuffer.flip();
        GL11.glTexEnv(8960, 8705, this.brightnessBuffer);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(field_177096_e.getGlTextureId());
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void unsetBrightness() {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.disableTexture2D();
        GlStateManager.bindTexture(0);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders()) {
            Shaders.setEntityColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(T entityLivingBaseIn, double x, double y, double z) {
        GL11.glTranslatef((float) x, (float) y, (float) z);
    }

    protected void rotateCorpse(T bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        GL11.glRotatef(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);

        if (bat.deathTime > 0) {
            float f = ((float) bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt_float(f);

            if (f > 1.0F) {
                f = 1.0F;
            }

            GL11.glRotatef(f * this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
        } else {
            String s = EnumChatFormatting.getTextWithoutFormattingCodes(bat.getCommandSenderName());

            if (s != null && (s.equals("Dinnerbone") || s.equals("Grumm")) && (!(bat instanceof EntityPlayer) || ((EntityPlayer) bat).isWearing(EnumPlayerModelParts.CAPE))) {
                GL11.glTranslatef(0.0F, bat.height + 0.1F, 0.0F);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    /**
     * Returns where in the swing animation the living entity is (from 0 to 1).  Args : entity, partialTickTime
     */
    protected float getSwingProgress(T livingBase, float partialTickTime) {
        return livingBase.getSwingProgress(partialTickTime);
    }

    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float handleRotationFloat(T livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }

    protected void renderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_) {
        for (LayerRenderer<T> layerrenderer : this.layerRenderers) {
            boolean flag = this.setBrightness(entitylivingbaseIn, partialTicks, layerrenderer.shouldCombineTextures());

            if (EmissiveTextures.isActive()) {
                EmissiveTextures.beginRender();
            }

            if (this.renderLayersPushMatrix) {
                GL11.glPushMatrix();
            }

            layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);

            if (this.renderLayersPushMatrix) {
                GL11.glPopMatrix();
            }

            if (EmissiveTextures.isActive()) {
                if (EmissiveTextures.hasEmissive()) {
                    this.renderLayersPushMatrix = true;
                    EmissiveTextures.beginRenderEmissive();
                    GL11.glPushMatrix();
                    layerrenderer.doRenderLayer(entitylivingbaseIn, p_177093_2_, p_177093_3_, partialTicks, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_);
                    GL11.glPopMatrix();
                    EmissiveTextures.endRenderEmissive();
                }

                EmissiveTextures.endRender();
            }

            if (flag) {
                this.unsetBrightness();
            }
        }
    }

    protected float getDeathMaxRotation(T entityLivingBaseIn) {
        return 90.0F;
    }

    /**
     * Returns an ARGB int color back. Args: entityLiving, lightBrightness, partialTickTime
     */
    protected int getColorMultiplier(T entitylivingbaseIn, float lightBrightness, float partialTickTime) {
        return 0;
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(T entitylivingbaseIn, float partialTickTime) {
    }

    public void setRenderOutlines(boolean renderOutlinesIn) {
        this.renderOutlines = renderOutlinesIn;
    }

    public List<LayerRenderer<T>> getLayerRenderers() {
        return this.layerRenderers;
    }
}
