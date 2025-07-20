package club.tifality.module.impl.render;

import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.render.overlay.Render2DEvent;
import club.tifality.manager.event.impl.render.overlay.RenderCrosshairEvent;
import club.tifality.utils.render.Colors;
import net.minecraft.client.gui.Gui;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.Property;
import club.tifality.property.impl.DoubleProperty;
import club.tifality.utils.movement.PlayerInfoCache;
import club.tifality.utils.Wrapper;
import club.tifality.utils.render.LockedResolution;
import club.tifality.utils.render.RenderingUtils;

@ModuleInfo(label = "Crosshair", category = ModuleCategory.RENDER)
public final class Crosshair extends Module {

    private final DoubleProperty gapProperty = new DoubleProperty("Gap", 1.0D, 0.0D, 10.0D, 0.5D);
    private final DoubleProperty lengthProperty = new DoubleProperty("Length", 3.0D, 0.0D, 10.0D, 0.5D);
    private final DoubleProperty widthProperty = new DoubleProperty("Width", 1.0D, 0.0D, 5.0D, 0.5D);
    private final Property<Boolean> tShapeProperty = new Property<>("T Shape", false);
    private final Property<Boolean> dotProperty = new Property<>("Dot", false);
    private final Property<Boolean> dynamicProperty = new Property<>("Dynamic", true);
    private final Property<Boolean> outlineProperty = new Property<>("Outline", true);
    private final DoubleProperty outlineWidthProperty = new DoubleProperty("Outline Width", 0.5D,
                                                                           outlineProperty::getValue,
                                                                           0.5D, 5.0D, 0.5D);
    private final Property<Integer> colorProperty = new Property<>("Color", Colors.BLUE);

    @Listener
    public void onRenderCrosshairEvent(RenderCrosshairEvent event) {
        event.setCancelled(true);
    }

    @Listener
    public void onRender2D(Render2DEvent event) {
        final double width = widthProperty.getValue();
        final double halfWidth = width / 2.0D;
        double gap = gapProperty.getValue();

        if (dynamicProperty.getValue()) {
            gap *= Math.max(Wrapper.getPlayer().isSneaking() ? 0.5D : 1.0D, RenderingUtils.interpolate(
                    PlayerInfoCache.getPrevLastDist(),
                    PlayerInfoCache.getLastDist(),
                    event.getPartialTicks()) * 10.0D);
        }
        final double length = lengthProperty.getValue();
        final int color = colorProperty.getValue();
        final double outlineWidth = outlineWidthProperty.getValue();
        final boolean outline = outlineProperty.getValue();
        final boolean tShape = tShapeProperty.getValue();

        final LockedResolution lr = event.getResolution();
        final double middleX = lr.getWidth() / 2.0D;
        final double middleY = lr.getHeight() / 2.0D;

        if (outline) {
            // Left
            Gui.drawRect(middleX - gap - length - outlineWidth,
                    middleY - halfWidth - outlineWidth,
                    middleX - gap + outlineWidth,
                    middleY + halfWidth + outlineWidth, 0x96000000);
            // Right
            Gui.drawRect(middleX + gap - outlineWidth,
                    middleY - halfWidth - outlineWidth,
                    middleX + gap + length + outlineWidth,
                    middleY + halfWidth + outlineWidth, 0x96000000);
            // Bottom
            Gui.drawRect(middleX - halfWidth - outlineWidth,
                    middleY + gap - outlineWidth,
                    middleX + halfWidth + outlineWidth,
                    middleY + gap + length + outlineWidth, 0x96000000);
            if (!tShape)
                // Top
                Gui.drawRect(middleX - halfWidth - outlineWidth,
                        middleY - gap - length - outlineWidth,
                        middleX + halfWidth + outlineWidth,
                        middleY - gap + outlineWidth, 0x96000000);
        }
        // Left
        Gui.drawRect(middleX - gap - length,
                middleY - halfWidth,
                middleX - gap,
                middleY + halfWidth, color);
        // Right
        Gui.drawRect(middleX + gap,
                middleY - halfWidth,
                middleX + gap + length,
                middleY + halfWidth, color);
        // Bottom
        Gui.drawRect(middleX - halfWidth,
                middleY + gap,
                middleX + halfWidth,
                middleY + gap + length, color);
        if (!tShape)
            // Top
            Gui.drawRect(middleX - halfWidth,
                    middleY - gap - length,
                    middleX + halfWidth,
                    middleY - gap , color);

        if (dotProperty.getValue()) {
            if (outline) {
                Gui.drawRect(middleX - halfWidth - outlineWidth, middleY - halfWidth - outlineWidth,
                        middleX + halfWidth + outlineWidth, middleY + halfWidth + outlineWidth, 0x96000000);
            }

            Gui.drawRect(middleX - halfWidth, middleY - halfWidth, middleX + halfWidth, middleY + halfWidth, color);
        }
    }
}
