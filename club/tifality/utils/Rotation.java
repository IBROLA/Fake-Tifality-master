package club.tifality.utils;

public final class Rotation {
    private float yaw;
    private float pitch;

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation copy(float yaw, float pitch) {
        return new Rotation(yaw, pitch);
    }

    public String toString() {
        return "Rotation(yaw=" + this.yaw + ", pitch=" + this.pitch + ")";
    }

    public int hashCode() {
        return Float.floatToIntBits(this.yaw) * 31 + Float.floatToIntBits(this.pitch);
    }

    public boolean equals(Object o) {
        if (this != o) {
            if (o instanceof Rotation) {
                Rotation rotation = (Rotation) o;
                return Float.compare(this.yaw, rotation.yaw) == 0 && Float.compare(this.pitch, rotation.pitch) == 0;
            }

            return false;
        } else {
            return true;
        }
    }
}
