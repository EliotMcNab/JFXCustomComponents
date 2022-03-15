package app.customControls.utilities;

import java.util.Locale;

/**
 * Collection of helper methods for color conversion
 */
public class ColorUtil {
    public static class Hsv {

        private interface HSVFunction {
            double value(double h, double dh, double min, double max, double slope);
        }

        private static final HSVFunction[][] sextants;

        static {
            sextants = new HSVFunction[6][3];

            HSVFunction minF = (h, dh, min, max, slope) -> min;
            HSVFunction maxF = (h, dh, min, max, slope) -> max;
            HSVFunction positiveSlope = (h, dh, min, max, slope) -> slope * (h - dh) + min;
            HSVFunction negativeSlope = (h, dh, min, max, slope) -> max - slope * (h - dh);

            sextants[0] = new HSVFunction[]{maxF, positiveSlope, minF};
            sextants[1] = new HSVFunction[]{negativeSlope, maxF, minF};
            sextants[2] = new HSVFunction[]{minF, maxF, positiveSlope};
            sextants[3] = new HSVFunction[]{minF, negativeSlope, maxF};
            sextants[4] = new HSVFunction[]{positiveSlope, minF, maxF};
            sextants[5] = new HSVFunction[]{maxF, minF, negativeSlope};
        }

        public static int[] toRgb(final double h, final double s, final double v) {

            final double adjustS = s / 100;
            final double adjustV = v / 100;

            int location = (int) (h / 60) % 6;
            final int dh = location * 60;
            final double slope = adjustV * adjustS / 60.0;
            final double min = (1 - adjustS) * adjustV;

            HSVFunction[] currentSextant = sextants[location];

            final int r = (int) Math.ceil(currentSextant[0].value(h, dh, min, adjustV, slope) * 255);
            final int g = (int) Math.ceil(currentSextant[1].value(h, dh, min, adjustV, slope) * 255);
            final int b = (int) Math.ceil(currentSextant[2].value(h, dh, min, adjustV, slope) * 255);

            return new int[]{r, g, b};

        }

        public static int toArgbCode(final double h, final double s, final double v) {

            final int[] rgb = toRgb(h, s, v);
            return Rgb.toArgbCode(rgb[0], rgb[1], rgb[2]);

        }

        public static int toRgbCode(final double h, final double s, final double v) {

            final int[] rgb = toRgb(h, s, v);
            return Rgb.toRgbCode(rgb[0], rgb[1], rgb[2]);

        }

        public static String toHex(final double h, final double s, final double v) {

            final int[] rgb = toRgb(h, s, v);
            return Rgb.toHex(rgb[0], rgb[1], rgb[2]);

        }

        public static int getSextant(final int hue) {
            // loops back the hue
            int adjustedHue = hue % 360;
            // adjusts the hue if it is negative
            if (adjustedHue < 0) adjustedHue += 360 * (int) (-adjustedHue / 360.0 + 1);
            // determines the sextant
            return adjustedHue / 60;
        }

        private static final int ROUNDING_PRECISION = 2;

        public static String toHsvString(final double hue, final double saturation, final double value) {

            final double hueRounded = MathUtil.roundToPrecision(hue, ROUNDING_PRECISION);
            final double saturationRounded = MathUtil.roundToPrecision(saturation, ROUNDING_PRECISION);
            final double valueRounded = MathUtil.roundToPrecision(value, ROUNDING_PRECISION);

            return String.format("hsv{h: %s, s: %s, v: %s}", hueRounded, saturationRounded, valueRounded);
        }

        public static double[] fromHsvString(final String hue, final String sat, final String val) {

            final String parsedHue = hue.replaceFirst("h:", "")
                                        .replaceAll(" ", "");
            final String parsedSat = sat.replaceFirst("s:", "")
                                        .replaceAll(" ", "");
            final String parsedVal = val.replaceFirst("v:", "")
                                        .replaceAll(" ", "");

            final double hueVal = Double.parseDouble(parsedHue) % 360;
            final double saturationVal = Double.parseDouble(parsedSat);
            final double valueVal = Double.parseDouble(parsedVal);

            return new double[]{hueVal, saturationVal, valueVal};

        }

