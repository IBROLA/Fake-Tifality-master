package club.tifality.utils.render;

public class AnimationUtil {
    public static float calculateCompensation(float target, float current, long delta, int speed) {
        float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }

        if (diff > (float)speed) {
            double xD = (double)((float)((long)speed * delta) / 16.0F) < 0.25 ? 0.5 : (double)((float)((long)speed * delta) / 16.0F);
            current = (float)((double)current - xD);
            if (current < target) {
                current = target;
            }
        } else if (diff < (float)(-speed)) {
            double xD = (double)((float)((long)speed * delta) / 16.0F) < 0.25 ? 0.5 : (double)((float)((long)speed * delta) / 16.0F);
            current = (float)((double)current + xD);
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }

        return current;
    }

    public static double animate(double target, double current, double speed) {
        boolean larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }

        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }

        if (larger) {
            current += factor;
        } else {
            current -= factor;
        }

        return current;
    }
}
