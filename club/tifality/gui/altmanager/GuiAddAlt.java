package club.tifality.gui.altmanager;

import club.tifality.Tifality;
import club.tifality.gui.altmanager.althening.api.AltService;
import club.tifality.gui.notification.client.NotificationPublisher;
import club.tifality.gui.notification.client.NotificationType;
import club.tifality.gui.notification.dev.DevNotifications;
import club.tifality.manager.config.Alts;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.realmsclient.gui.ChatFormatting;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;

public class GuiAddAlt extends GuiScreen {
    private final GuiAltManager manager;
    private PasswordField password;
    private String status = ChatFormatting.GRAY + "Idle...";
    private GuiTextField username;
    private ResourceLocation backgroundTexture;
    private String splashText;
    private DynamicTexture viewportTexture;
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    private int panoramaTimer;

    @Override
    public void updateScreen() {
        ++this.panoramaTimer;
    }

    GuiAddAlt(GuiAltManager manager) {
        this.manager = manager;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: {
                AddAltThread login = new AddAltThread(this.username.getText(), this.password.getText());
                login.start();
                break;
            }
            case 1: {
                this.mc.displayGuiScreen(this.manager);
                break;
            }
            case 2: {
                String data2;
                try {
                    data2 = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                } catch (Exception ex) {
                    return;
                }
                if (data2.contains(":")) {
                    String[] credentials = data2.split(":");
                    this.username.setText(credentials[0]);
                    this.password.setText(credentials[1]);
                }
            }
            case 1337: {
                String data1;
                try {
                    data1 = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                }
                catch (Exception var4) {
                    return;
                }
                ArrayList<String> list = new ArrayList<>(Arrays.asList(data1.replace("\r\n", "\n").split("\n")));
                for (String string : list) {
                    String user;
                    String pass;
                    if (string.contains("@alt.com") && !string.contains(":")) {
                        user = string;
                        pass = "ok";
                    } else {
                        user = string.split(":")[0];
                        pass = string.split(":")[1];
                    }
                    AltManager.registry.add(new Alt(user, pass, "Login Now", Alt.Status.Working));
                }
                this.mc.displayGuiScreen(this.manager);
            }
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
    public void drawScreen(int i, int j, float f) {
        GlStateManager.disableAlpha();
        this.renderSkybox(i, j, f);
        GlStateManager.enableAlpha();
        GuiAddAlt.drawGradientRect(0.0f, 0.0f, this.width, this.height, -2130706433, 0xFFFFFF);
        GuiAddAlt.drawGradientRect(0.0f, 0.0f, this.width, this.height, 0, Integer.MIN_VALUE);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2 + 90, 70.0f, 0.0f);
        GlStateManager.rotate(-20.0f, 0.0f, 0.0f, 1.0f);
        float fa = 1.8f - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0f * (float)Math.PI * 2.0f) * 0.1f);
        fa = fa * 100.0f / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
        GlStateManager.scale(fa, fa, fa);
        GlStateManager.popMatrix();
        MinecraftFontRenderer fonts = this.mc.fontRendererObj;
        this.username.drawTextBox();
        this.password.drawTextBox();
        this.drawCenteredString(this.mc.fontRendererObj, "Add Alt", this.width / 2, 20, -1);
        if (this.username.getText().isEmpty() && !this.username.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Username / E-Mail", this.width / 2 - 96, 66, -7829368);
        }
        if (this.password.getText().isEmpty() && !this.password.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Password", this.width / 2 - 96, 106, -7829368);
        }
        this.drawCenteredString(this.mc.fontRendererObj, this.status, this.width / 2, 30, -1);
        super.drawScreen(i, j, f);
    }

    @Override
    public void initGui() {
        this.viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", this.viewportTexture);
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 92 + 12, "Login"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 116 + 12, "Back"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 92 - 12, "Import user:pass"));
        this.buttonList.add(new GuiButton(1337, this.width / 2 - 100, this.height / 4 + 92 - 24 - 12, "Import ClipBoard"));
        this.username = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
        this.password = new PasswordField(20, this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.username.textboxKeyTyped(par1, par2);
        this.password.textboxKeyTyped(par1, par2);
        if (par1 == '\t' && (this.username.isFocused() || this.password.isFocused())) {
            this.username.setFocused(!this.username.isFocused());
            this.password.setFocused(!this.password.isFocused());
        }
        if (par1 == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.username.mouseClicked(par1, par2, par3);
        this.password.mouseClicked(par1, par2, par3);
    }

    private static void setStatus(GuiAddAlt guiAddAlt, String status) {
        guiAddAlt.status = status;
    }

    private class AddAltThread extends Thread {
        private final String password;
        private final String username;

        AddAltThread(String username, String password) {
            this.username = username;
            this.password = password;
            GuiAddAlt.setStatus(GuiAddAlt.this, ChatFormatting.GRAY + "Idle...");
        }

        private void checkAndAddAlt(String username, String password) {
            try {
                GuiAltManager.altService.switchService(username.contains("@alt.com") ? AltService.EnumAltService.TheAltening : AltService.EnumAltService.Mojang);
                YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
                YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication)service.createUserAuthentication(Agent.MINECRAFT);
                auth.setUsername(username);
                auth.setPassword(password);
                try {
                    auth.logIn();
                    AltManager.registry.add(new Alt(username, password, auth.getSelectedProfile().getName(), Alt.Status.Working));
                    try {
                        Tifality.getInstance().getConfigManager().getFile(Alts.class).saveFile();
                    } catch (Exception exception) {
                        // empty catch block
                    }
                    GuiAddAlt.setStatus(GuiAddAlt.this, "Alt added. (" + username + ")");
                    NotificationPublisher.queue("Alt Manager", "Alt added. (" + username + ")", NotificationType.OKAY, 3000);
                    DevNotifications.getManager().post("Alt added. (" + username + ")");
                } catch (AuthenticationException ex) {
                    GuiAddAlt.setStatus(GuiAddAlt.this, ChatFormatting.RED + "Alt failed!");
                    NotificationPublisher.queue("Alt Manager", "Alt failed!", NotificationType.WARNING, 3000);
                    DevNotifications.getManager().post("Alt failed!");
                    ex.printStackTrace();
                }
            } catch (Throwable e) {
                GuiAddAlt.setStatus(GuiAddAlt.this, ChatFormatting.RED + "Error :(");
                NotificationPublisher.queue("Alt Manager", "Error!", NotificationType.WARNING, 3000);
                DevNotifications.getManager().post("Error!");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            if (this.password.equals("")) {
                AltManager.registry.add(new Alt(this.username, ""));
                GuiAddAlt.setStatus(GuiAddAlt.this, (Object)((Object)ChatFormatting.GREEN) + "Alt added. (" + this.username + " - offline name)");
                NotificationPublisher.queue("Alt Manager", "Alt added. (" + this.username + " - offline name)", NotificationType.OKAY, 3000);
                DevNotifications.getManager().post("Alt added. (" + this.username + " - offline name)");
            } else {
                GuiAddAlt.setStatus(GuiAddAlt.this, (Object)((Object)ChatFormatting.AQUA) + "Trying alt...");
                NotificationPublisher.queue("Alt Manager", "Trying alt...", NotificationType.INFO, 3000);
                DevNotifications.getManager().post("Trying alt...");
                this.checkAndAddAlt(this.username, this.password);
            }
        }
    }
}