        public static double[] fromHsvString(final String hsv) {

            // checks the validity of the hsv string
            if (StringUtil.Validation.isValidHsv(hsv)) return new double[]{0, 100, 100};

            final String[] parsed = hsv.replace("hsv", "")
                                       .replace("(", "")
                                       .replace(")", "")
                                       .split(",");

            return fromHsvString(parsed[0], parsed[1], parsed[2]);

        }
    }

    public static class Rgb {

        public static int toArgbCode(final int r, final int g, final int b) {
            return (0xFF << 24) | toRgbCode(r, g, b);
        }

        public static int toRgbCode(final int r, final int g, final int b) {
            return (r << 16) | (g << 8) | b;
        }

        public static String toHex(final int r, final int g, final int b) {
            String hex = Integer.toHexString(toRgbCode(r, g, b)).toUpperCase(Locale.ROOT);

            final int length = hex.length();
            if (length < 6) hex = "0".repeat(6 - length) + hex;

            return "#" + hex;
        }

        /**
         * Color conversion from the RGB to the HSV color space
         * @see <a href="https://www.rapidtables.com/convert/color/rgb-to-hsv.html">tutorial</a>
         * @param red (int): red value
         * @param green (int): green value
         * @param blue (int): blue value
         * @return (double[]): HSV values, where <br>
         * <list>
         *     <li>0 = hue</li>
         *     <li>1 = saturation</li>
         *     <li>2 = value</li>
         * </list>
         */
        public static double[] toHsv(final double red, final double green, final double blue) {

            // normalises red, green and blue values
            final double rNormal = red / 255;
            final double gNormal = green / 255;
            final double bNormal = blue / 255;

            final double cMax = MathUtil.max(rNormal, gNormal, bNormal);
            final double cMin = MathUtil.min(rNormal, gNormal, bNormal);
            final double delta = cMax - cMin;

            final double h = h(rNormal, gNormal, bNormal, cMax, delta);
            final double s = s(cMax, delta);
            final double v = cMax;

            return new double[]{h >= 0 ? h : 360 + h, s * 100, v * 100};
        }

        private static double h(
                final double rNormal,
                final double gNormal,
                final double bNormal,
                final double cMax,
                final double delta
        ) {

            if (delta == 0) return 0;

            if (cMax == rNormal) return 60 * ((gNormal - bNormal) / delta % 6);
            if (cMax == gNormal) return 60 * ((bNormal - rNormal) / delta + 2);
            else return 60 * ((rNormal - gNormal) / delta + 4);

        }

        private static double s(final double cMax, final double delta) {
            return cMax == 0 ? 0 : delta / cMax;
        }

        public static int[] fromRgbString(final String r, final String g, final String b) {

            final String parsedR = r.replaceFirst("r:", "")
                                    .replaceAll(" ", "");
            final String parsedG = g.replaceFirst("g:", "")
                                    .replaceAll(" ", "");
            final String parsedB = b.replaceFirst("b:", "")
                                    .replaceAll(" ", "");

            final int redValue = Integer.parseInt(parsedR);
            final int greenValue = Integer.parseInt(parsedG);
            final int blueValue = Integer.parseInt(parsedB);

            return new int[]{redValue, greenValue, blueValue};

        }

        public static int[] fromRgbString(final String rgb) {

            // checks the validity of the rgb string
            if (StringUtil.Validation.isValidHsv(rgb)) return new int[]{255, 0, 0};

            final String[] parsed = rgb.replace("rgb", "")
                                       .replace("(", "")
                                       .replace(")", "")
                                       .split(",");

            return fromRgbString(parsed[0], parsed[1], parsed[2]);

        }

    }

    public static class Hex {

        public static int[] toRgb(final String hex) {

            final String hexCode = hex.replaceFirst("#", "");

            // makes sure that the hex ode is of the correct length
            if (hexCode.length() != 6) {
                throw new IllegalArgumentException(String.format("Invalid hex code %s, must have a length of 6", hex));
            }

            final String red = hexCode.substring(0, 2);
            final String green = hexCode.substring(2, 4);
            final String blue = hexCode.substring(4, 6);

            final int redValue = Integer.parseInt(red, 16);
            final int greenValue = Integer.parseInt(green, 16);
            final int blueValue = Integer.parseInt(blue, 16);

            return new int[]{redValue, greenValue, blueValue};
        }

        public static double[] toHsv(final String hex) {
            final int[] rgb = toRgb(hex);
            return Rgb.toHsv(rgb[0], rgb[1], rgb[2]);
        }

    }

}
