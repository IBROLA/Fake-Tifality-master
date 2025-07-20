package club.tifality.gui.altmanager;

import club.tifality.Tifality;
import club.tifality.gui.altmanager.althening.GuiAlthening;
import club.tifality.gui.altmanager.althening.api.AltService;
import club.tifality.gui.notification.client.NotificationPublisher;
import club.tifality.gui.notification.client.NotificationType;
import club.tifality.gui.notification.dev.DevNotifications;
import club.tifality.manager.config.Alts;
import club.tifality.utils.render.Colors;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;
import club.tifality.utils.render.RenderingUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GuiAltManager extends GuiScreen {
    public static final AltService altService = new AltService();
    private GuiButton login;
    private GuiButton remove;
    private GuiButton rename;
    private AltLoginThread loginThread;
    private double offset;
    public Alt selectedAlt = null;
    public String status = ChatFormatting.GRAY + "Idle...";
    private GuiTextField seatchField;
    private int panoramaTimer;
    private ResourceLocation backgroundTexture;
    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
    private String splashText = "missingno";
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    private static final Random RANDOM = new Random();

    @Override
    public void updateScreen() {
        ++this.panoramaTimer;
    }

    public GuiAltManager() {
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
                } catch (IOException iOException) {}
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: {
                break;
            }
            case 1: {
                this.loginThread = new AltLoginThread(this.selectedAlt);
                this.loginThread.start();
                break;
            }
            case 2: {
                if (this.loginThread != null) {
                    this.loginThread = null;
                }
                AltManager.registry.remove(this.selectedAlt);
                this.status = "§aRemoved.";
                NotificationPublisher.queue("Alt Manager", "Removed Alt", NotificationType.WARNING, 3000);
                DevNotifications.getManager().post("Removed Alt");
                try {
                    Tifality.getInstance().getConfigManager().getFile(Alts.class).saveFile();
                } catch (Exception exception) {
                    // empty catch block
                }
                this.selectedAlt = null;
                break;
            }
            case 3: {
                this.mc.displayGuiScreen(new GuiAddAlt(this));
                break;
            }
            case 4: {
                this.mc.displayGuiScreen(new GuiAltLogin(this));
                break;
            }
            case 5: {
                ArrayList<Alt> registry = AltManager.registry;
                Random random = new Random();
                Alt randomAlt = registry.get(random.nextInt(AltManager.registry.size()));
                this.loginThread = new AltLoginThread(randomAlt);
                this.loginThread.start();
                break;
            }
            case 6: {
                this.mc.displayGuiScreen(new GuiRenameAlt(this));
                break;
            }
            case 7: {
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
            }
            case 8: {
                try {
                    Tifality.getInstance().getConfigManager().getFile(Alts.class).saveFile();
                } catch (Exception exception) {
                    // empty catch block
                }
                this.status = "§bReloaded!";
                NotificationPublisher.queue("Alt Manager", "Reloaded Alt-Manager !", NotificationType.OKAY, 3000);
                DevNotifications.getManager().post("Reloaded Altmanager");
                break;
            }
            case 1919: {
                this.mc.displayGuiScreen(new GuiAlthening(this));
                break;
            }
            case 4545: {
                this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, new ServerData(I18n.format("selectServer.defaultName"), "hypixel.net", false)));
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
    public void drawScreen(int par1, int par2, float par3) {
        GlStateManager.disableAlpha();
        this.renderSkybox(par1, par2, par3);
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
        if (Mouse.hasWheel()) {
            int wheel = Mouse.getDWheel();
            if (wheel < 0) {
                this.offset += 26.0;
                if (this.offset < 0.0) {
                    this.offset = 0.0;
                }
            } else if (wheel > 0) {
                this.offset -= 26.0;
                if (this.offset < 0.0) {
                    this.offset = 0.0;
                }
            }
        }
        RenderingUtils.rectangleBordered(
                45.0, 33.0, (double)(this.width - 45), (double)(this.height - 50), 1.0, Colors.getColor(5, 35), new Color(39, 39, 39).getRGB()
        );
        this.mc.fontRendererObj.drawStringWithShadow(this.mc.session.getUsername(), 10.0F, 15.0F, -1);
        StringBuilder sb = new StringBuilder("Account Manager - ");
        sb.append(AltManager.registry.size())
                .append(" alts")
                .append(" | Banned: ")
                .append(AltManager.registry.stream().filter(o -> o.getStatus().equals(Alt.Status.Banned)).collect(Collectors.toList()).size());
        this.drawCenteredString(this.mc.fontRendererObj, sb.toString(), this.width / 2, 10, -1);
        this.drawCenteredString(this.mc.fontRendererObj, this.loginThread == null ? this.status : this.loginThread.getStatus(), this.width / 2, 20, -1);
        GL11.glPushMatrix();
        this.prepareScissorBox(0.0F, 33.0F, (float)this.width, (float)(this.height - 50));
        GL11.glEnable(3089);
        int y = 38;
        int number = 0;
        for(Alt alt : this.getAlts()) {
            if (this.isAltInArea(y)) {
                ++number;
                String name;
                if (alt.getMask().equals("")) {
                    name = alt.getUsername();
                } else {
                    name = alt.getMask();
                }

                if (name.equalsIgnoreCase(this.mc.session.getUsername())) {
                    name = "§n" + name;
                }

                String prefix = alt.getStatus().equals(Alt.Status.Banned) ? "§c" : (alt.getStatus().equals(Alt.Status.NotWorking) ? "§m" : "");
                name = prefix + name + "§r §7| " + alt.getStatus().toFormatted();
                String pass;
                if (alt.getPassword().equals("")) {
                    pass = "§cCracked";
                } else {
                    pass = alt.getPassword().replaceAll(".", "*");
                }

                if (alt != this.selectedAlt) {
                    if (this.isMouseOverAlt(par1, par2, (double)y - this.offset) && Mouse.isButtonDown(0)) {
                        RenderingUtils.rectangleBordered(
                                52.0,
                                (double)y - this.offset - 4.0,
                                this.width - 52,
                                (double)y - this.offset + 20.0,
                                1.0,
                                -Colors.getColor(145, 50),
                                -2146101995
                        );
                    } else if (this.isMouseOverAlt(par1, par2, (double)y - this.offset)) {
                        RenderingUtils.rectangleBordered(
                                52.0,
                                (double)y - this.offset - 4.0,
                                this.width - 52,
                                (double)y - this.offset + 20.0,
                                1.0,
                                Colors.getColor(145, 50),
                                -2145180893
                        );
                    }
                } else {
                    if (this.isMouseOverAlt(par1, par2, (double)y - this.offset) && Mouse.isButtonDown(0)) {
                        RenderingUtils.rectangleBordered(
                                52.0,
                                (double)y - this.offset - 4.0,
                                this.width - 77,
                                (double)y - this.offset + 20.0,
                                1.0,
                                Colors.getColor(145, 50),
                                -2142943931
                        );
                    } else if (this.isMouseOverAlt(par1, par2, (double)y - this.offset)) {
                        RenderingUtils.rectangleBordered(
                                52.0,
                                (double)y - this.offset - 4.0,
                                this.width - 77,
                                (double)y - this.offset + 20.0,
                                1.0,
                                Colors.getColor(145, 50),
                                -2142088622
                        );
                    } else {
                        RenderingUtils.rectangleBordered(
                                52.0,
                                (double)y - this.offset - 4.0,
                                (double)(this.width - 77),
                                (double)y - this.offset + 20.0,
                                1.0,
                                Colors.getColor(145, 50),
                                -2144259791
                        );
                    }

                    boolean hovering = par1 >= this.width - 76
                            && par1 <= this.width - 52
                            && (double)par2 >= (double)y - this.offset - 4.0
                            && (double)par2 <= (double)y - this.offset + 20.0;
                    RenderingUtils.rectangleBordered(
                            this.width - 76,
                            (double)y - this.offset - 4.0,
                            this.width - 52,
                            (double)y - this.offset + 20.0,
                            1.0,
                            Colors.getColor(145, 50),
                            hovering ? Colors.getColor(145, 50) : -2144259791
                    );
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(this.width - 74 + 10, (double)y - this.offset, 0.0);
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    this.mc.fontRendererObj.drawStringWithShadow("Change", 0.0F - (float)this.mc.fontRendererObj.getStringWidth("Change") / 2.0F, 0.0F, -1);
                    this.mc.fontRendererObj.drawStringWithShadow("Account", 0.0F - (float)this.mc.fontRendererObj.getStringWidth("Account") / 2.0F, 12.0F, -1);
                    this.mc.fontRendererObj.drawStringWithShadow("Status", 0.0F - (float)this.mc.fontRendererObj.getStringWidth("Status") / 2.0F, 24.0F, -1);
                    GlStateManager.popMatrix();
                }
                String numberP = "§7" + number + ". §f* §n";
                this.drawCenteredString(this.mc.fontRendererObj, numberP + name, this.width / 2, (int)((double)y - this.offset), -1);
                this.drawCenteredString(
                        this.mc.fontRendererObj,
                        (alt.getStatus().equals(Alt.Status.NotWorking) ? "§m" : "") + pass,
                        this.width / 2,
                        (int)((double)y - this.offset + 10.0),
                        Colors.getColor(110)
                );
                y += 26;
            }
        }
        GL11.glDisable(3089);
        GL11.glPopMatrix();
        super.drawScreen(par1, par2, par3);
        if (this.selectedAlt == null) {
            this.login.enabled = false;
            this.remove.enabled = false;
            this.rename.enabled = false;
        } else {
            this.login.enabled = true;
            this.remove.enabled = true;
            this.rename.enabled = true;
        }
        if (Keyboard.isKeyDown(200)) {
            this.offset -= 26.0;
        } else if (Keyboard.isKeyDown(208)) {
            this.offset += 26.0;
        }
        if (this.offset < 0.0) {
            this.offset = 0.0;
        }
        this.seatchField.drawTextBox();
        if (this.seatchField.getText().isEmpty() && !this.seatchField.isFocused()) {
            this.drawString(this.mc.fontRendererObj, "Search Alt", this.width / 2 + 120, this.height - 18, Colors.getColor(180));
        }
    }

    @Override
    public void initGui() {
        DynamicTexture viewportTexture = new DynamicTexture(256, 256);
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", viewportTexture);
        this.seatchField = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 + 116, this.height - 22, 72, 16);
        this.login = new GuiButton(1, this.width / 2 - 122, this.height - 48, 100, 20, "Login");
        this.buttonList.add(this.login);
        this.remove = new GuiButton(2, this.width / 2 - 40, this.height - 24, 70, 20, "Remove");
        this.buttonList.add(this.remove);
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 86, this.height - 48, 100, 20, "Add"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 16, this.height - 48, 100, 20, "Direct Login"));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 122, this.height - 24, 78, 20, "Random"));
        this.rename = new GuiButton(6, this.width / 2 + 38, this.height - 24, 70, 20, "Edit");
        this.buttonList.add(this.rename);
        this.buttonList.add(new GuiButton(7, this.width / 2 - 190, this.height - 24, 60, 20, "Back"));
        this.buttonList.add(new GuiButton(8, this.width / 2 - 190, this.height - 48, 60, 20, "Reload"));
        this.buttonList.add(new GuiButton(1919, this.width / 2 + 195, this.height - 48, 100, 20, "TheAlthening"));
        this.buttonList.add(new GuiButton(4545, this.width / 2 + 255, 5, 100, 20, "Connect Hypixel"));
        this.login.enabled = false;
        this.remove.enabled = false;
        this.rename.enabled = false;
    }

    @Override
    protected void keyTyped(char par1, int par2) {
        this.seatchField.textboxKeyTyped(par1, par2);
        if ((par1 == '\t' || par1 == '\r') && this.seatchField.isFocused()) {
            this.seatchField.setFocused(!this.seatchField.isFocused());
        }
        try {
            super.keyTyped(par1, par2);
        }
        catch (IOException var4) {
            var4.printStackTrace();
        }
    }

    private boolean isAltInArea(int y) {
        return (double)y - this.offset <= (double)(this.height - 50);
    }

    private boolean isMouseOverAlt(double x, double y, double y1) {
        return x >= 52.0 && y >= y1 - 4.0 && x <= (double)(this.width - 77) && y <= y1 + 20.0 && x >= 0.0 && y >= 33.0 && x <= (double)this.width && y <= (double)(this.height - 50);
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) {
        this.seatchField.mouseClicked(par1, par2, par3);
        if (this.offset < 0.0) {
            this.offset = 0.0;
        }
        double y = 38.0 - this.offset;
        for (Alt alt : this.getAlts()) {
            boolean hovering;
            if (this.isMouseOverAlt(par1, par2, y)) {
                if (alt == this.selectedAlt) {
                    this.actionPerformed(this.login);
                    return;
                }
                this.selectedAlt = alt;
            }
            boolean bl = hovering = par1 >= this.width - 76 && par1 <= this.width - 52 && (double)par2 >= y - 4.0 && (double)par2 <= y + 20.0;
            if (hovering && alt == this.selectedAlt) {
                switch (alt.getStatus()) {
                    case Unchecked: {
                        alt.setStatus(Alt.Status.Working);
                        break;
                    }
                    case Working: {
                        alt.setStatus(Alt.Status.Banned);
                        break;
                    }
                    case Banned: {
                        alt.setStatus(Alt.Status.NotWorking);
                        break;
                    }
                    case NotWorking: {
                        alt.setStatus(Alt.Status.Unchecked);
                    }
                }
                try {
                    Tifality.getInstance().getConfigManager().getFile(Alts.class).saveFile();
                } catch (Exception exception) {
                    // empty catch block
                }
            }
            y += 26.0;
        }
        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private List<Alt> getAlts() {
        ArrayList<Alt> altList = new ArrayList<>();
        for (Alt alt : AltManager.registry) {
            if (!this.seatchField.getText().isEmpty() && !alt.getMask().toLowerCase().contains(this.seatchField.getText().toLowerCase()) && !alt.getUsername().toLowerCase().contains(this.seatchField.getText().toLowerCase()))
                continue;
            altList.add(alt);
        }
        return altList;
    }

    private void prepareScissorBox(float x, float y, float x2, float y2) {
        ScaledResolution scale = new ScaledResolution(this.mc);
        int factor = scale.getScaleFactor();
        GL11.glScissor((int)(x * (float)factor), (int)(((float)scale.getScaledHeight() - y2) * (float)factor), (int)((x2 - x) * (float)factor), (int)((y2 - y) * (float)factor));
    }
}

