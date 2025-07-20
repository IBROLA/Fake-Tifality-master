package club.tifality.gui.altmanager.althening.api;

import club.tifality.gui.altmanager.althening.api.utilities.ReflectionUtility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class AltService {
    private final ReflectionUtility userAuthentication = new ReflectionUtility("com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication");
    private final ReflectionUtility minecraftSession = new ReflectionUtility("com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService");
    private EnumAltService currentService;

    public void switchService(EnumAltService service) throws NoSuchFieldException, IllegalAccessException {
        if (this.currentService == service) {
            return;
        }
        this.reflectionFields(service.hostname);
        this.currentService = service;
    }

    private void reflectionFields(String field) throws NoSuchFieldException, IllegalAccessException {
        HashMap<String, URL> link = new HashMap<>();
        String http = field.contains("thealtening") ? "http" : "https";
        link.put("ROUTE_AUTHENTICATE", this.constantURL(http + "://authserver." + field + ".com/authenticate"));
        link.put("ROUTE_INVALIDATE", this.constantURL(http + "://authserver" + field + "com/invalidate"));
        link.put("ROUTE_REFRESH", this.constantURL(http + "://authserver." + field + ".com/refresh"));
        link.put("ROUTE_VALIDATE", this.constantURL(http + "://authserver." + field + ".com/validate"));
        link.put("ROUTE_SIGNOUT", this.constantURL(http + "://authserver." + field + ".com/signout"));
        link.forEach((clazz, value) -> {
            try {
                this.userAuthentication.setStaticField(clazz, value);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        this.userAuthentication.setStaticField("BASE_URL", http + "://authserver." + field + ".com/");
        this.minecraftSession.setStaticField("BASE_URL", http + "://sessionserver." + field + ".com/session/minecraft/");
        this.minecraftSession.setStaticField("JOIN_URL", this.constantURL(http + "://sessionserver." + field + ".com/session/minecraft/join"));
        this.minecraftSession.setStaticField("CHECK_URL", this.constantURL(http + "://sessionserver." + field + ".com/session/minecraft/hasJoined"));
        this.minecraftSession.setStaticField("WHITELISTED_DOMAINS", new String[]{".minecraft.net", ".mojang.com", ".thealtening.com"});
    }

    private URL constantURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new Error("Couldn't create constant for " + url, ex);
        }
    }

    public EnumAltService getCurrentService() {
        if (this.currentService == null) {
            this.currentService = EnumAltService.TheAltening;
        }
        return this.currentService;
    }

    public enum EnumAltService {
        Mojang("Mojang", 0, "mojang"),
        TheAltening("THEALTENING", 1, "thealtening");

        String hostname;

        EnumAltService(String mode, int type, String host) {
            this.hostname = host;
        }
    }
}

