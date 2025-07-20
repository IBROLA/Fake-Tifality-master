package club.tifality.module.impl.render;

import club.tifality.Tifality;
import club.tifality.gui.font.FontRenderer;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.api.annotations.Priority;
import club.tifality.manager.event.impl.packet.PacketReceiveEvent;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.Colors;
import club.tifality.utils.render.LockedResolution;
import club.tifality.utils.render.RenderingUtils;
import club.tifality.utils.render.Translate;
import club.tifality.utils.timer.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.MinecraftFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import club.tifality.module.Module;
import club.tifality.module.impl.player.Scaffold;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.property.impl.MultiSelectEnumProperty;
import club.tifality.utils.movement.MovementUtils;
import viamcp.ViaMCP;
import viamcp.protocols.ProtocolCollection;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@ModuleInfo(label = "HUD", category = ModuleCategory.RENDER)
public class Hud extends Module {
    private final EnumProperty<FontRendererMode> fontRender = new EnumProperty<>("Font Renderer", FontRendererMode.SMOOTHTTF);;
    private final MultiSelectEnumProperty<HudOptions> hudOption = new MultiSelectEnumProperty<>("HUD Options", HudOptions.ARRAYLIST, HudOptions.PROTOCOL, HudOptions.SUFFIX, HudOptions.FPS);
    public EnumProperty<ArrayListColorMode> arrayListColorModeProperty = new EnumProperty<>("Color Mode", ArrayListColorMode.WHITE);
    public Property<String> watermarkText = new Property<>("Client Name", this.hudOption.isSelected(HudOptions.NOSTALGIA) ? "" : (Tifality.NAME.charAt(0) + "§R§F" + Tifality.NAME.substring(1)));
    private final Property<Boolean> arraylistBg = new Property<>("Arraylist BG", false, () -> this.hudOption.isSelected(HudOptions.ARRAYLIST));
    private final Property<Boolean> virtue = new Property<>("Virtue", false, () -> this.hudOption.isSelected(HudOptions.NOSTALGIA));
    public EnumProperty<ArrayPosition> arrayPositionProperty = new EnumProperty<>("Array Position", ArrayPosition.RIGHT, () -> this.hudOption.isSelected(HudOptions.ARRAYLIST) && !(this.hudOption.isSelected(HudOptions.NOSTALGIA) && this.virtue.get()));
    private final DoubleProperty fadeSpeedProperty = new DoubleProperty("Fade Speed", 1.0, () -> this.hudOption.isSelected(HudOptions.ARRAYLIST) && this.arrayListColorModeProperty.getValue() == ArrayListColorMode.FADE, 0.1, 10.0, 0.1);
    public DoubleProperty rainbowSpeed = new DoubleProperty("Rainbow Speed", 80.0, () -> this.hudOption.isSelected(HudOptions.ARRAYLIST) && this.arrayListColorModeProperty.getValue() == ArrayListColorMode.RAINBOW, 80.0, 1000.0, 1.0);
    public DoubleProperty rainbowWidth = new DoubleProperty("Rainbow Width", 100.0, () -> this.hudOption.isSelected(HudOptions.ARRAYLIST) && this.arrayListColorModeProperty.getValue() == ArrayListColorMode.RAINBOW, 1.0, 300.0, 1.0);
    public static Property<Integer> hudColor = new Property<>("Color", new Color(0, 150, 250).getRGB());
    public static DoubleProperty scoreBoardHeight = new DoubleProperty("Scoreboard Height", 0.0, 0.0, 300.0, 1.0);
    private final TimerUtil timer = new TimerUtil();

    public Hud() {
        this.setHidden(true);
    }

    private FontRenderer getFontRenderer() {
        return (this.fontRender.getValue() == FontRendererMode.SMOOTHTTF) ? Wrapper.getTestFont1() : mc.fontRendererObj;
    }

