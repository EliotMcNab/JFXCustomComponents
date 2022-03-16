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
 * A {@link javafx.scene.canvas.Canvas Canvas}-based {@link Control} which allows user
 * to point to a desired color in the hsv spectrum<br>
 * <br>
 * <u><i>CSS Pseudo-class</i></u> : hsv-color-select<br>
 * <br>
 * <u><i>Substructure</i></u>
 * <ul>
 *     <li>pointer: {@link Region}</li>
 * </ul>
 * <u><i>Features</i></u> :<br>
 * <ul>
 *     <li>allows to select a color inside of the Hsv spectrum</li>
 *     <li>can retrieve current spectrum color (top-left of the current spectrum)</li>
 *     <li>can retrieve current pointer color (currently selected color)</li>
 *     /!\ <i>hsv spectrum color</i> is not the same as <i>pointer color</i>, which corresponds to the current color
 * </ul>
 * @implNote requires an external control, such as a {@link javafx.scene.control.Slider Slider} or
 * {@link app.customControls.controls.loopSlider.LoopSlider LoopSlider} to be able to select the slice of the hsv
 * spectrum to view. The same is true for changing the spectrum's value, since the HsvColorSelect only displays a 2-way
 * gradient along the hue and saturation axis. Therefore, the spectrum's value must also be set from the exterior
 */
public class HsvColorSelect extends Control {

    // ===================================
    //              FIELDS
    // ===================================

    /*           DEFAULT VALUES         */
    private final double DEFAULT_HUE        = 0;
    private final double DEFAULT_SATURATION = 100;
    private final double DEFAULT_VALUE = 100;

    /*     CSS STYLEABLE PROPERTIES     */
    private static final StyleablePropertyFactory<HsvColorSelect> FACTORY =
            new StyleablePropertyFactory<>(Control.getClassCssMetaData());

    /*         OBJECT PROPERTIES        */
    private final SimpleDoubleProperty hue;
    private final SimpleDoubleProperty saturation;
    private final SimpleDoubleProperty value;

    /*              MOVEMENT            */

    private final Region pointer;
    private final MovementHandler pointerMovement;

    /*            GENERATION            */
    private boolean generated = false;

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    /**
     * {@link HsvColorSelect} constructor
     * @param width (double): initial color select width
     * @param height (double): initial color select height
     */
    public HsvColorSelect(final double width, final double height) {

        // sets the initial color select size
        setWidth(width);
        setHeight(height);

        // initialises properties
        this.hue = new SimpleDoubleProperty(this, "hue", DEFAULT_HUE);
        this.saturation = new SimpleDoubleProperty(this, "saturation", DEFAULT_SATURATION);
        this.value = new SimpleDoubleProperty(this, "value", DEFAULT_VALUE);

        // initialises components
        this.pointer = new Region();
        this.pointerMovement = new MovementHandler(pointer);

        // sets up the pointer movement handler
        pointerMovement.setCentered(true);
    }

    // ===================================
    //           INITIALISATION
    // ===================================

    /**
     * Determines if the {@link HsvColorSelect} has finished generating yet
     * @return (boolean): whether the HsvColorSelect has finished generating yet
     */
    private boolean hasGenerated() {
        return generated;
    }

    /**
     * Marks the {@link HsvColorSelect} as having generated
     */
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
    //             POINTER
    // ===================================

    /**
     * Getter for the hsv pointer's hue [0; 359]
     * @return (double): the pointer's hue
     */
    public double getPointerHue() {

        // if the HsvColorSelect has not finished generating, returns the default hue instead
        if (!hasGenerated()) return DEFAULT_HUE;

        // determines pointer x position
        final double x = getPointerX();
        // calculates the pointer's progression along the color selector
        final double width = getWidth();
        final double xProgression = x / width;

        return (getHue() + xProgression * 60) % 360;

    }

