package net.minecraft.client.gui;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;
import club.tifality.gui.altmanager.GuiAltManager;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.RenderingUtils;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import club.tifality.utils.render.SkeetButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
    private static final AtomicInteger field_175373_f = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private float updateCounter;
    private String splashText = "missingno";
    private GuiButton buttonResetDemo;
    private int panoramaTimer;
    private DynamicTexture viewportTexture;
    private boolean field_175375_v = true;
    private final Object threadLock = new Object();
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private ResourceLocation backgroundTexture;
    private final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation("tifality/skeetchainmail.png");

    @Override
    public void updateScreen() {
        ++this.panoramaTimer;
    }

    public GuiMainMenu() {
        this.openGLWarning2 = field_96138_a;
        BufferedReader bufferedreader = null;
        try {
            String s;
            ArrayList<String> list = Lists.newArrayList();
            bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));
            while ((s = bufferedreader.readLine()) != null) {
                if ((s = s.trim()).isEmpty()) continue;
                list.add(s);
            }
            if (!list.isEmpty()) {
                do {
                    this.splashText = list.get(RANDOM.nextInt(list.size()));
                } while (this.splashText.hashCode() == 125780783);
            }
        } catch (IOException iOException) {
        } finally {
            if (bufferedreader != null) {
                try {
                    bufferedreader.close();
                }
                catch (IOException iOException) {}
            }
        }
        this.updateCounter = RANDOM.nextFloat();
        this.openGLWarning1 = "";
        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            this.openGLWarning1 = I18n.format("title.oldgl1", new Object[0]);
            this.openGLWarning2 = I18n.format("title.oldgl2", new Object[0]);
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            this.splashText = "Merry X-mas!";
        } else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            this.splashText = "Happy new year!";
        } else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            this.splashText = "OOoooOOOoooo! Spooky!";
        }
        int i = 24;
        int j = this.height / 4 + 48;
        if (this.mc.isDemo()) {
            this.addDemoButtons(j, 24);
        } else {
            this.addSingleplayerMultiplayerButtons(j, 24);
        }
        Object object = this.threadLock;
        synchronized (object) {
            this.field_92023_s = this.fontRendererObj.getStringWidth(this.openGLWarning1);
            this.field_92024_r = this.fontRendererObj.getStringWidth(this.openGLWarning2);
            int k = Math.max(this.field_92023_s, this.field_92024_r);
            this.field_92022_t = (this.width - k) / 2;
            this.field_92021_u = ((GuiButton)this.buttonList.get((int)0)).yPosition - 24;
            this.field_92020_v = this.field_92022_t + k;
            this.field_92019_w = this.field_92021_u + 24;
        }
        this.mc.func_181537_a(false);
    }

    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
        this.buttonList.add(new SkeetButton(1, this.width / 2 - 40, p_73969_1_ + p_73969_2_ - 25, I18n.format("menu.singleplayer")));
        this.buttonList.add(new SkeetButton(2, this.width / 2 - 40, p_73969_1_ + p_73969_2_ + 20 - 25, I18n.format("menu.multiplayer")));
        this.buttonList.add(new SkeetButton(14, this.width / 2 - 40, p_73969_1_ + p_73969_2_ + 40 - 25, I18n.format("Accounts")));
        this.buttonList.add(new SkeetButton(5, this.width / 2 - 40, p_73969_1_ + p_73969_2_ + 60 - 25, I18n.format("Language")));
        this.buttonList.add(new SkeetButton(0, this.width / 2 - 40, p_73969_1_ + p_73969_2_ + 80 - 25, I18n.format("menu.options")));
        this.buttonList.add(new SkeetButton(4, this.width / 2 - 40, p_73969_1_ + p_73969_2_ + 100 - 25, I18n.format("menu.quit")));
    }

    private void addDemoButtons(int p_73972_1_, int p_73972_2_) {
        this.buttonList.add(new GuiButton(11, this.width / 2 - 100, p_73972_1_, I18n.format("menu.playdemo")));
        this.buttonResetDemo = new GuiButton(12, this.width / 2 - 100, p_73972_1_ + p_73972_2_ * 1, I18n.format("menu.resetdemo"));
        this.buttonList.add(this.buttonResetDemo);
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");
        if (worldinfo == null) {
            this.buttonResetDemo.enabled = false;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        ISaveFormat isaveformat = this.mc.getSaveLoader();
        WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }
        if (button.id == 5) {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }
        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (button.id == 4) {
            this.mc.shutdown();
        }
        if (button.id == 11) {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
        }
        if (button.id == 12 && worldinfo != null) {
            GuiYesNo guiyesno = GuiSelectWorld.func_152129_a(this, worldinfo.getWorldName(), 12);
            this.mc.displayGuiScreen(guiyesno);
        }
        if (button.id == 14) {
            this.mc.displayGuiScreen(new GuiAltManager());
        }
    }

    @Override
    public void confirmClicked(boolean result2, int id) {
        if (result2 && id == 12) {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        } else if (id == 13) {
            if (result2) {
                try {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop").invoke(null);
                    oclass.getMethod("browse", URI.class).invoke(object, new URI(this.openGLWarningLink));
                }
                catch (Throwable throwable) {
                    logger.error("Couldn't open link", throwable);
                }
            }
            this.mc.displayGuiScreen(this);
        }
    }

    private void drawPanorama(int p_drawPanorama_1_, int p_drawPanorama_2_, float p_drawPanorama_3_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0f, 1.0f, 0.05f, 10.0f);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = 8;
        for (int j = 0; j < i * i; ++j) {
            GlStateManager.pushMatrix();
            float f = ((float)(j % i) / (float)i - 0.5f) / 64.0f;
            float f1 = ((float)(j / i) / (float)i - 0.5f) / 64.0f;
            float f2 = 0.0f;
            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin(((float)this.panoramaTimer + p_drawPanorama_3_) / 400.0f) * 25.0f + 20.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(-((float)this.panoramaTimer + p_drawPanorama_3_) * 0.1f, 0.0f, 1.0f, 0.0f);
            for (int k = 0; k < 6; ++k) {
                GlStateManager.pushMatrix();
                if (k == 1) {
                    GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
                }
                if (k == 2) {
                    GlStateManager.rotate(180.0f, 0.0f, 1.0f, 0.0f);
                }
                if (k == 3) {
                    GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
                }
                if (k == 4) {
                    GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
                }
                if (k == 5) {
                    GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
                }
                this.mc.getTextureManager().bindTexture(titlePanoramaPaths[k]);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int l = 255 / (j + 1);
                worldrenderer.pos(-1.0, -1.0, 1.0).tex(0.0, 0.0).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(1.0, -1.0, 1.0).tex(1.0, 0.0).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(1.0, 1.0, 1.0).tex(1.0, 1.0).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(-1.0, 1.0, 1.0).tex(0.0, 1.0).color(255, 255, 255, l).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }
        worldrenderer.setTranslation(0.0, 0.0, 0.0);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void rotateAndBlurSkybox(float p_rotateAndBlurSkybox_1_) {
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;
        for (int j = 0; j < i; ++j) {
            float f = 1.0f / (float)(j + 1);
            int k = this.width;
            int l = this.height;
            float f1 = (float)(j - i / 2) / 256.0f;
            worldrenderer.pos(k, l, this.zLevel).tex(0.0f + f1, 1.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            worldrenderer.pos(k, 0.0, this.zLevel).tex(1.0f + f1, 1.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            worldrenderer.pos(0.0, 0.0, this.zLevel).tex(1.0f + f1, 0.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
            worldrenderer.pos(0.0, l, this.zLevel).tex(0.0f + f1, 0.0).color(1.0f, 1.0f, 1.0f, f).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    private void renderSkybox(int p_renderSkybox_1_, int p_renderSkybox_2_, float p_renderSkybox_3_) {
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(p_renderSkybox_1_, p_renderSkybox_2_, p_renderSkybox_3_);
        this.rotateAndBlurSkybox(p_renderSkybox_3_);
        this.rotateAndBlurSkybox(p_renderSkybox_3_);
        this.rotateAndBlurSkybox(p_renderSkybox_3_);
        this.rotateAndBlurSkybox(p_renderSkybox_3_);
        this.rotateAndBlurSkybox(p_renderSkybox_3_);
        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        float f = this.width > this.height ? 120.0f / (float)this.width : 120.0f / (float)this.height;
        float f1 = (float)this.height * f / 256.0f;
        float f2 = (float)this.width * f / 256.0f;
        int i = this.width;
        int j = this.height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0, j, this.zLevel).tex(0.5f - f1, 0.5f + f2).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        worldrenderer.pos(i, j, this.zLevel).tex(0.5f - f1, 0.5f - f2).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        worldrenderer.pos(i, 0.0, this.zLevel).tex(0.5f + f1, 0.5f - f2).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        worldrenderer.pos(0.0, 0.0, this.zLevel).tex(0.5f + f1, 0.5f + f2).color(1.0f, 1.0f, 1.0f, 1.0f).endVertex();
        tessellator.draw();
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();
        drawGradientRect(0.0F, 0.0F, (float)this.width, (float)this.height, -2130706433, 16777215);
        drawGradientRect(0.0F, 0.0F, (float)this.width, (float)this.height, 0, Integer.MIN_VALUE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * (float) Math.PI * 2.0F) * 0.1F);
        f = f * 100.0F / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
        GlStateManager.scale(f, f, f);
        GlStateManager.popMatrix();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        RenderingUtils.rectangleBordered(sr.getScaledWidth() / 2.0f - 62 - 0.5D, this.height / 4.0f + 30 - 0.3D, sr.getScaledWidth() / 2.0f + 62 + 0.5D, this.height / 4.0f + 175 + 0.3D, 0.5D, Colors.getColor(60), Colors.getColor(10));
        RenderingUtils.rectangleBordered(sr.getScaledWidth() / 2.0f - 62 + 0.5D, this.height / 4.0f + 30 + 0.6D, sr.getScaledWidth() / 2.0f + 62 - 0.5D, this.height / 4.0f + 175 - 0.6D, 1.3D, Colors.getColor(60), Colors.getColor(40));
        RenderingUtils.rectangleBordered(sr.getScaledWidth() / 2.0f - 62 + 2.5D, this.height / 4.0f + 30 + 2.5D, sr.getScaledWidth() / 2.0f + 62 - 2.5D, this.height / 4.0f + 175 - 2.5D, 0.5D, Colors.getColor(22), Colors.getColor(12));

        GlStateManager.pushMatrix();
        GlStateManager.translate(sr.getScaledWidth() / 2.0f - 60, this.height / 4.0f + 30, 0.0F);
        GlStateManager.popMatrix();

        RenderingUtils.drawGradientRect(sr.getScaledWidth() / 2.0f - 62 + 3, this.height / 4.0f + 30 + 3, sr.getScaledWidth() / 2.0f, this.height / 4.0f + 30 + 4, true, Colors.getColor(55, 177, 218), Colors.getColor(204, 77, 198));
        RenderingUtils.drawGradientRect(sr.getScaledWidth() / 2.0f, this.height / 4.0f + 30 + 3, sr.getScaledWidth() / 2.0f + 62 - 3, this.height / 4.0f + 30 + 4, true, Colors.getColor(204, 77, 198), Colors.getColor(204, 227, 53));

        RenderingUtils.rectangle(sr.getScaledWidth() / 2.0f - 62 + 3, this.height / 4.0f + 30 + 3.5D, sr.getScaledWidth() / 2.0f + 62 - 3, this.height / 4.0f + 30 + 4, Colors.getColor(0, 110));
        RenderingUtils.rectangleBordered(sr.getScaledWidth() / 2.0f - 62 + 6, this.height / 4.0f + 30 + 8, sr.getScaledWidth() / 2.0f + 62 - 6.5D, this.height / 4.0f + 169, 0.3D, Colors.getColor(48), Colors.getColor(10));
        RenderingUtils.rectangle(sr.getScaledWidth() / 2.0f - 62 + 6 + 1, this.height / 4.0f + 30 + 9, sr.getScaledWidth() / 2.0f + 62 - 7.5D, this.height / 4.0f + 169 - 1, Colors.getColor(17));
        RenderingUtils.rectangle(sr.getScaledWidth() / 2.0f - 62 + 6 + 4.5F, this.height / 4.0f + 30 + 8, sr.getScaledWidth() / 2.0f - 62 + 35, this.height / 4.0f + 30 + 9, Colors.getColor(17));

        GlStateManager.pushMatrix();
        GlStateManager.translate(sr.getScaledWidth() / 2.0f - 62 + 6 + 5,this.height / 4.0f + 30 + 8, 0.0F);
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        mc.fontRendererObj.drawStringWithShadow("Main Menu", 0.0F, 0.0F, -1);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(4.0F, 4.0F, 4.0F);
        GlStateManager.popMatrix();
        if (this.openGLWarning1 != null && this.openGLWarning1.length() > 0) {
            drawRect(this.field_92022_t - 2, this.field_92021_u - 2, this.field_92020_v + 2, this.field_92019_w - 1, 0x55200000);
            this.drawString(this.fontRendererObj, this.openGLWarning1, this.field_92022_t, this.field_92021_u, -1);
            this.drawString(this.fontRendererObj, this.openGLWarning2, (this.width - this.field_92024_r) / 2, this.buttonList.get(0).yPosition - 12, -1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        synchronized (this.threadLock) {
            if (this.openGLWarning1.length() > 0 && mouseX >= this.field_92022_t && mouseX <= this.field_92020_v && mouseY >= this.field_92021_u && mouseY <= this.field_92019_w) {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                mc.displayGuiScreen(guiconfirmopenlink);
            }
        }
    }
}