    @Listener(Priority.HIGH)
    public void onRender2D(Render2DEvent event) {
        LockedResolution lockedResolution = event.getResolution();
        int screenX = lockedResolution.getWidth();
        int notificationYOffset = (mc.currentScreen instanceof GuiChat) ? 14 : 0;
        FontRenderer fontRenderer;
        if (this.hudOption.isSelected(HudOptions.NOSTALGIA)) {
            if (this.virtue.get()) {
                fontRenderer = mc.blockyFont;
            } else {
                fontRenderer = mc.fontRendererObj;
            }
        } else if (this.fontRender.isSelected(FontRendererMode.BLOCKY)) {
            fontRenderer = mc.blockyFont;
        } else {
            fontRenderer = this.getFontRenderer();
        }
        drawPotionStatus(new ScaledResolution(mc));
        float speed = this.fadeSpeedProperty.getValue().floatValue();
        long ms = (long)(speed * 1000.0f);
        float darkFactor = 0.48999998f;
        long currentMillis = -1L;
        int arrayListColor = hudColor.getValue();
            if (this.hudOption.isSelected(HudOptions.ARRAYLIST)) {
                if (!(this.hudOption.isSelected(HudOptions.NOSTALGIA) && this.virtue.get())) {
                    currentMillis = System.currentTimeMillis();
                    int y = this.arrayPositionProperty.getValue().equals(ArrayPosition.RIGHT) ? 2 : 12;
                    int i = 0;

                    ArrayList<Module> enabledMods = new ArrayList<>(Tifality.getInstance().getModuleManager().getModules());
                    enabledMods.sort((m1, m2) -> (int) (fontRenderer.getWidth(m2.getDisplayLabel()) - fontRenderer.getWidth(m1.getDisplayLabel())));

                    for (Module module : enabledMods) {
                        Translate translate = module.getTranslate();
                        String name = module.getDisplayLabel();
                        float moduleWidth = this.hudOption.isSelected(HudOptions.NOSTALGIA) ? mc.fontRendererObj.getWidth(name) : fontRenderer.getWidth(name);
                        if (this.arrayPositionProperty.getValue().equals(ArrayPosition.RIGHT)) {
                            if (module.isEnabled() && !module.isHidden()) {
                                translate.translate(screenX - moduleWidth - 1.0f, (float) y);
                                y += 9;
                            } else {
                                translate.animate(screenX - 1, -25.0);
                            }
                        } else if (module.isEnabled() && !module.isHidden()) {
                            translate.translate(2.0f, (float) y);
                            y += 9;
                        } else {
                            translate.animate(-moduleWidth, -25.0);
                        }
                        boolean shown = translate.getX() < screenX;
                        if (shown) {
                            int wColor = -1;
                            float offset = (currentMillis + i * 100) % ms / (ms / 2.0f);
                            switch (this.arrayListColorModeProperty.getValue()) {
                                case WHITE: {
                                    wColor = new Color(255, 255, 255).getRGB();
                                    break;
                                }
                                case CUSTOM: {
                                    wColor = arrayListColor;
                                    break;
                                }
                                case RAINBOW: {
                                    wColor = RenderingUtils.getRainbow(this.rainbowSpeed.getValue().intValue(), this.rainbowWidth.getValue().intValue(), (int) (i + System.currentTimeMillis() / 15L));
                                    break;
                                }
                                case FADE: {
                                    wColor = this.fadeBetween(arrayListColor, this.darker(arrayListColor, darkFactor), offset);
                                    break;
                                }
                            }
                            if (this.arraylistBg.getValue()) {
                                double bgY;
                                if (i == 0) {
                                    bgY = translate.getY() - 2.0;
                                } else {
                                    bgY = translate.getY();
                                }
                                if (this.arrayPositionProperty.isSelected(ArrayPosition.RIGHT)) {
                                    RenderingUtils.drawRect((float) translate.getX() - 1.0f, (float) bgY, (float) screenX, (float) translate.getY() + 9.0f, new Color(0, 0, 0, 120).getRGB());
                                } else {
                                    RenderingUtils.rectangle(translate.getX() - 1.0, bgY, translate.getX() + moduleWidth + 4.0, 9.0 + translate.getY(), new Color(12, 12, 12, 135).getRGB());
                                }
                            }
                            if (this.hudOption.isSelected(HudOptions.NOSTALGIA)) {
                                mc.fontRendererObj.drawStringWithShadow(name, (float) translate.getX() - 0.5f, (float) translate.getY(), module.getCategory().getColor());
                            } else {
                                fontRenderer.drawStringWithShadow(name, (float) translate.getX(), (float) (translate.getY() + ((this.fontRender.getValue() == FontRendererMode.SMOOTHTTF) ? -1 : 0)), wColor);
                            }
                            ++i;
                        }
                    }
                } else {
                    ArrayList<Module> sorted = new ArrayList<>();
                    for (Module m : Tifality.getInstance().getModuleManager().getModules()) {
                        if (!m.isEnabled() || m.isHidden()) continue;
                        sorted.add(m);
                    }
                    sorted.sort((m1, m2) -> (int) (mc.blockyFont.getWidth(m2.getLabel() + (m2.getSuffix() == null & m2.getUpdatedSuffix() == null ? "" : m2.getUpdatedSuffix() != null ? " §7(" + m2.getUpdatedSuffix() + ")" : " §7(" + m2.getSuffix() + ")")) - mc.blockyFont.getWidth(m1.getLabel() + (m1.getSuffix() == null & m1.getUpdatedSuffix() == null ? "" : m1.getUpdatedSuffix() != null ? " §7(" + m1.getUpdatedSuffix() + ")" : " §7(" + m1.getSuffix() + ")"))));
                    int y = 1;
                    for (Module m : sorted) {
                        String name = m.getLabel() + (m.getSuffix() == null & m.getUpdatedSuffix() == null ? "" : m.getUpdatedSuffix() != null ? " §7(" + m.getUpdatedSuffix() + ")" : " §7(" + m.getSuffix() + ")");
                        float x = lockedResolution.getWidth() - mc.blockyFont.getStringWidth(name);
                        mc.blockyFont.drawStringWithShadow(name, x - 0.5f, y, new Color(255, 255, 255).getRGB());
                        y += 9;
                    }
                }
            }
        int j = 0;
        int watermarkColor = -1;
        float offset2 = (currentMillis + j * 100.0f) % ms / (ms / 2.0f);
        switch (this.arrayListColorModeProperty.getValue()) {
            case WHITE:
            case CUSTOM: {
                watermarkColor = arrayListColor;
                break;
            }
            case RAINBOW: {
                watermarkColor = RenderingUtils.getRainbow(this.rainbowSpeed.getValue().intValue(), this.rainbowWidth.getValue().intValue(), (int) (j + System.currentTimeMillis() / 15L));
                break;
            }
            case FADE: {
                watermarkColor = this.fadeBetween(arrayListColor, this.darker(arrayListColor, darkFactor), offset2);
                break;
            }
        }
        SimpleDateFormat shit = new SimpleDateFormat("HH:mm");
        DecimalFormat yes = new DecimalFormat("0");
        DecimalFormat bps = new DecimalFormat("0.00");
        boolean nostalgia = this.hudOption.isSelected(HudOptions.NOSTALGIA);
        boolean time = this.hudOption.isSelected(HudOptions.TIME);
        boolean protocol = this.hudOption.isSelected(HudOptions.PROTOCOL);
        boolean fps = this.hudOption.isSelected(HudOptions.FPS);
        boolean ping = this.hudOption.isSelected(HudOptions.PING);
        boolean coords = this.hudOption.isSelected(HudOptions.COORDS);
        boolean sessionTime = this.hudOption.isSelected(HudOptions.SESSIONTIME);
        float y2 = (mc.currentScreen instanceof GuiChat) ? -14.0f : -3.0f;
        ScaledResolution sr = new ScaledResolution(mc);
        int endTime = (int)System.currentTimeMillis();
        int lmfao = endTime - Tifality.startTime;
        String sigma = " " + (protocol ? "§7[§f" + ProtocolCollection.getProtocolById(ViaMCP.getInstance().getVersion()).getName() +"§7]§r " : "") + (time ? ("§7[§f" + shit.format(System.currentTimeMillis()) + "§7]§r ") : "") + (fps ? ("§7[§f" + Minecraft.getDebugFPS() + " FPS§7]§r ") : "") + (ping ? ("§7[§f" + getPing(mc.thePlayer) + "ms§7]§r ") : "");
        String bruh = "§7XYZ:§r  " + yes.format(mc.thePlayer.posX) + " " + yes.format(mc.thePlayer.posY) + " " + yes.format(mc.thePlayer.posZ);
        String kekw = "§7b/s:  §r" + bps.format(MovementUtils.getBPS());
        String timed = String.format("%dh %dm %ds", TimeUnit.MILLISECONDS.toHours(lmfao), TimeUnit.MILLISECONDS.toMinutes(lmfao), TimeUnit.MILLISECONDS.toSeconds(lmfao) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(lmfao)));
        float x = sr.getScaledWidth() / 2.0f - mc.fontRendererObj.getStringWidth(bruh) / 2.0f;
        if (nostalgia) {
            if (this.virtue.get()) {
                float rectX = 45;
                float rectY = 13;
                float textX = 3;
                float textY = 3;
                float optionX = 3;

                if (this.hudOption.isSelected(HudOptions.FPS)) {
                    rectX += 6;
                    rectY += 9;
                    textX += 2;
                    optionX += 2;
                }
                if (this.hudOption.isSelected(HudOptions.PROTOCOL)) {
                    if (!this.hudOption.isSelected(HudOptions.FPS)) {
                        rectX += 6;
                        rectY += 9;
                        textX += 3;
                        optionX += 2.5;
                    } else {
                        rectX += 5;
                        rectY += 9;
                        textX += 3;
                        optionX += 2.5;
                    }
                }
                if (this.hudOption.isSelected(HudOptions.FPS) && !this.hudOption.isSelected(HudOptions.PROTOCOL) && Minecraft.getDebugFPS() >= 1000) {
                    rectX += 5;
                    textX += 2;
                }
                RenderingUtils.drawRectBordered(1.0f, 1.0f, rectX, rectY, .5f, new Color(111, 111, 111, 111).getRGB(), new Color(20, 20, 20, 200).getRGB());
                mc.blockyFont.drawStringWithShadow("§fVirtue 6", textX, 3.0f, -1);
                if (this.hudOption.isSelected(HudOptions.FPS)) {
                    textY += 9;
                    mc.blockyFont.drawStringWithShadow("§7Fps: " + Minecraft.getDebugFPS(), optionX, textY, -1);
                }
                if (this.hudOption.isSelected(HudOptions.PROTOCOL)) {
                    textY += 9;
                    mc.blockyFont.drawStringWithShadow("§7Ver: " + ProtocolCollection.getProtocolById(ViaMCP.getInstance().getVersion()).getName(), optionX - 1.5f, textY, -1);
                }
            } else {
                mc.fontRendererObj.drawStringWithShadow("Exhibition".charAt(0) + "§7" + "Exhibition".substring(1) + sigma, 2.0f, 2.0f, new Color(188, 255, 188).getRGB());
            }
        } else {
            fontRenderer.drawStringWithShadow((this.fontRender.getValue() == FontRendererMode.SMOOTHTTF) ? (this.watermarkText.getValue() + sigma) : ("§l" + this.watermarkText.getValue() + sigma), 2.0f, 2.0f, watermarkColor);
        }
        if (coords && nostalgia) {
            mc.fontRendererObj.drawStringWithShadow(bruh, x, 2.0f, watermarkColor);
            mc.fontRendererObj.drawStringWithShadow(kekw, sr.getScaledWidth() / 2.0f - mc.fontRendererObj.getStringWidth(kekw) / 2.0f, 12.0f, watermarkColor);
        } else if (coords) {
            fontRenderer.drawStringWithShadow("§7XYZ:§r " + yes.format(mc.thePlayer.posX) + " " + yes.format(mc.thePlayer.posY) + " " + yes.format(mc.thePlayer.posZ) + " §7b/s: §r" + bps.format(MovementUtils.getBPS()), 2.0f, sr.getScaledHeight() - 9 + y2, watermarkColor);
        }
        if (sessionTime) {
            mc.fontRendererObj.drawStringWithShadow(timed, sr.getScaledWidth() / 2.0f - mc.fontRendererObj.getStringWidth(timed) / 2.0f, 30.0f, -1);
        }
        Scaffold scaffold = Tifality.getInstance().getModuleManager().getModule(Scaffold.class);
        if (this.timer.hasElapsed(1000L)) {
            if (this.timer.hasElapsed(150L)) {
                RenderingUtils.drawImage(new ResourceLocation("tifality/lag2.png"), sr.getScaledWidth() / 2 - 20, sr.getScaledHeight() / 2 - (scaffold.isEnabled() ? 85 : 65), 40, 40);
            } else {
                RenderingUtils.drawImage(new ResourceLocation("tifality/lag.png"), sr.getScaledWidth() / 2 - 20, sr.getScaledHeight() / 2 - (scaffold.isEnabled() ? 85 : 65), 40, 40);
            }
            RenderingUtils.drawOutlinedString("§lLag Detected", sr.getScaledWidth() / 2.0f - mc.fontRendererObj.getStringWidth("§lLag Detected") / 2.0f - 3.0f, sr.getScaledHeight() / 2.0f - (scaffold.isEnabled() ? 40 : 20), new Color(255, 127, 0).getRGB(), new Color(0, 0, 0).getRGB());
        }
        Tifality.getInstance().getNotificationManager().render(null, lockedResolution, true, notificationYOffset);
    }

    private static void drawPotionStatus(ScaledResolution sr) {
        MinecraftFontRenderer font = mc.fontRendererObj;
        ArrayList<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        potions.sort(Comparator.comparingDouble(effectx -> -mc.fontRendererObj.getWidth(I18n.format(Potion.potionTypes[effectx.getPotionID()].getName()))));
        float pY = (mc.currentScreen instanceof GuiChat) ? -14.0f : -3.0f;
        for (PotionEffect effect : potions) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = I18n.format(potion.getName());
            String PType = "";
            if (effect.getAmplifier() == 1) {
                name += " II";
            } else if (effect.getAmplifier() == 2) {
                name += " III";
            } else if (effect.getAmplifier() == 3) {
                name += " IV";
            }
            if (effect.getDuration() < 600 && effect.getDuration() > 300) {
                PType = PType + "§6 " + Potion.getDurationString(effect);
            } else if (effect.getDuration() < 300) {
                PType = PType + "§c " + Potion.getDurationString(effect);
            } else if (effect.getDuration() > 600) {
                PType = PType + "§7 " + Potion.getDurationString(effect);
            }
            Color c = new Color(potion.getLiquidColor());
            font.drawStringWithShadow(name, sr.getScaledWidth() - font.getWidth(name + PType) - 3, sr.getScaledHeight() - 9 + pY, Colors.getColor(c.getRed(), c.getGreen(), c.getBlue()));
            font.drawStringWithShadow(PType, sr.getScaledWidth() - font.getWidth(PType), sr.getScaledHeight() - 9 + pY, -1);
            pY -= 9.0f;
        }
    }

    @Listener
    public void onPacket(PacketReceiveEvent event) {
        Packet<?> receive = event.getPacket();
        if (!(receive instanceof S02PacketChat)) {
            this.timer.reset();
        }
    }

    private int darker(int color, float factor) {
        int r = (int)((color >> 16 & 0xFF) * factor);
        int g = (int)((color >> 8 & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF) | (a & 0xFF) << 24;
    }

    private int fadeBetween(int color1, int color2, float offset) {
        if (offset > 1.0f) {
            offset = 1.0f - offset % 1.0f;
        }
        double invert = 1.0f - offset;
        int r = (int)((color1 >> 16 & 0xFF) * invert + (color2 >> 16 & 0xFF) * offset);
        int g = (int)((color1 >> 8 & 0xFF) * invert + (color2 >> 8 & 0xFF) * offset);
        int b = (int)((color1 & 0xFF) * invert + (color2 & 0xFF) * offset);
        int a = (int)((color1 >> 24 & 0xFF) * invert + (color2 >> 24 & 0xFF) * offset);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int getPing(EntityPlayer entityPlayer) {
        if (entityPlayer == null) {
            return 0;
        }
        NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(entityPlayer.getUniqueID());
        return (networkPlayerInfo == null) ? 0 : networkPlayerInfo.getResponseTime();
    }

    public enum ArrayListColorMode {
        WHITE,
        CUSTOM,
        RAINBOW,
        FADE
    }

    public enum FontRendererMode {
        SMOOTHTTF,
        MINECRAFT,
        BLOCKY
    }

    private enum HudOptions {
        SESSIONTIME,
        NOSTALGIA,
        ARRAYLIST,
        PROTOCOL,
        COORDS,
        SUFFIX,
        TIME,
        PING,
        FPS
    }

    private enum ArrayPosition {
        LEFT,
        RIGHT
    }
}
