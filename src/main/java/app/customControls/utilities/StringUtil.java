package app.customControls.utilities;

import app.customControls.controls.colorPicker.ColorType;
import javafx.scene.control.TextFormatter;

import java.util.Arrays;
import java.util.function.UnaryOperator;

/**
 * Collection of methods used to validate and manipulate Strings
 */
public class StringUtil {
    public static class Validation {

        // regex bases
        private static final String rgb = "(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
        private static final String hue = "(\\d|[1-9]\\d|[1-2]\\d\\d|3[0-5]\\d|360)";
        private static final String sv = "(\\d|[1-9]\\d|100)";

        // hex regexes
        private static final String hexRegex = "^#?(([a-zA-Z0-9]?){6})$";

        // rgb regexes
        private static final String rgbCodeRegex = "^" + rgb + "$";
        private static final String redRegex = "^(r:|r)";
        private static final String greenRegex = "^(g:|g)";
        private static final String blueRegex = "^(b:|b)";

        // hsv regexes
        private static final String hueCodeRegex    = "^" + hue + "$";
        private static final String svCodeRegex = "^" + sv + "$";
        private static final String hueRegex = "^(h:|h)";
        private static final String saturationRegex = "^(s:|s)";
        private static final String valueRegex = "^(v:|v)";

        // full color codes
        // hex

        private static final String fullHexCode = "#?(([a-zA-Z0-9]){6})";

        // rgb

        private static final String fullRgbCode = "\s*rgb\\(\s*r:\s*" + rgb + "\s*,\s*g:\s*" + rgb + "\s*,\s*b:\s*" + rgb + "\s*\\)\s*";
        private static final String fullRedCode = "^((r:)?" + rgb + ")$";
        private static final String fullGreenCode = "^((g:)?" + rgb + ")$";
        private static final String fullBlueCode = "^((b:)?" + rgb + ")$";

        // hsv

        private static final String fullHsvCode = "\s*hsv\\(\s*h:\s*" + hue + "\s*,\s*s:\s*" + sv + "\s*,\s*v:\s*" + sv + "\s*\\)\s*";
        private static final String fullHueCode = "^((h:)?" + hue + ")$";
        private static final String fullSatCode = "^((s:)?" + sv + ")$";
        private static final String fullValueCode = "^((v:)?" + sv + ")$";


        public static UnaryOperator<TextFormatter.Change> hexValidation = change -> {

            // gets the text which is already present and the proposed change
            final String text = change.getText();
            final String baseText = change.getControlText();

            // gets the full text, discarding color unit
            final String unselectedText = baseText.substring(0, change.getRangeStart())
                                          + baseText.substring(change.getRangeEnd());
            final String fullText = unselectedText + text;
            final String colorCode = removeUnit(fullText, "#");

            // checks the validity of the color code
            if (!validateColorCode(colorCode, hexRegex)) return inValidateChange(change);

            return change;

        };

        public static UnaryOperator<TextFormatter.Change> redValidation = change -> {
            if (validateColor(change, ColorType.RED)) return change;
            else return inValidateChange(change);
        };

        public static UnaryOperator<TextFormatter.Change> greenValidation = change -> {
            if (validateColor(change, ColorType.GREEN)) return change;
            else return inValidateChange(change);
        };

        public static UnaryOperator<TextFormatter.Change> blueValidation = change -> {
            if (validateColor(change, ColorType.BLUE)) return change;
            else return inValidateChange(change);
        };

        public static UnaryOperator<TextFormatter.Change> hueValidation = change -> {
            if (validateColor(change, ColorType.HUE)) return change;
            else return inValidateChange(change);
        };

        public static UnaryOperator<TextFormatter.Change> saturationValidation = change -> {
            if (validateColor(change, ColorType.SATURATION)) return change;
            else return inValidateChange(change);
        };

        public static UnaryOperator<TextFormatter.Change> valueValidation = change -> {
            if (validateColor(change, ColorType.VALUE)) return change;
            else return inValidateChange(change);
        };

        // partial validation
        public static boolean isPartialHex(final String hexCode) {
            return hexCode.matches(hexRegex);
        }

