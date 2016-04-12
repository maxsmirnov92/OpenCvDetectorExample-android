package ru.maxsmr.commonutils.data;

import java.util.Random;

public class MathUtils {

    public static double round(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("incorrect precision: "+ precision);
        }
        double delimiter = 1d;
        for (int i = 0; i < precision; i++) {
            delimiter *= 10;
        }
        return (double) Math.round(value * delimiter) / delimiter;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive

        return rand.nextInt((max - min) + 1) + min;
    }

    public static long randLong(long min, long max) {
        return (new Random().nextLong() % (max - min)) + min;
    }
}