package club.tifality.gui.altmanager.althening.api.api.data;

import com.google.gson.annotations.SerializedName;

public class LicenseData {
    private String username;
    private boolean premium;
    @SerializedName(value="premium_name")
    private String premiumName;
    @SerializedName(value="expires")
    private String expiryDate;

    public String getUsername() {
        return this.username;
    }

    public boolean isPremium() {
        return this.premium;
    }

    public String getPremiumName() {
        return this.premiumName;
    }

    public String getExpiryDate() {
        return this.expiryDate;
    }

    public String toString() {
        return String.format("LicenseData[%s:%s:%s:%s]", this.username, this.premium, this.premiumName, this.expiryDate);
    }

    public boolean equals(Object object) {
        if (!(object instanceof LicenseData)) {
            return false;
        }
        LicenseData data = (LicenseData)object;
        return data.getExpiryDate().equals(this.getExpiryDate()) && data.getPremiumName().equals(this.getPremiumName()) && data.isPremium() == this.isPremium() && data.getUsername().equals(this.getUsername());
    }
}

