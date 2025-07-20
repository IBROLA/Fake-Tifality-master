package club.tifality.utils.render;

import java.math.BigDecimal;
import java.math.MathContext;
import net.minecraft.client.Minecraft;

public final class Translate {
    private double x;
    private double y;
    private long lastMS;

    public Translate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void animate(double newX, double newY) {
        this.x = RenderingUtils.progressiveAnimation(this.x, newX, 1.0);
        this.y = RenderingUtils.progressiveAnimation(this.y, newY, 1.0);
    }

    public void translate(float targetX, float targetY) {
        this.x = (float)anim(this.x, targetX, 1.0);
        this.y = (float)anim(this.y, targetY, 1.0);
    }

    public void interpolate(float targetX, float targetY, float smoothing) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - this.lastMS;
        this.lastMS = currentMS;
        int deltaX = (int)(Math.abs((double)targetX - this.x) * (double)smoothing);
        int deltaY = (int)(Math.abs((double)targetY - this.y) * (double)smoothing);
        this.x = AnimationUtil.calculateCompensation(targetX, (float)this.x, delta, deltaX);
        this.y = AnimationUtil.calculateCompensation(targetY, (float)this.y, delta, deltaY);
    }

    public void interpolate(double targetX, double targetY, double smoothing) {
        this.x = AnimationUtil.animate(targetX, this.x, smoothing);
        this.y = AnimationUtil.animate(targetY, this.y, smoothing);
    }

    public static double anim(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);
        int fps = Minecraft.getDebugFPS();
        if (dif > 0.0) {
            double animationSpeed = roundToDecimalPlace(Math.min(10.0, Math.max(0.05, 144.0 / (double)fps * dif / 10.0 * speed)), 0.05);
            if (dif != 0.0 && dif < animationSpeed) {
                animationSpeed = dif;
            }

            if (now < desired) {
                return now + animationSpeed;
            }

            if (now > desired) {
                return now - animationSpeed;
            }
        }

        return now;
    }

    public static double roundToDecimalPlace(double value, double inc) {
        double halfOfInc = inc / 2.0;
        double floored = StrictMath.floor(value / inc) * inc;
        return value >= floored + halfOfInc
                ? new BigDecimal(StrictMath.ceil(value / inc) * inc, MathContext.DECIMAL64).stripTrailingZeros().doubleValue()
                : new BigDecimal(floored, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
    }

    public double getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }
}