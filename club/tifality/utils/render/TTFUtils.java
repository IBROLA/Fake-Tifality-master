package club.tifality.utils.render;

import net.minecraft.util.ResourceLocation;
import club.tifality.utils.Wrapper;

import java.awt.*;
import java.io.IOException;

public final class TTFUtils {

    private TTFUtils() {

    }

    public static Font getFontFromLocation(String fileName, int size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Wrapper.getMinecraft().getResourceManager()
                .getResource(new ResourceLocation("tifality/fonts/" + fileName))
                .getInputStream())
                .deriveFont(Font.PLAIN, size);
        } catch (FontFormatException | IOException ignored) {
            return null;
        }
    }

}
