package app.customControls.controls.colorPicker;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;

/**
 * An HSV color picker based on material design with support for hex, rgb and hsv color formats<br>
 * <br>
 * <u><i>CSS Pseudo-class</i></u> : color-picker<br>
 * <br>
 * <u><i>Substructure</i></u> : <br>
 * <ul>
 *     <li>color-picker: {@link Control}</li>
 *     <ul>
 *         <li>current-color: {@link javafx.scene.layout.Region Region}</li>
 *         <li>hue-slider: {@link app.customControls.controls.loopSlider.LoopSlider LoopSlider}</li>
 *         <li>value-slider: {@link app.customControls.controls.loopSlider.LoopSlider LoopSlider}</li>
 *     </ul>
 * </ul>
 * <u><i>Features</i></u> :<br>
 * <br>
 * <ul>
 *     <li>text input validation for hex, rgb and hsv color formats</li>
 *     <li>error messages for invalid color input</li>
 *     <li>functional copy and paste which takes into account text validation</li>
 *     <li>notification message telling user when a color code has been copied</li>
 *     <li>
 *         smart copy by pressing CTRL-SHIFT-C, copies entire color code<br>
 *         <i>ex: rgb(r:255, g:0, b:0)</i>
 *     </li>
 *     <li>
 *         smart paste recognises the color format being pasted and automatically transition to it
 *         if the pasted text is valid
 *     </li>
 *     <li>
 *         color selection either by pointing / clicking on the Hsv spectrum
 *         or by adjusting the hue and value sliders
 *     </li>
 *     <li>
 *         functional drop tool, allowing user to select any color on the screen
 *         (excluding the taskbar for the moment)
 *     </li>
 *     <li>
 *         supports resizing, although there are still some performance issues at high resolutions
 *         due to poor utilisation of the Canvas
 *     </li>
 * </ul>
 *
 */
public class MaterialColorPicker extends Control {

    // ===================================
    //              FIELDS
    // ===================================

    /*          DEFAULT VALUES          */
    private static final Color DEFAULT_COLOR              = Color.RED;
    private static final ColorFormat DEFAULT_COLOR_FORMAT = ColorFormat.HEX;

    /*            PROPERTIES            */
    private final SimpleObjectProperty<Color> color;
    private final SimpleObjectProperty<ColorFormat> colorFormat;

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    /**
     * Default {@link MaterialColorPicker} constructor
     */
    public MaterialColorPicker() {
        this(DEFAULT_COLOR);
    }

    /**
     * {@link MaterialColorPicker} constructor
     * @param initialColor ({@link Color}): starting color for the color picker
     */
    public MaterialColorPicker(Color initialColor) {
        this(initialColor, DEFAULT_COLOR_FORMAT);
    }

    /**
     * {@link MaterialColorPicker} constructor
     * @param initialColor ({@link Color}): starting color for the color picker
     * @param initialColorFormat ({@link ColorFormat}): starting color format for the color picker
     */
    public MaterialColorPicker(Color initialColor, ColorFormat initialColorFormat) {

        color = new SimpleObjectProperty<>(this, "color", DEFAULT_COLOR);
        colorFormat = new SimpleObjectProperty<>(this, "colorFormat", DEFAULT_COLOR_FORMAT);

        // saves the starting color & color format to the color picker
        setColor(initialColor);
        setColorFormat(initialColorFormat);
    }

    // ===================================
    //               STYLE
    // ===================================

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MaterialColorPickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return MaterialColorPicker.class.getResource("/app/customControls/style/color-picker.css").toExternalForm();
    }

    // ===================================
    //            PROPERTIES
    // ===================================

    /**
     * Property for the {@link MaterialColorPicker}'s current color
     * @return (ObjectProperty(Color)): the color picker's associated color property
     */
    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    /**
     * Property for the {@link MaterialColorPicker}'s color format
     * @return (ObjectProperty(Color)): the color picker's associated color format
     */
    public ObjectProperty<ColorFormat> colorFormatProperty() {
        return colorFormat;
    }

    // ===================================
    //              SETTERS
    // ===================================

    /**
     * Setter for the {@link MaterialColorPicker}'s color
     * @param newColor ({@link Color}): the color picker's new color
     */
    public void setColor(final Color newColor) {
        color.set(newColor);
    }

    /**
     * Setter for the {@link MaterialColorPicker}'s color format
     * @param newColorFormat ({@link ColorFormat}): the color picker's new color format
     */
    public void setColorFormat(final ColorFormat newColorFormat) {
        colorFormat.set(newColorFormat);
    }

    // ===================================
    //              GETTERS
    // ===================================

    /**
     * Getter for the {@link MaterialColorPicker}'s color
     * @return (Color): the color picker's current color
     */
    public Color getColor() {
        return color.get();
    }

    /**
     * Getter for the {@link MaterialColorPicker}'s color format
     * @return (ColorFormat): the color picker's current color format
     */
    public ColorFormat getColorFormat() {
        return colorFormat.get();
    }


}
