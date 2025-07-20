package club.tifality.module.impl.other;

import club.tifality.manager.api.annotations.Listener;
import org.apache.commons.lang3.RandomUtils;
import club.tifality.manager.event.impl.player.SendMessageEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.property.impl.EnumProperty;

@ModuleInfo(label = "Chat Bypass", category = ModuleCategory.OTHER)
public final class ChatBypass extends Module {

    private static final char[] INVIS_CHARS = {'\u2764'};
    
    private final EnumProperty<BypassMode> bypassModeProperty = new EnumProperty<>("Mode", BypassMode.INVIS);

    @Listener
    public void SendMessageEvent(SendMessageEvent event) {
        if (event.getMessage().startsWith("/")) {
            return;
        }
        switch (bypassModeProperty.getValue()) {
            case INVIS:
                final StringBuilder stringBuilder = new StringBuilder();
                for (char character : event.getMessage().toCharArray()) {
                    stringBuilder.append(character)
                            .append(INVIS_CHARS[RandomUtils.nextInt(0, INVIS_CHARS.length)]);
                }
                event.setMessage(stringBuilder.toString());
                break;
            case FONT:
                // TODO: find a way to use fonts
                break;
        }
    }

    private enum BypassMode {
        INVIS, FONT
    }
}