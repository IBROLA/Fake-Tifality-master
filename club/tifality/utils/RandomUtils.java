package club.tifality.utils;

import java.util.Random;

public final class RandomUtils {
    private static final Random RANDOM = new Random();

    private RandomUtils() {
    }

    public static double getRandomInRange(double min, double max) {
        return min + RANDOM.nextDouble() * (max - min);
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        if (endExclusive - startInclusive <= 0) {
            return startInclusive;
        }
        return startInclusive + new Random().nextInt(endExclusive - startInclusive);
    }

    public static double getRandom(double min, double max) {
        double shifted;
        Random random = new Random();
        double range = max - min;
        double scaled = random.nextDouble() * range;
        if (scaled > max) {
            scaled = max;
        }
        if ((shifted = scaled + min) > max) {
            shifted = max;
        }
        return shifted;
    }
}

