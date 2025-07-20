package club.tifality.utils.timer;

import club.tifality.utils.MathUtils;
import org.apache.commons.lang3.RandomUtils;

public final class TimerUtil {
    private long currentMs;
    protected static long lastMS;
    private long lastMs;

    public TimerUtil() {
        this.reset();
    }

    public long getCurrentMs() {
        return this.currentMs;
    }

    public boolean hasElapsed(long milliseconds) {
        return System.currentTimeMillis() - this.currentMs > milliseconds;
    }

    public boolean hasTimePassed(long MS) {
        long time = -1L;
        return System.currentTimeMillis() >= time + MS;
    }

    public boolean hasReached(long milliseconds) {
        return this.getCurrentMS() - lastMS >= milliseconds;
    }

    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public void reset() {
        this.currentMs = System.currentTimeMillis();
    }

    public void Reset() {
        this.lastMs = System.currentTimeMillis();
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }

    public double getPassed() {
        return this.getTime() - this.lastMs;
    }

    public void reSet() {
        this.lastMs = this.getTime();
    }

    public static long randomDelay(int minDelay, int maxDelay) {
        return RandomUtils.nextInt(minDelay, maxDelay);
    }

    public long lastReset() {
        return this.currentMs;
    }

    public long elapsed() {
        return System.currentTimeMillis() - this.currentMs;
    }

    public boolean delay(float milliSec) {
        long prevMS = 0L;
        return (float) MathUtils.getIncremental(this.getTime() - prevMS, 50.0) >= milliSec;
    }

    public static long randomClickDelay(int minCPS, int maxCPS) {
        return (long)(Math.random() * (double)(1000 / minCPS - 1000 / maxCPS + 1) + (double)(1000 / maxCPS));
    }
}

