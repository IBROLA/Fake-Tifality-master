package club.tifality;

import club.tifality.manager.command.CommandManager;
import org.lwjgl.opengl.Display;
import club.tifality.gui.console.SourceConsoleGUI;
import club.tifality.gui.screen.SplashProgress;
import club.tifality.manager.api.bus.Bus;
import club.tifality.manager.api.bus.BusImpl;
import club.tifality.manager.friend.FriendManager;
import club.tifality.manager.homoBus.bus.impl.EventBus;
import club.tifality.manager.config.ConfigManager;
import club.tifality.manager.event.Event;
import club.tifality.manager.keybind.BindSystem;
import club.tifality.player.PlayerManager;
import club.tifality.gui.csgo.SkeetUI;
import club.tifality.gui.font.FontManager;
import club.tifality.module.ModuleManager;
import club.tifality.gui.notification.NotificationManager;
import club.tifality.utils.Wrapper;

import static org.lwjgl.opengl.GL11.*;

/**
 * Stop inv manager from throwing out main sword
 * tabui
 * finish enemy manager implementation (killaura priority etc)
 * basic modules + commands need to be added
 * Fix gui expandable boxes not being able to be clicked if outside module box
 * Config in da gui
 * 2 block step
 * Fix scaffold silent or ghostblock when not jumping
 * southside chams mode
 * Armor dura on target hud and esps
 * add forceground to autopotuion and module checks (fly scaffold etc)
 * child options do not save
 * southside combat mods
 * Auto load config on startup
 * module/property aliases
 */

public final class Tifality {
    public static final Tifality INSTANCE = new Tifality();
    private Bus<Event> eventBuz;
    private EventBus<Event> eventBus;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private PlayerManager playerManager;
    private NotificationManager notificationManager;
    private static String API;
    private CommandManager commandManager;
    private FriendManager friendManager;
    public static final String NAME = "Tifality";
    public static final String VERSION = "Beta Build";
    public static int startTime = (int)System.currentTimeMillis();;
    private static SourceConsoleGUI sourceConsoleGUI;

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CommandManager getCommandHandler() {
        return commandManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    /*private RadiumClient() {
        getEventBus().subscribe(this);
    }*/

    public void onStarting() {
        Display.setTitle("Launching, Please wait...");
    }

    public void onStarted() {
        Display.setTitle("Minecraft 1.8.9");
    }

    public void onPostInit() {
        onStarting();
        SplashProgress.setProgress(1);
        //setGLHints();
        FontManager.initTextures();
        /*try {
            RichPresence clientRichPresence = new RichPresence();
            clientRichPresence.setup();
        } catch (Throwable clientRichPresence) {
            // empty catch block
        }*/
        configManager = new ConfigManager();
        playerManager = new PlayerManager();
        friendManager = new FriendManager();
        EventBus<?> eventBus = new EventBus<>();
        eventBuz = new BusImpl<>();
        moduleManager = new ModuleManager();
        SkeetUI.init();
        sourceConsoleGUI = new SourceConsoleGUI();
        notificationManager = new NotificationManager();
        commandManager = new CommandManager();
        //this.eventBuz.subscribe(new club.tifality.gui.click.ClickGui());
        //this.eventBuz.subscribe(new ClickGui());
        eventBus.subscribe(new BindSystem(moduleManager.getModules()));
        eventBuz.subscribe(new BindSystem(moduleManager.getModules()));
        moduleManager.postInit();
        getConfigManager().loadConfig("Value");
        configManager.loadFiles();
        Wrapper.getFontRenderer().generateTextures();
        Wrapper.getNameTagFontRenderer().generateTextures();
        Wrapper.getSFBold12Font().generateTextures();
        Wrapper.getSFBold18Font().generateTextures();
        Wrapper.getSFBold20Font().generateTextures();
        Wrapper.getCSGOFontRenderer().generateTextures();
        Wrapper.getTestFont().generateTextures();
        Wrapper.getVerdana10().generateTextures();
        Wrapper.getVerdana16().generateTextures();
        Wrapper.getTitleFont().generateTextures();
        Wrapper.getInfoFont().generateTextures();
        Wrapper.getBigFontRenderer().generateTextures();
        Wrapper.getEspFontRenderer().generateTextures();
        Wrapper.getEspBiggerFontRenderer().generateTextures();
        Wrapper.getTestFont1().generateTextures();
        onStarted();
    }

    private static void setGLHints() {
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
    }

    /*public EventBus<Event> getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus<>();
        }

        return eventBus;
    }*/

    public static SourceConsoleGUI getSourceConsoleGUI() {
        return sourceConsoleGUI;
    }

    public void onShutDown() {
        this.getConfigManager().saveConfig("Value");
        this.configManager.saveFiles();
    }

    public Bus<Event> getEventBus() {
        return this.eventBuz;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static Tifality getInstance() {
        return INSTANCE;
    }

    public static String getAPI() {
        return API;
    }

    public static void setAPI(String API) {
        Tifality.API = API;
    }
}
