package club.tifality.gui.altmanager;

import club.tifality.Tifality;
import club.tifality.gui.altmanager.althening.api.AltService;
import club.tifality.gui.notification.client.NotificationPublisher;
import club.tifality.gui.notification.client.NotificationType;
import club.tifality.gui.notification.dev.DevNotifications;
import club.tifality.manager.config.Alts;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import java.net.Proxy;

public final class AltLoginThread extends Thread {
    private final Alt alt;
    private String status;
    private final Minecraft mc = Minecraft.getMinecraft();

    public AltLoginThread(Alt alt) {
        super("Alt Login Thread");
        this.alt = alt;
        this.status = "§7Waiting...";
    }

    private Session createSession(String username, String password) {
        try {
            GuiAltManager.altService.switchService(username.contains("@alt.com") ? AltService.EnumAltService.TheAltening : AltService.EnumAltService.Mojang);
            YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
            YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication)service.createUserAuthentication(Agent.MINECRAFT);
            auth.setUsername(username);
            auth.setPassword(password);
            long var5 = System.currentTimeMillis();
            try {
                auth.logIn();
                return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
            } catch (AuthenticationException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getStatus() {
        return this.status;
    }

    @Override
    public void run() {
        if (this.alt.getPassword().equals("")) {
            this.mc.session = new Session(this.alt.getUsername(), "", "", "mojang");
            this.status = "§aLogged in. (" + this.alt.getUsername() + " - offline name)";
            NotificationPublisher.queue("Alt Manager", "Logged in as " + this.alt.getUsername(), NotificationType.OKAY, 3000);
            DevNotifications.getManager().post("Logged in as " + this.alt.getUsername());
        } else {
            this.status = "§bLogging in...";
            NotificationPublisher.queue("Alt Manager", "Loggging in", NotificationType.INFO, 3000);
            DevNotifications.getManager().post("Loggging in");
            Session auth = this.createSession(this.alt.getUsername(), this.alt.getPassword());
            if (auth == null) {
                this.status = "§cLogin failed!";
                NotificationPublisher.queue("Alt Manager", "Login failed!", NotificationType.WARNING, 3000);
                DevNotifications.getManager().post("Login failed!");
                if (this.alt.getStatus().equals(Alt.Status.Unchecked)) {
                    this.alt.setStatus(Alt.Status.NotWorking);
                }
            } else {
                AltManager.lastAlt = new Alt(this.alt.getUsername(), this.alt.getPassword());
                this.status = "§aLogged in. (" + auth.getUsername() + ")";
                NotificationPublisher.queue("Alt Manager", "Logged in as " + auth.getUsername(), NotificationType.OKAY, 3000);
                DevNotifications.getManager().post("Logged in as " + auth.getUsername());
                this.alt.setMask(auth.getUsername());
                this.mc.session = auth;
                if (this.alt.getStatus().equals((Object)Alt.Status.Unchecked)) {
                    this.alt.setStatus(Alt.Status.Working);
                }
                try {
                    Tifality.getInstance().getConfigManager().getFile(Alts.class).saveFile();
                } catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

