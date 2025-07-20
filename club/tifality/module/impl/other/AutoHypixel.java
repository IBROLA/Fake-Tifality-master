package club.tifality.module.impl.other;

import club.tifality.Tifality;
import club.tifality.manager.api.annotations.Listener;
import club.tifality.manager.event.impl.packet.PacketReceiveEvent;
import club.tifality.manager.event.impl.render.DisplayTitleEvent;
import club.tifality.module.impl.movement.LongJump;
import club.tifality.module.impl.player.InventoryCleaner;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import club.tifality.manager.event.impl.player.UpdatePositionEvent;
import club.tifality.module.Module;
import club.tifality.module.ModuleCategory;
import club.tifality.module.ModuleInfo;
import club.tifality.module.ModuleManager;
import club.tifality.module.impl.combat.KillAura;
import club.tifality.module.impl.movement.Speed;
import club.tifality.gui.notification.Notification;
import club.tifality.gui.notification.NotificationType;
import club.tifality.property.Property;
import club.tifality.utils.server.HypixelGameUtils;
import club.tifality.utils.timer.TimerUtil;
import club.tifality.utils.Wrapper;

import java.util.Arrays;
import java.util.List;

@ModuleInfo(label = "Auto Hypixel", category = ModuleCategory.OTHER)
public final class AutoHypixel extends Module {

    private final Property<Boolean> autoDisableProperty = new Property<>("On Flag", true);
    private final Property<Boolean> respawnProperty = new Property<>("On Respawn", true);
    private final Property<Boolean> autoJoinProperty = new Property<>("Auto Join", true);

    private final TimerUtil gameTimer = new TimerUtil();
    private final TimerUtil respawnTimer = new TimerUtil();

    private List<Module> movementModules;
    private List<Module> disableOnRespawn;

    private boolean needSend;

    @Listener
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && autoDisableProperty.getValue()) {
            boolean msg = false;
            for (Module module : movementModules)
                if (module.isEnabled()) {
                    module.toggle();
                    if (!msg)
                        msg = true;
                }

            if (msg)
                Tifality.getInstance().getNotificationManager().add(new Notification("Flag",
                        "Disabling modules to prevent flags", NotificationType.WARNING));
        } else if (event.getPacket() instanceof S07PacketRespawn && respawnProperty.getValue()) {
            if (respawnTimer.hasElapsed(50L)) {
                boolean msg = false;
                for (Module module : disableOnRespawn) {
                    if (module.isEnabled()) {
                        module.toggle();
                        if (!msg)
                            msg = true;
                    }
                }

                if (msg)
                    Tifality.getInstance().getNotificationManager().add(new Notification("Respawned",
                            "Disabled some modules on respawn",
                            NotificationType.INFO));
                respawnTimer.reset();
            }
        } else if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) event.getPacket();
            if (packetChat.getChatComponent().getUnformattedText().contains("Protect your bed and destroy the enemy beds"))
                Tifality.getInstance().getNotificationManager().add(new Notification(
                        "Bedwars",
                        "Do not fly until this notification closes",
                        20000L,
                        NotificationType.WARNING));
        }
    }

    @Listener
    public void onDisplayTitle(DisplayTitleEvent event) {
        if (autoJoinProperty.getValue() && event.getTitle().contains("VICTORY")) {
            Tifality.getInstance().getNotificationManager().add(
                    new Notification("Auto Join",
                            "Sending you to a new game in 2 seconds", 2000L,
                            NotificationType.INFO));
            needSend = true;
            gameTimer.reset();
        }
    }

    @Listener
    public void onUpdatePositionEven(UpdatePositionEvent event) {
        if (event.isPre()) {
            if (needSend && gameTimer.hasElapsed(2000L)) {
                Wrapper.sendPacketDirect(new C01PacketChatMessage("/play " +
                        HypixelGameUtils.getSkyWarsMode().name().toLowerCase()));
                needSend = false;
            }

//            if (!pingSpoof.isEnabled() && ServerUtils.isOnHypixel()) {
//                pingSpoof.toggle();
//                RadiumClient.getInstance().getNotificationManager().add(new Notification("Bypass",
//                        "You must use Ping Spoof on hypixel", NotificationType.WARNING));
//            }
        }
    }
    /*@EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && autoDisableProperty.getValue()) {
            boolean msg = false;
            for (Module module : movementModules)
                if (module.isEnabled()) {
                    module.toggle();
                    if (!msg)
                        msg = true;
                }

            if (msg)
                RadiumClient.getInstance().getNotificationManager().add(new Notification("Flag",
                        "Disabling modules to prevent flags", NotificationType.WARNING));
        } else if (event.getPacket() instanceof S07PacketRespawn && respawnProperty.getValue()) {
            if (respawnTimer.hasElapsed(50L)) {
                boolean msg = false;
                for (Module module : disableOnRespawn) {
                    if (module.isEnabled()) {
                        module.toggle();
                        if (!msg)
                            msg = true;
                    }
                }

                if (msg)
                    RadiumClient.getInstance().getNotificationManager().add(new Notification("Respawned",
                            "Disabled some modules on respawn",
                            NotificationType.INFO));
                respawnTimer.reset();
            }
        } else if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) event.getPacket();
            if (packetChat.getChatComponent().getUnformattedText().contains("Protect your bed and destroy the enemy beds"))
                RadiumClient.getInstance().getNotificationManager().add(new Notification(
                        "Bedwars",
                        "Do not fly until this notification closes",
                        20000L,
                        NotificationType.WARNING));
        }
    };

    @EventLink
    public final Listener<DisplayTitleEvent> onDisplayTitleEvent = event -> {
        if (autoJoinProperty.getValue() && event.getTitle().contains("VICTORY")) {
            RadiumClient.getInstance().getNotificationManager().add(
                    new Notification("Auto Join",
                            "Sending you to a new game in 2 seconds", 2000L,
                            NotificationType.INFO));
            needSend = true;
            gameTimer.reset();
        }
    };
    @EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
        if (event.isPre()) {
            if (needSend && gameTimer.hasElapsed(2000L)) {
                Wrapper.sendPacketDirect(new C01PacketChatMessage("/play " +
                        HypixelGameUtils.getSkyWarsMode().name().toLowerCase()));
                needSend = false;
            }

//            if (!pingSpoof.isEnabled() && ServerUtils.isOnHypixel()) {
//                pingSpoof.toggle();
//                RadiumClient.getInstance().getNotificationManager().add(new Notification("Bypass",
//                        "You must use Ping Spoof on hypixel", NotificationType.WARNING));
//            }
        }
    };*/

    @Override
    public void onEnable() {
        if (movementModules == null)
            movementModules = Arrays.asList(
                    ModuleManager.getInstance(Speed.class),
                    ModuleManager.getInstance(LongJump.class));

        if (disableOnRespawn == null)
            disableOnRespawn = Arrays.asList(
                    ModuleManager.getInstance(KillAura.class),
                    ModuleManager.getInstance(InventoryCleaner.class));
    }
}
