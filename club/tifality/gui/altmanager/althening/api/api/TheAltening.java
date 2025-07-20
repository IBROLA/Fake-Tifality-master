package club.tifality.gui.altmanager.althening.api.api;

import club.tifality.gui.altmanager.althening.api.api.data.AccountData;
import club.tifality.gui.altmanager.althening.api.api.data.LicenseData;
import club.tifality.gui.altmanager.althening.api.api.util.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class TheAltening extends HttpUtils {
    private final String apiKey;
    private final String endpoint = "http://api.thealtening.com/v1/";
    private final Logger logger = Logger.getLogger("TheAltening");
    private final Gson gson = new Gson();

    public TheAltening(String apiKey) {
        this.apiKey = apiKey;
    }

    public LicenseData getLicenseData() {
        try {
            System.setProperty("http.agent", "chrome");
            String v2 = this.connect(String.format("http://api.thealtening.com/v1/license?token=%s", this.apiKey));
            return this.gson.fromJson(v2, LicenseData.class);
        } catch (IOException ex) {
            if (ex.getMessage().contains("401")) {
                this.logger.info("Invalid API Key provided");
            } else {
                this.logger.info("Failed to communicate with the website. Try again later");
            }
            return null;
        }
    }

    public AccountData getAccountData() {
        try {
            String url = this.connect(String.format("http://api.thealtening.com/v1/generate?info=true&token=%s", this.apiKey));
            return this.gson.fromJson(url, AccountData.class);
        }
        catch (IOException ex) {
            if (ex.getMessage().contains("401")) {
                this.logger.info("Invalid API Key provided");
            } else {
                this.logger.info("Failed to communicate with the website. Try again later");
            }
            return null;
        }
    }

    public boolean isPrivate(String token) {
        try {
            String url = this.connect("http://api.thealtening.com/v1/private?acctoken=" + token + "&token=" + this.apiKey);
            JsonObject state = this.gson.fromJson(url, JsonObject.class);
            return state != null && state.has("success") && state.get("success").getAsBoolean();
        } catch (IOException ex) {
            if (ex.getMessage().contains("401")) {
                this.logger.info("Invalid API Key provided");
            } else {
                this.logger.info("Failed to communicate with the website. Try again later");
            }
            return false;
        }
    }

    public boolean isFavorite(String token) {
        try {
            String url = this.connect("http://api.thealtening.com/v1/favorite?acctoken=" + token + "&token=" + this.apiKey);
            JsonObject state = this.gson.fromJson(url, JsonObject.class);
            return state != null && state.has("success") && state.get("success").getAsBoolean();
        } catch (IOException ex) {
            if (ex.getMessage().contains("401")) {
                this.logger.info("Invalid API Key provided");
            } else {
                this.logger.info("Failed to communicate with the website. Try again later");
            }
            return false;
        }
    }

    public static class Asynchronous {
        private TheAltening theAltening;

        public Asynchronous(TheAltening altening) {
            this.theAltening = altening;
        }

        public CompletableFuture<LicenseData> getLicenseData() {
            return CompletableFuture.supplyAsync(this.theAltening::getLicenseData);
        }

        public CompletableFuture<AccountData> getAccountData() {
            return CompletableFuture.supplyAsync(this.theAltening::getAccountData);
        }

        public CompletableFuture<Boolean> isPrivate(String url) {
            return CompletableFuture.supplyAsync(() -> this.theAltening.isPrivate(url));
        }

        public CompletableFuture<Boolean> isFavorite(String url) {
            return CompletableFuture.supplyAsync(() -> this.theAltening.isFavorite(url));
        }
    }
}

