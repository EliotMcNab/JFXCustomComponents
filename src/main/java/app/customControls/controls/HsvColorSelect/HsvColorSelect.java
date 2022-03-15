package app.customControls.controls.HsvColorSelect;

import app.customControls.handlers.movementHandler.MovementHandler;
import app.customControls.utilities.ColorUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Point2D;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * A {@link javafx.scene.canvas.Canvas Canvas}-based control which allows user
 * to point to a desired color in the hsv spectrum
 */
public class HsvColorSelect extends Control {

    // ===================================
    //              FIELDS
    // ===================================

    /*           DEFAULT VALUES         */
    private final double DEFAULT_HUE        = 0;
    private final double DEFAULT_SATURATION = 100;
    private final double DEFAULT_VALUE      = 100;

    /*     CSS STYLEABLE PROPERTIES     */
    private static final StyleablePropertyFactory<HsvColorSelect> FACTORY =
            new StyleablePropertyFactory<>(Control.getClassCssMetaData());

    /*         OBJECT PROPERTIES        */
    private final SimpleDoubleProperty hue;
    private final SimpleDoubleProperty saturation;
    private final SimpleDoubleProperty value;

    /*              MOVEMENT            */
    private final Region pointer = new Region();
    private final MovementHandler pointerMovement = new MovementHandler(pointer);

    /*            GENERATION            */
    private boolean generated = false;

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    public HsvColorSelect(final double width, final double height) {

        setWidth(width);
        setHeight(height);

        hue = new SimpleDoubleProperty(this, "hue", DEFAULT_HUE);
        saturation = new SimpleDoubleProperty(this, "saturation", DEFAULT_SATURATION);
        value = new SimpleDoubleProperty(this, "value", DEFAULT_VALUE);

        pointerMovement.setCentered(true);
    }

    // ===================================
    //           INITIALISATION
    // ===================================

    private boolean hasGenerated() {
        return generated;
    }

    public void validateGeneration() {
        generated = true;
    }

    // ===================================
    //              STYLING
    // ===================================

    @Override
    protected Skin<?> createDefaultSkin() {
        return new HsvColorSelectSkin(this, pointer, pointerMovement);
    }

    @Override
    public String getUserAgentStylesheet() {
        return HsvColorSelect.class.getResource("/app/customControls/style/hsv-color-select.css").toExternalForm();
    }

    // ===================================
    //             MOVEMENT
    // ===================================

    public double getPointerHue() {

        if (!hasGenerated()) return DEFAULT_HUE;

        // determines pointer x position
        final double x = getPointerX();
        // calculates the pointer's progression along the color selector
        final double width = getWidth();
        final double xProgression = x / width;

        return (getHue() + xProgression * 60) % 360;

    }

    public void setPointerHue(final double hue) {

        if (!validateHue(hue)) return;

        // checks if the hue is not already accessible from the current sextant
        final boolean accessibleHue = hue >= getHue() && hue <= getHue() + 60;

        // determines which sextant the hue is in and moves to it
        final double sextant = accessibleHue ? getHue() : ColorUtil.Hsv.getSextant((int) hue) * 60;
        setHue(sextant);

        // determines the distance between the new sextant and the target hue
        final double deltaH = hue - sextant;

        // calculates the pointer's new position
        final double width = getWidth();
        final double hProgress = width / 60 * deltaH;

        // moves the pointer to the correct destination
        pointerMovement.moveToX(hProgress);
    }

    public double getPointerSaturation() {

        if (!hasGenerated()) return DEFAULT_SATURATION;

        // determines pointer y position
        final double y = getPointerY();
        // calculates the
        final double height = getHeight();

        return y / height * 100;

    }

    public void setPointerSaturation(final double saturation) {

        if(!validateSaturation(saturation)) return;

        // calculates the pointer's new position
        final double height = getHeight();
        final double sProgress = (height / 100.0) * (100 - saturation);

        // moves the pointer to the correct destination
        pointerMovement.moveToY(sProgress);

    }

    public DoubleProperty pointerLayoutXProperty() {
        return pointer.layoutXProperty();
    }

    public DoubleProperty pointerLayoutYProperty() {
        return pointer.layoutYProperty();
    }

