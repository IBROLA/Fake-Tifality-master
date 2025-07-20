package club.tifality.module;

import java.awt.Color;

public enum ModuleCategory {
    COMBAT("Combat", new Color(219, 120, 163).getRGB()),
    PLAYER("Player", new Color(224, 197, 242).getRGB()),
    MOVEMENT("Movement", new Color(91, 153, 204).getRGB()),
    RENDER("Render", new Color(255, 187, 145).getRGB()),
    OTHER("Other", new Color(196, 224, 249).getRGB());

    private final String displayName;
    private final int color;

    public final String getDisplayName() {
        return displayName;
    }

    public final int getColor() {
        return color;
    }

    private ModuleCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }
}