    /**
     * Setter for the hsv pointer's hue
     * @param hue (double): new pointer hue [0; 360]
     */
    public void setPointerHue(final double hue) {

        // checks the new hue is in the interval [0; 360]
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

    /**
     * Getter for the hsv pointer's saturation [0; 100]
     * @return (double): the hev pointer's saturation
     */
    public double getPointerSaturation() {

        // if the HsvColorSelect has not finished generating, returns the default saturation instead
        if (!hasGenerated()) return DEFAULT_SATURATION;

        // determines pointer y position
        final double y = getPointerY();
        // calculates the
        final double height = getHeight();

        return y / height * 100;

    }

    /**
     * Setter for the hsv pointer's saturation
     * @param saturation (double): new pointer saturation [0; 100]
     */
    public void setPointerSaturation(final double saturation) {

        // checks if the specified saturation is in the range [0; 100]
        if(!validateSaturation(saturation)) return;

        // calculates the pointer's new position
        final double height = getHeight();
        final double sProgress = (height / 100.0) * (100 - saturation);

        // moves the pointer to the correct destination
        pointerMovement.moveToY(sProgress);

    }

    /**
     * Hsv pointer layoutXProperty (changes when the pointer moves along the x-axis)
     * @return (DoubleProperty): the hsv pointer's layoutXProperty
     */
    public DoubleProperty pointerLayoutXProperty() {
        return pointer.layoutXProperty();
    }

    /**
     * Hsv pointer layoutYProperty (changes when the pointer moves along the y-axis)
     * @return (DoubleProperty): the hsv pointer's layoutYProperty
     */
    public DoubleProperty pointerLayoutYProperty() {
        return pointer.layoutYProperty();
    }

    /**
     * Getter for the hsv pointer's x coordinates
     * @return (double): hsv pointer's x coordinates
     */
    public double getPointerX() {
        return pointerMovement.getPointerX();
    }

    /**
     * Getter for the hsv pointer's y coordinates
     * @return (double): hsv pointer's y coordinates
     */
    public double getPointerY() {
        // gets the pointer's y coordinates
        final double pointerY = getHeight() - pointerMovement.getPointerY();
        // accounts for discrepancies in the HsvColorSelect height
        // (would otherwise result in negative y-coordinates from time to time)
        return pointerY >= 0 ? pointerY : 0;
    }

    /**
     * Getter for the hsv pointer's coordinates
     * @return (Point2D): hsv pointer's current coordinates
     */
    public Point2D getPointerPosition() {
        return new Point2D(getPointerX(), getPointerY());
    }

    // ===================================
    //             ACCESSORS
    // ===================================

    /**
     * Setter for the {@link HsvColorSelect}'s color
     * @param color ({@link Color}): new HsvColorSelect color
     */
    public void setColor(final Color color) {

        // extracts the red, green and blue values from the color
        final int r = (int) (color.getRed() * 255);
        final int g = (int) (color.getGreen() * 255);
        final int b = (int) (color.getBlue() * 255);

        // converts the color to hsv
        final double[] hsv = ColorUtil.Rgb.toHsv(r, g, b);

        // sets the HsvColorSelect color to the equivalent hsv color
        setHue(hsv[0]);
        setSaturation(hsv[1]);
        setValue(hsv[2]);
    }

    /**
     * Hue {@link javafx.beans.property.Property Property} for the {@link HsvColorSelect}
     * (updates when the pointer's hue changes)
     * @return (DoubleProperty): HsvColorSelect hue property
     */
    public DoubleProperty hueProperty() {
        return hue;
    }

    /**
     * Getter for the {@link HsvColorSelect}'s hue
     * @return (double): the HsvColorSelect's <strong>current spectrum hue</strong>
     * @implNote do not mistake this with <u><i>getPointerHue</i></u>, which returns the hue at the
     * <strong>current pointer's position</strong>
     */
    public double getHue() {
        return hue.get();
    }

    /**
     * Setter for the {@link HsvColorSelect}'s hue
     * @param newHue (double): new HsvColorSelect hue [0; 360]
     */
    public void setHue(final double newHue) {
        // makes suer the new hue is in the interval [0; 360]
        if (!validateHue(newHue)) return;
        // if hue is valid, sets it
        hue.set(newHue);
    }

    /**
     * Checks if specified hue is valid (belongs to interval [0;360]
     * @param hue (double): hue to check
     * @return (boolean): whether hue belongs to [0; 360]
     */
    private boolean validateHue(final double hue) {
        return hue >= 0 && hue <= 360;
    }

    /**
     * Saturation {@link javafx.beans.property.Property Property} for the {@link HsvColorSelect}
     * (updates when the pointer's saturation changes)
     * @return (DoubleProperty): HsvColorSelect saturation property
     */
    public DoubleProperty saturationProperty() {
        return saturation;
    }

    /**
     * Getter for the {@link HsvColorSelect}'s saturation
     * @return (double): the HsvColorSelect's <strong>current spectrum saturation</strong>
     * @implNote do not mistake this with <u><i>getPointerSaturation</i></u>, which returns the saturation at the
     * <strong>current pointer's position</strong>
     */
    public double getSaturation() {
        return saturation.get();
    }

    /**
     * Sets the {@link HsvColorSelect}'s saturation
     * @param newSaturation (double): the new HsvColorSelect saturation [0; 100]
     */
    public void setSaturation(final double newSaturation) {
        // makes sure the saturation is in the interval [0; 100]
        if (!validateSaturation(newSaturation)) return;
        // if saturation is valid, sets it
        saturation.set(newSaturation);
    }

    /**
     * Makes sure the specified saturation is in the range [0; 100]
     * @param saturation (double): the saturation to check
     * @return (boolean): whether the saturation is in the interval [0; 100]
     */
    public boolean validateSaturation(final double saturation) {
        return saturation >= 0 && saturation <= 100;
    }

    /**
     * Value {@link javafx.beans.property.Property Property} for the {@link HsvColorSelect}
     * (updates when the pointer's value changes)
     * @return (DoubleProperty): HsvColorSelect value property
     */
    public DoubleProperty valueProperty() {
        return value;
    }

    /**
     * Getter for the {@link HsvColorSelect}'s value
     * @return (double): the hsvColorSelect's current value
     */
    public double getValue() {
        return value.get();
    }

    /**
     * Setter for the {@link HsvColorSelect}'s value
     * @param newValue (double): the HsvColorSelect's new value [0; 100]
     */
    public void setValue(final double newValue) {
        // makes sure the new value is in the interval [0; 100]
        if (!validateValue(newValue)) return;
        // if the new value is valid, sets it
        value.set(newValue);
    }

    /**
     * Checks to see if the specified value is in the interval [0; 100]
     * @param newValue (double): the value to check
     * @return (boolean): whether the value is the interval [0; 100]
     */
    private boolean validateValue(final double newValue) {
        return newValue >= 0 && newValue <= 100;
    }

    // ===================================
    //           COLOR GETTERS
    // ===================================

    /**
     * Getter for the {@link HsvColorSelect}'s hsv code
     * @return (double[]): the HsvColorSelect's <strong>current spectrum hsv code</strong>
     * @implNote do not mistake this with <u><i>getPointerHsv</i></u>, which gets the hsv color at the
     * <strong>current pointer position</strong>
     */
    public double[] getHsv() {
        return new double[]{getHue(), getSaturation(), getValue()};
    }

    /**
     * Getter for the {@link HsvColorSelect}'s <strong>pointer</strong> hsv code
     * @return (double[]): the hsv color at the <strong>current pointer position</strong>
     */
    public double[] getPointerHsv() {
        return new double[]{getPointerHue(), getPointerSaturation(), getValue()};
    }

    /**
     * Getter for the {@link HsvColorSelect}'s rgb code
     * @return (int[]): the HsvColorSelect's <strong>current spectrum rgb code</strong>
     * @implNote do not mistake this with <u><i>getPointerRgb</i></u>, which gets the rgb color at the
     * <strong>current pointer position</strong>
     */
    public int[] getRgb() {
        final double[] hsv = getHsv();
        return ColorUtil.Hsv.toRgb(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Getter for the {@link HsvColorSelect}'s <strong>pointer</strong> rgb code
     * @return (int[]): the rgb color at the <strong>current pointer position</strong>
     */
    public int[] getPointerRgb() {
        final double[] hsv = getPointerHsv();
        return ColorUtil.Hsv.toRgb(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Getter for the {@link HsvColorSelect}'s hex code
     * @return (String): the HsvColorSelect's <strong>current spectrum hex code</strong>
     * @implNote do not mistake this with <u><i>getPointerHex</i></u>, which gets the hex color at the
     * <strong>current pointer position</strong>
     */
    public String getHex() {
        final double[] hsv = getHsv();
        return ColorUtil.Hsv.toHex(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Getter for the {@link HsvColorSelect}'s <strong>pointer</strong> hex code
     * @return (String): the hex color at the <strong>current pointer position</strong>
     */
    public String getPointerHex() {
        final double[] hsv = getPointerHsv();
        return ColorUtil.Hsv.toHex(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Getter for the {@link HsvColorSelect}'s current {@link Color}
     * @return (Color): the HsvColorSelect's <strong>current spectrum color</strong>
     * @implNote do not mistake this with <u><i>getPointerColor</i></u>, which gets the color at the
     * <strong>current pointer position</strong>
     */
    public Color getColor() {
        final int[] rgb = getRgb();
        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Getter for the {@link HsvColorSelect}'s <strong>pointer</strong> color
     * @return (Color): the color at the <strong>current pointer position</strong>
     */
    public Color getPointerColor() {
        final int[] rgb = getPointerRgb();
        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    // ===================================
    //           COLOR SETTERS
    // ===================================

    /**
     * Setter for the currently selected color (where the pointer points at)
     * @param color ({@link Color}); new selected color
     */
    public void setPointerColor(final Color color) {

        // extracts the red, green & blue elements from the color
        final int r = (int) (color.getRed() * 255);
        final int g = (int) (color.getGreen() * 255);
        final int b = (int) (color.getBlue() * 255);

        // sets the pointer to point to the color
        setPointerRgb(r, g, b);
    }

    /**
     * Setter for the currently selected color (where the pointer points at)
     * @param hex ({@link String}); hex code of the new selected color
     */
    public void setPointerHex(final String hex) {
        // converts hex color code to hsv values
        final double[] hsv = ColorUtil.Hex.toHsv(hex);

        // updates the pointer's position accordingly
        setPointerTo(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Setter for the currently selected color (where the pointer points at)
     * @param red (int): red component of the currently selected color [0; 255]
     * @param green (int): green component of the currently selected color [0; 255]
     * @param blue (int): blue component of the currently selected color [0; 255]
     */
    public void setPointerRgb(final int red, final int green, final int blue) {
        // converts rgb color to hsv values
        final double[] hsv = ColorUtil.Rgb.toHsv(red, green, blue);

        // updates the pointer's position accordingly
        setPointerTo(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Setter for the currently selected color (where the pointer points at)
     * @param hue (double): hue component of the currently selected color [0; 360]
     * @param saturation (double): saturation component of the currently selected color [0; 100]
     * @param value (double): value component of the currently selected color [0; 100]
     */
    public void setPointerHsv(final double hue, final double saturation, final double value) {
        setPointerTo(hue, saturation, value);
    }

    /**
     * Moves the pointer to the specified hsv color
     * @param h (double): hue component of the currently selected color [0; 360]
     * @param s (double): saturation component of the currently selected color [0; 100]
     * @param v (double): value component of the currently selected color [0; 100]
     */
    private void setPointerTo(final double h, final double s, final double v) {
        setPointerHue(h);
        setPointerSaturation(s);
        setValue(v);
    }

}
