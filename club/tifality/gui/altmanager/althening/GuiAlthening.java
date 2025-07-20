package club.tifality.gui.altmanager.althening;

import club.tifality.Tifality;
import club.tifality.gui.altmanager.althening.api.AltService;
import club.tifality.gui.altmanager.althening.api.api.TheAltening;
import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.MinecraftFontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import club.tifality.gui.altmanager.GuiAltManager;
import club.tifality.gui.altmanager.PasswordField;

import java.awt.*;
import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class GuiAlthening extends GuiScreen {
    private final GuiAltManager manager;
    public static String rank1 = "none";
    public static String rank2 = "none";
    public static int level1 = 0;
    public static int level2 = 0;
    private PasswordField apikeyField;
    private GuiTextField tokenField;
    public String status = "";
    private int panoramaTimer;
    private ResourceLocation backgroundTexture;
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    private String splashText;
    private DynamicTexture viewportTexture;

    public GuiAlthening(GuiScreen manager) {
        this.manager = (GuiAltManager)manager;
    }

    @Override
    public void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        switch (button.id) {
            case 0: {
                this.mc.displayGuiScreen(this.manager);
                break;
            }
            case 1: {
                String api = this.apikeyField.getText();
                TheAltening theAltening = new TheAltening(api.contains("api") ? api : "");
                Tifality.setAPI(api.contains("api") ? api : "");
                try {
                    this.status = "\u00a7cLogging in...";
                    GuiAltManager.altService.switchService(AltService.EnumAltService.TheAltening);
                    YggdrasilUserAuthentication authentication = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString()), Agent.MINECRAFT);
                    authentication.setUsername(theAltening.getAccountData().getToken());
                    authentication.setPassword("Tifality");
                    authentication.logIn();
                    this.mc.session = new Session(authentication.getSelectedProfile().getName(), authentication.getSelectedProfile().getId().toString(), authentication.getAuthenticatedToken(), "legacy");
                    rank1 = theAltening.getAccountData().getInfo().getHypixelRank();
                    level1 = theAltening.getAccountData().getInfo().getHypixelLevel();
                    rank2 = theAltening.getAccountData().getInfo().getMineplexRank();
                    level2 = theAltening.getAccountData().getInfo().getMineplexLevel();
                    this.manager.status = "\u00a7fYour name is now \u00a7a" + authentication.getSelectedProfile().getName() + "\u00a7f.";
                    this.mc.displayGuiScreen(this.manager);
                } catch (Throwable e) {
                    this.status = "\u00a7cFailed Login";
                    e.printStackTrace();
                }
                break;
            }
            case 2: {
                try {
                    GuiAltManager.altService.switchService(AltService.EnumAltService.TheAltening);
                    this.status = "\u00a7cLogging in...";
                    YggdrasilUserAuthentication yggdrasilUserAuthentication = new YggdrasilUserAuthentication(new YggdrasilAuthenticationService(Proxy.NO_PROXY, ""), Agent.MINECRAFT);
                    yggdrasilUserAuthentication.setUsername(this.tokenField.getText());
                    yggdrasilUserAuthentication.setPassword("Existent");
                    yggdrasilUserAuthentication.logIn();
                    this.mc.session = new Session(yggdrasilUserAuthentication.getSelectedProfile().getName(), yggdrasilUserAuthentication.getSelectedProfile().getId().toString(), yggdrasilUserAuthentication.getAuthenticatedToken(), "mojang");
                    this.manager.status = "\u00a7fYour name is now \u00a7a" + yggdrasilUserAuthentication.getSelectedProfile().getName() + "f.";
                    this.mc.displayGuiScreen(this.manager);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                    this.status = "\u00a7cThat Token cannot be used.";
                }
                break;
            }
            case 3: {
                GuiAlthening.showURL("https://thealtening.com/");
            }
        }
        super.actionPerformed(button);
    }

    public static void showURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        }
        catch (IOException | URISyntaxException e) {
            e.printStackTrace();
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
                float f3 = 0.0f;
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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();
        GuiAlthening.drawGradientRect(0.0f, 0.0f, this.width, this.height, -2130706433, 0xFFFFFF);
        GuiAlthening.drawGradientRect(0.0f, 0.0f, this.width, this.height, 0, Integer.MIN_VALUE);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2 + 90, 70.0f, 0.0f);
        GlStateManager.rotate(-20.0f, 0.0f, 0.0f, 1.0f);
        float f1 = 1.8f - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0f * (float)Math.PI * 2.0f) * 0.1f);
        f1 = f1 * 100.0f / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
        GlStateManager.scale(f1, f1, f1);
        GlStateManager.popMatrix();
        this.drawCenteredString(this.mc.fontRendererObj, "TheAltening", this.width / 2, 6, 0xFFFFFF);
        this.drawCenteredString(this.mc.fontRendererObj, this.status, this.width / 2, 18, 0xFFFFFF);
        this.apikeyField.drawTextBox();
        this.tokenField.drawTextBox();
        if (this.apikeyField.getText().isEmpty() && !this.apikeyField.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Api-Key", this.width / 2 - 96, 156, -7829368);
        }
        if (this.tokenField.getText().isEmpty() && !this.tokenField.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Token", this.width / 2 - 96, 86, -7829368);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        MinecraftFontRenderer font = this.mc.fontRendererObj;
        Keyboard.enableRepeatEvents(true);
        GuiButton login = new GuiButton(2, this.width / 2 - 100, 105, "Login");
        this.buttonList.add(login);
        GuiButton generate = new GuiButton(1, this.width / 2 - 100, 175, "Generate");
        this.buttonList.add(generate);
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height - 83, "Buy"));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height - 60, "Back"));
        this.tokenField = new GuiTextField(666, font, this.width / 2 - 100, 80, 200, 20);
        this.tokenField.setMaxStringLength(Integer.MAX_VALUE);
        this.apikeyField = new PasswordField(20, font, this.width / 2 - 100, 150, 200, 20);
        this.apikeyField.setText(Tifality.getAPI() != null ? Tifality.getAPI() : "");
        this.apikeyField.setMaxStringLength(18);
        super.initGui();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (1 == keyCode) {
            this.mc.displayGuiScreen(this.manager);
            return;
        }
        if (this.apikeyField.isFocused()) {
            this.apikeyField.textboxKeyTyped(typedChar, keyCode);
        }
        if (this.tokenField.isFocused()) {
            this.tokenField.textboxKeyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.apikeyField.mouseClicked(mouseX, mouseY, mouseButton);
        this.tokenField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        this.apikeyField.updateCursorCounter();
        this.tokenField.updateCursorCounter();
        ++this.panoramaTimer;
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }
}