        public static boolean isPartialRgb(final String rgbCode) {
            return rgbCode.replace(redRegex, "")
                          .replace(greenRegex, "")
                          .replace(blueRegex, "")
                          .matches(rgbCodeRegex);
        }

        public static boolean isPartialHue(final String hueCode) {
            return hueCode.replace(hueRegex, "")
                          .matches(hueCodeRegex);
        }

        public static boolean isPartialSv(final String svCode) {
            return svCode.replace(saturationRegex, "")
                         .replace(valueRegex, "")
                         .matches(svCodeRegex);
        }

        // full validation
        // hex
        public static boolean isValidHex(final String hexCode) {
            return hexCode.matches(fullHexCode);
        }

        // rgb
        public static boolean isValidRgb(final String rgbCode) {
            return rgbCode.matches(fullRgbCode);
        }

        public static boolean isValidRed(final String redCode) {
            return redCode.matches(fullRedCode);
        }

        public static boolean isValidGreen(final String greenCode) {
            return greenCode.matches(fullGreenCode);
        }

        public static boolean isValidBlue(final String blueCode) {
            return blueCode.matches(fullBlueCode);
        }

        // hsv
        public static boolean isValidHsv(final String hsvCode) {
            return hsvCode.matches(fullHsvCode);
        }

        public static boolean isValidHue(final String hueCode) {
            return hueCode.matches(fullHueCode);
        }

        public static boolean isValidSaturation(final String saturationCode) {
            return saturationCode.matches(fullSatCode);
        }

        public static boolean isValidValue(final String valueCode) {
            return valueCode.matches(fullValueCode);
        }

        private static boolean validateColor(final TextFormatter.Change change, final ColorType colorType) {

            final String baseTextKey = switch (colorType) {
                case RED        -> "r";
                case GREEN      -> "g";
                case BLUE       -> "b";
                case HUE        -> "h";
                case SATURATION -> "s";
                case VALUE      -> "v";
                default         -> throw new IllegalStateException("Unexpected value: " + colorType);
            };

            final String colorRegex = switch (colorType) {
                case RED        -> redRegex;
                case GREEN      -> greenRegex;
                case BLUE       -> blueRegex;
                case HUE        -> hueRegex;
                case SATURATION -> saturationRegex;
                case VALUE      -> valueRegex;
                default         -> throw new IllegalStateException("Unexpected value: " + colorType);
            };

            // gets the text already present and the proposed change
            final String text = change.getText();
            final String baseText = change.getControlText();

            // if user has started writing out units they must finish before writing the color code
            if (baseText.equals(baseTextKey) && !text.equals(":") && !text.equals("")) return false;

            // gets the full text, discarding color unit
            final String unselectedText = baseText.substring(0, change.getRangeStart())
                                          + baseText.substring(change.getRangeEnd());
            final String fullText = unselectedText + text;
            final String colorCode = removeUnit(fullText, colorRegex);

            // checks the validity of the text
            return switch (colorType) {
                case RED, GREEN, BLUE  -> validateColorCode(colorCode, rgbCodeRegex);
                case HUE               -> validateColorCode(colorCode, hueCodeRegex);
                case SATURATION, VALUE -> validateColorCode(colorCode, svCodeRegex);
                default                -> throw new IllegalStateException("Unexpected value: " + colorType);
            };
        }

        private static String removeUnit(final String string, final String regex) {
            return string.replaceFirst(regex, "");
        }

        private static boolean validateColorCode(final String text, final String regex) {
            // if only units were present, change is valid
            if (text.equals("")) return true;

            // if other text besides the units was present, checks its validity
            return text.matches(regex);
        }

        private static TextFormatter.Change inValidateChange(final TextFormatter.Change change) {
            change.setText("");
            change.setRange(change.getRangeStart(), change.getRangeStart());
            return change;
        }
    }

    public static void main(String[] args) {
        // rgb(r:255, g:0, b:0)

        System.out.println(Arrays.toString(ColorUtil.Hsv.fromHsvString("hsv(h:0, s:100, v:100)")));
        System.out.println(Arrays.toString(ColorUtil.Rgb.fromRgbString("rgb(r:255, g:0, b:0)")));
    }
}
