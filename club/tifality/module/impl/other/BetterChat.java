package club.tifality.module.impl.other;

import club.tifality.manager.api.annotations.Listener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import club.tifality.manager.event.impl.packet.PacketReceiveEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.utils.Wrapper;

@ModuleInfo(label = "Better Chat", category = ModuleCategory.OTHER)
public final class BetterChat extends Module {

    private String lastMessage = "";
    private int amount;
    private int line;

    @Listener
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        final Packet<?> packet = event.getPacket();
        if (packet instanceof S2EPacketCloseWindow) {
            if (isTypingInChat()) event.setCancelled();
        } else if (packet instanceof S02PacketChat) {
            S02PacketChat s02PacketChat = (S02PacketChat) packet;
            if (s02PacketChat.getType() == 0) {
                IChatComponent message = s02PacketChat.getChatComponent();
                String rawMessage = message.getFormattedText();
                GuiNewChat chatGui = Wrapper.getMinecraft().ingameGUI.getChatGUI();
                if (lastMessage.equals(rawMessage)) {
                    chatGui.deleteChatLine(line);
                    amount++;
                    s02PacketChat.getChatComponent().appendText(EnumChatFormatting.GRAY + " [x" + amount + "]");
                } else {
                    amount = 1;
                }
                line++;
                lastMessage = rawMessage;
                chatGui.printChatMessageWithOptionalDeletion(message, line);

                if (this.line > 256) {
                    this.line = 0;
                }

                event.setCancelled();
            }
        }
    }
    /*@EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = e -> {
        final Packet<?> packet = e.getPacket();
        if (packet instanceof S2EPacketCloseWindow) {
            if (isTypingInChat()) e.setCancelled();
        } else if (packet instanceof S02PacketChat) {
            S02PacketChat s02PacketChat = (S02PacketChat) packet;
            if (s02PacketChat.getType() == 0) {
                IChatComponent message = s02PacketChat.getChatComponent();
                String rawMessage = message.getFormattedText();
                GuiNewChat chatGui = Wrapper.getMinecraft().ingameGUI.getChatGUI();
                if (lastMessage.equals(rawMessage)) {
                    chatGui.deleteChatLine(line);
                    amount++;
                    s02PacketChat.getChatComponent().appendText(EnumChatFormatting.GRAY + " [x" + amount + "]");
                } else {
                    amount = 1;
                }
                line++;
                lastMessage = rawMessage;
                chatGui.printChatMessageWithOptionalDeletion(message, line);

                if (this.line > 256) {
                    this.line = 0;
                }

                e.setCancelled();
            }
        }
    };*/

    private boolean isTypingInChat() {
        return Wrapper.getCurrentScreen() instanceof GuiChat;
    }
}
