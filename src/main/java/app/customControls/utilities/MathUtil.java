package app.customControls.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;

/**
 * Collection of helper methods which extend the standard Java {@link Math} class
 */
public class MathUtil {

    public static double clamp(final double val, final double min, final double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    public static boolean isInInterval(final double val, final double min, final double max) {
        return val >= min && val <= max;
    }

    public static double roundToPrecision(final double val, final int precision) {
        BigDecimal rounding = new BigDecimal(val);
        return rounding.setScale(precision, RoundingMode.HALF_UP).doubleValue();
    }

    public static double max(double... n) {

        final double[] max = {0.0};

        BiFunction<Double, Double, Double> compare = (Double a, Double b) -> {
            if (b >= a && b > max[0]) max[0] = b;
            if (a >= b && a > max[0]) max[0] = a;
            return null;
        };

        final int start = n.length % 2 == 0 ? 2 : 1;

        if (start == 2) compare.apply(n[0], n[1]);
        else max[0] = n[0];

        for (int i = start; i < n.length - 1; i += 2) {
            compare.apply(n[i], n[i + 1]);
        }

        return max[0];
    }

    public static double min(double... n) {

        final double[] min = {0.0};

        BiFunction<Double, Double, Double> compare = (Double a, Double b) -> {
            if (b <= a && b < min[0]) min[0] = b;
            if (a <= b && a < min[0]) min[0] = a;
            return null;
        };

        final int start = n.length % 2 == 0 ? 2 : 1;

        if (start == 2) compare.apply(n[0], n[1]);
        else min[0] = n[0];

        for (int i = start; i < n.length - 1; i += 2) {
            compare.apply(n[i], n[i + 1]);
        }

        return min[0];
    }

}