    public double getPointerX() {
        return pointerMovement.getPointerX();
    }

    public double getPointerY() {
        final double pointerY = getHeight() - pointerMovement.getPointerY();
        return pointerY >= 0 ? pointerY : 0;
    }

    public Point2D getPointerPosition() {
        return new Point2D(getPointerX(), getPointerY());
    }

    // ===================================
    //             ACCESSORS
    // ===================================

    // color
    public void setColor(final Color color) {
        final int r = (int) (color.getRed() * 255);
        final int g = (int) (color.getGreen() * 255);
        final int b = (int) (color.getBlue() * 255);

        final int[] rgb = new int[]{r, g, b};
        final double[] hsv = ColorUtil.Rgb.toHsv(rgb[0], rgb[1], rgb[2]);

        setHue(hsv[0]);
        setSaturation(hsv[1]);
        setValue(hsv[2]);
    }

    // hue
    public DoubleProperty hueProperty() {
        return hue;
    }

    public double getHue() {
        return hue.get();
    }

    public void setHue(final double newHue) {
        if (!validateHue(newHue)) return;
        hue.set(newHue);
    }

    private boolean validateHue(final double hue) {
        return hue >= 0 && hue <= 360;
    }

    // saturation
    public DoubleProperty saturationProperty() {
        return saturation;
    }

    public double getSaturation() {
        return saturation.get();
    }

    public void setSaturation(final double newSaturation) {
        if (!validateSaturation(newSaturation)) return;
        saturation.set(newSaturation);
    }

    public boolean validateSaturation(final double saturation) {
        return saturation >= 0 && saturation <= 100;
    }

    // value
    public DoubleProperty valueProperty() {
        return value;
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(final double newValue) {
        if (!validateValue(newValue)) return;
        value.set(newValue);
    }

    private boolean validateValue(final double newValue) {
        return newValue >= 0 && newValue <= 100;
    }

    // color codes
    public double[] getHsv() {
        return new double[]{getHue(), getSaturation(), getValue()};
    }

    public double[] getPointerHsv() {
        return new double[]{getPointerHue(), getPointerSaturation(), getValue()};
    }

    public int[] getRgb() {
        final double[] hsv = getHsv();
        return ColorUtil.Hsv.toRgb(hsv[0], hsv[1], hsv[2]);
    }

    public int[] getPointerRgb() {
        final double[] hsv = getPointerHsv();
        return ColorUtil.Hsv.toRgb(hsv[0], hsv[1], hsv[2]);
    }

    public String getHex() {
        final double[] hsv = getPointerHsv();
        return ColorUtil.Hsv.toHex(hsv[0], hsv[1], hsv[2]);
    }

    public String getPointerHex() {
        final double[] hsv = getPointerHsv();
        return ColorUtil.Hsv.toHex(hsv[0], hsv[1], hsv[2]);
    }

    public Color getColor() {
        final int[] rgb = getRgb();
        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    public Color getPointerColor() {
        final int[] rgb = getPointerRgb();
        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    // ===================================
    //              POINTER
    // ===================================

    public void setPointerColor(final Color color) {
        final int r = (int) (color.getRed() * 255);
        final int g = (int) (color.getGreen() * 255);
        final int b = (int) (color.getBlue() * 255);

        final int[] rgb = new int[]{r, g, b};
        setPointerRgb(rgb[0], rgb[1], rgb[2]);
    }

    public void setPointerHex(final String hex) {
        // converts hex color code to hsv values
        final double[] hsv = ColorUtil.Hex.toHsv(hex);

        // updates the pointer's position accordingly
        setPointerTo(hsv[0], hsv[1], hsv[2]);
    }

    public void setPointerRgb(final int red, final int green, final int blue) {
        // converts rgb color to hsv values
        final double[] hsv = ColorUtil.Rgb.toHsv(red, green, blue);

        // updates the pointer's position accordingly
        setPointerTo(hsv[0], hsv[1], hsv[2]);
    }

    public void setPointerHsv(final double hue, final double saturation, final double value) {
        setPointerTo(hue, saturation, value);
    }

    private void setPointerTo(final double h, final double s, final double v) {
        setPointerHue(h);
        setPointerSaturation(s);
        setValue(v);
    }

}
