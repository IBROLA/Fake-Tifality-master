package club.tifality.module.impl.render;

import club.tifality.utils.render.Colors;
import org.lwjgl.opengl.GL11;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.render.OGLUtils;
import club.tifality.utils.render.RenderingUtils;

@ModuleInfo(label = "Chams", category = ModuleCategory.RENDER)
public final class Chams extends Module {
    private final Property<Boolean> wallHake = new Property<>("Through Wall", true);
    private final EnumProperty<Mode> modeValue = new EnumProperty<>("Mode", Mode.COLOR);
    private final DoubleProperty rainbowAlphaValue = new DoubleProperty("Rainbow Alpha", 255.0, () -> getRainbow().get(), 0.0, 255.0, 1.0);
    private final Property<Integer> visibleColorValue = new Property<>("Visible Color", Colors.WHITE, () -> modeValue.get() != Mode.NORMAL);
    private final Property<Integer> invisibleColorValue = new Property<>("Invisible Color", Colors.BLUE, () -> modeValue.get() != Mode.NORMAL);
    private final Property<Boolean> rainbow = new Property<>("Rainbow", false, () -> modeValue.get() != Mode.NORMAL);
    private final Property<Boolean> handValue = new Property<>("Hand", true, () -> modeValue.get() != Mode.NORMAL);

    public Property<Boolean> getWallHake() {
        return this.wallHake;
    }

    public EnumProperty<Mode> getModeValue() {
        return this.modeValue;
    }

    public DoubleProperty getRainbowAlphaValue() {
        return this.rainbowAlphaValue;
    }

    public Property<Integer> getVisibleColorValue() {
        return this.visibleColorValue;
    }

    public Property<Integer> getInvisibleColorValue() {
        return this.invisibleColorValue;
    }

    public Property<Boolean> getRainbow() {
        return this.rainbow;
    }

    public void preHandRender() {
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glDisable(2896);
        Hud hud = ModuleManager.getInstance(Hud.class);
        if (this.rainbow.get()) {
            int rgb = RenderingUtils.getRainbowFromEntity((int)hud.rainbowSpeed.get().doubleValue(), (int)hud.rainbowWidth.getValue().doubleValue(), (int)System.currentTimeMillis() / 15, false, (float)this.rainbowAlphaValue.get().doubleValue());
            RenderingUtils.color(rgb);
        } else {
            OGLUtils.color(this.visibleColorValue.get());
        }
    }

    public void postHandRender() {
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    public boolean shouldRenderHand() {
        return this.handValue.get() && this.isEnabled() && this.modeValue.get() != Mode.NORMAL;
    }

    public enum Mode {
        COLOR,
        NORMAL,
        CSGO;
    }
}
