package app.customControls.controls.colorPicker;

import app.customControls.controls.HsvColorSelect.HsvColorSelect;
import app.customControls.controls.colorpickerOverlay.ColorPickerOverlay;
import app.customControls.controls.loopSlider.LoopSlider;
import app.customControls.controls.temporaryPopup.TemporaryPopup;
import app.customControls.utilities.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;

import static app.customControls.controls.colorPicker.ColorFormat.*;
import static app.customControls.controls.colorPicker.ColorFormat.HEX;
import static app.customControls.utilities.KeyboardUtil.Letters.*;
import static app.customControls.utilities.KeyboardUtil.Modifier.CTRL;
import static app.customControls.utilities.KeyboardUtil.Modifier.SHIFT;
import static app.customControls.utilities.KeyboardUtil.areKeysDown;

public class MaterialColorPickerSkin extends SkinBase<MaterialColorPicker> implements Skin<MaterialColorPicker> {

    // ===================================
    //               FIELDS
    // ===================================

    /*               STATIC             */

    // default values

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 320;
    private static final int DEFAULT_BOTTOM_SPACE = 90;
    private static final int TOP_SPACE = 180;
    private static final String COLOR_PICKER_ICON = "fas-eye-dropper";

    // default color code

    private static final String DEFAULT_HEX  = "#FF0000";
    private static final int[] DEFAULT_RGB  = new int[]{255, 0, 0};
    private static final int[] DEFAULT_HSV  = new int[]{0, 100, 100};

    // constants

    private static final String MARGIN_KEY = "color-picker-margin";
    private static final int MIN_HUE = 0;
    private static final int MAX_HUE = 360;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 100;
    private static final long POPUP_DURATION = 1000;

    /*              PADDING             */

    private static final Insets pickerMargins = new Insets(0, 20, 0, 20);
    private static final Insets currentColorMargins = new Insets(0 , 20, 0 , 0);
    private static final Insets hueSliderMargins = new Insets(20, 10, 0, 0);
    private static final Insets valueSliderMargins = new Insets(0, 10, 0, 0);
    private static final Insets colorFormatMargins = new Insets(0, 10, 30, 10);
    private double bottomSpace;
    private double topSpace;

    /*             COMPONENTS           */

    // hsv spectrum display
    private final HsvColorSelect        hsvColorSelect;
    // color picking
    private final FontIcon              colorPickerIcon;
    private final Region currentColor;
    // overlay
    private final ColorPickerOverlay    overlay;
    // sliders
    private final LoopSlider            hueSlider;
    private final LoopSlider valueSlider;
    // color formats
    private final Pane                  formatPane;
    private final ComboBox<ColorFormat> colorFormat;
    // hex
    private final TextField             hexDisplay;
    // rgb
    private final TextField             redDisplay;
    private final TextField greenDisplay;
    private final TextField blueDisplay;
    private final TextField[] rgb;
    // hsv
    private final TextField             hueDisplay;
    private final TextField saturationDisplay;
    private final TextField valueDisplay;
    private final TextField[] hsv;
    // copy notification
    private final TemporaryPopup        copyDisplay;

    /*            ASSOCIATED            */

    private final MaterialColorPicker colorPicker;

    /*             LISTENERS            */

    private final InvalidationListener resizeListener;          // handles resizing and repositioning components
    private final InvalidationListener formatListener;          // handles color format changes
    private final InvalidationListener colorListener;           // synchronises the color to the hsv pointer
    private final InvalidationListener hueListener;             // synchronises the color to the hue slider
    private final InvalidationListener valueListener;           // synchronises the color to the value slider
    private final InvalidationListener colorChangeListener;     // handles exterior color changes
    private final EventHandler<KeyEvent> colorKeyListener;        // handles key presses in color display textFields
    private final EventHandler<KeyEvent> globalKeyListener;       // handles key presses in other ColorPicker regions
    private final EventHandler<MouseEvent> overlayToggleListener;   // handles displaying the ColorPickerOverlay
    private final EventHandler<MouseEvent> onPointerRelease;        // handles synchronising color with the ColorPicker

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    protected MaterialColorPickerSkin(MaterialColorPicker colorPicker) {
        super(colorPicker);

        // saves the associated colorPicker
        this.colorPicker = colorPicker;

        /*      INITIALISING COMPONENTS     */

        // hsv spectrum
        this.hsvColorSelect = new HsvColorSelect(DEFAULT_WIDTH, TOP_SPACE);
        // color picking
        this.currentColor = new Region();
        this.colorPickerIcon = new FontIcon(COLOR_PICKER_ICON);
        // overlay
        this.overlay = new ColorPickerOverlay(colorPicker);
        // sliders
        this.hueSlider = new LoopSlider(MIN_HUE, MAX_HUE, MIN_HUE);
        this.valueSlider = new LoopSlider(MIN_VALUE, MAX_VALUE, MIN_VALUE, false);
        // color formats
        this.formatPane = new Pane();
        this.colorFormat = new ComboBox<>();
        // hex
        this.hexDisplay = new TextField();
        // rgb
        this.redDisplay = new TextField();
        this.greenDisplay = new TextField();
        this.blueDisplay = new TextField();
        this.rgb = new TextField[]{redDisplay, greenDisplay, blueDisplay};
        // hsv
        this.hueDisplay = new TextField();
        this.saturationDisplay = new TextField();
        this.valueDisplay = new TextField();
        this.hsv = new TextField[]{hueDisplay, saturationDisplay, valueDisplay};
        // copy
        this.copyDisplay = new TemporaryPopup("", POPUP_DURATION);

        /*             LISTENERS            */

        this.resizeListener = observable -> handleResize();
        this.formatListener = observable -> handleFormatChange();
        this.colorListener = observable -> synchroniseColorToPointer();
        this.hueListener = observable -> synchroniseDisplayHue();
        this.valueListener = observable -> synchroniseDisplayValue();
        this.colorChangeListener = observable -> loadPickerColor();
        this.colorKeyListener = this::handleColorFieldKeyPresses;
        this.globalKeyListener = this::handleGlobalKeyPresses;
        this.overlayToggleListener = mouseEvent -> showOverlay();
        this.onPointerRelease = mouseEvent -> savePickerColor();

        initialise();
        style();
        populate();

        registerListeners();

        Platform.runLater(this::handleResize);
    }

    // ===================================
    //             INITIALISE
    // ===================================

    private void initialise() {

        /*               SIZE               */

        // color picker size
        colorPicker.setMinHeight(DEFAULT_BOTTOM_SPACE + TOP_SPACE);
        colorPicker.setMinWidth(DEFAULT_WIDTH);
        colorPicker.setPrefWidth(DEFAULT_WIDTH);
        colorPicker.setPrefHeight(DEFAULT_HEIGHT);

        // current color size
        currentColor.setMaxSize(40, 40);
        currentColor.setPrefSize(40, 40);

         /*           STYLE CLASSES          */

        // color picker
        colorPicker.getStyleClass().setAll("color-picker");

        // color picking
        currentColor.getStyleClass().add("current-color");

        // hue slider
        hueSlider.getStyleClass().setAll("hue-slider");

        // value slider
        valueSlider.getStyleClass().setAll("value-slider");

        /*            COLOR FORMAT          */
        ObservableList<ColorFormat> colorFormats = FXCollections.observableArrayList(HEX, RGB, HSV);
        colorFormat.setItems(colorFormats);
        colorFormat.setValue(HEX);
        // hides rgb text fields
        hideRgb();
        // hides hsv text fields
        hideHsv();

        /*          TEXT VALIDATION         */
        // hex
        hexDisplay          .setTextFormatter(new TextFormatter<>(StringUtil.Validation.hexValidation));
        // rgb
        redDisplay          .setTextFormatter(new TextFormatter<>(StringUtil.Validation.redValidation));
        greenDisplay        .setTextFormatter(new TextFormatter<>(StringUtil.Validation.greenValidation));
        blueDisplay         .setTextFormatter(new TextFormatter<>(StringUtil.Validation.blueValidation));
        // hsv
        hueDisplay          .setTextFormatter(new TextFormatter<>(StringUtil.Validation.hueValidation));
        saturationDisplay   .setTextFormatter(new TextFormatter<>(StringUtil.Validation.saturationValidation));
        valueDisplay        .setTextFormatter(new TextFormatter<>(StringUtil.Validation.valueValidation));
    }

    private void style() {
        colorPicker.getStylesheets().add(colorPicker.getUserAgentStylesheet());
        currentColor.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.rgb(16, 16, 16), 10, .2, 2, 2));
    }

    private void populate() {

        formatPane.getChildren().addAll(
                colorFormat,
                hueDisplay,
                saturationDisplay,
                valueDisplay,
                redDisplay,
                greenDisplay,
                blueDisplay,
                hexDisplay
        );

        Pane container = new Pane();

        container.getChildren().addAll(
                hsvColorSelect,
                colorPickerIcon,
                currentColor,
                hueSlider,
                valueSlider,
                formatPane
        );

        getChildren().add(container);
    }

    // ===================================
    //             LISTENERS
    // ===================================

    private void registerListeners() {

        // resizing
        colorPicker.widthProperty().addListener(resizeListener);
        colorPicker.heightProperty().addListener(resizeListener);

        // overlay toggling
        colorPickerIcon.setOnMousePressed(overlayToggleListener);

        // color format changes
        colorFormat.valueProperty().addListener(formatListener);
        colorFormat.valueProperty().addListener(formatListener);

        // color changes
        colorPicker.colorProperty().addListener(colorChangeListener);

        // current color changes
        hsvColorSelect.pointerLayoutXProperty().addListener(colorListener);
        hsvColorSelect.pointerLayoutYProperty().addListener(colorListener);

        // color synchronisation with color picker
        hsvColorSelect.setOnMouseClicked(onPointerRelease);

        // sliders
        hueSlider.valueProperty().addListener(hueListener);
        valueSlider.valueProperty().addListener(valueListener);

        // user input (color key presses)
        hexDisplay.setOnKeyPressed(colorKeyListener);
        redDisplay.setOnKeyPressed(colorKeyListener);
        greenDisplay.setOnKeyPressed(colorKeyListener);
        blueDisplay.setOnKeyPressed(colorKeyListener);
        hueDisplay.setOnKeyPressed(colorKeyListener);
        saturationDisplay.setOnKeyPressed(colorKeyListener);
        valueDisplay.setOnKeyPressed(colorKeyListener);

        // user input (global key presses)
        colorPicker.setOnKeyPressed(globalKeyListener);
    }

    // ===================================
    //          SYNCHRONISATION
    // ===================================

    /**
     * Synchronises the hsv display to match the slider's hue
     */
    private void synchroniseDisplayHue() {
        hsvColorSelect.setHue(hueSlider.getValue());
        // updates the color code accordingly
        synchroniseColorToPointer();
    }

    /**
     * Synchronises the hsv display to match the slider's value
     */
    private void synchroniseDisplayValue() {
        hsvColorSelect.setValue(100 - valueSlider.getValue());
        // updates the color code accordingly
        synchroniseColorToPointer();
    }

    /**
     * Synchronises the sliders to match the hsv display's values
     */
    private void SynchroniseSliders() {
        hueSlider.setValue(hsvColorSelect.getHue());
        valueSlider.setValue(100 - hsvColorSelect.getValue());
    }

    /**
     * Synchronises the hsv display to match the values in the color format text fields
     * & is responsible for calling upon methods to verify the validity of user inout in these text fields
     * @param colorFormat ({@link ColorFormat}): the current color format
     * @apiNote if color format is invalid, resets color code to match the current color
     */
    private void synchroniseDisplayToTextFields(final ColorFormat colorFormat) {

        // checks the validity of the color format text before updating the display
        switch (colorFormat) {
            case HEX -> applyHex(validateHexInput());
            case RGB -> applyRgb(validateRgbInput());
            case HSV -> applyHsv(validateHsvInput());
            case NULL -> { // invalid colorFormat passed
                final String errorMessage = "Invalid color format %s, must be HEX, RGB or HSV";
                throw new IllegalStateException(String.format(errorMessage, colorFormat));
            }
        }

    }

    /**
     * Synchronises current color if ColoPicker color has been set from the exterior
     */
    private void loadPickerColor() {

        // gets the color picker's color
        final Color color = colorPicker.getColor();
        // updates the pointer accordingly
        hsvColorSelect.setPointerColor(color);

        // synchronises the other components
        synchroniseColorCodeToPointer();
        synchroniseCurrentColorToPointer();
        SynchroniseSliders();
    }

    /**
     * Updates the ColorPicker's color each time the pointer stops moving so that it can be accessed from the exterior
     * @implNote this <i>will trigger</i> the loadPickerColor method once
     */
    private void savePickerColor() {
        final Color color = hsvColorSelect.getPointerColor();
        colorPicker.setColor(color);
    }

    // ===================================
    //            COLOR CODE
    // ===================================

    /**
     * Handles displaying the correct color format when the user changes it in the format ComboBox
     */
    private void handleFormatChange() {

        // gets the new current format
        final ColorFormat currentFormat = colorFormat.getSelectionModel().getSelectedItem();

        // updates the color picker's format, so it can be accessed from the exterior
        colorPicker.setColorFormat(currentFormat);

        // hides certain text fields depending on the format
        switch (currentFormat) {
            case HEX -> showHexFormat();
            case RGB -> showRgbFormat();
            case HSV -> showHsvFormat();
        }

        // arranges the nodes making up the color format, taking into consideration any change in size
        arrangeColorFormat();
        // updates the color code to match the current color
        synchroniseColorCodeToPointer();

    }

    /**
     * Displays the hex color format, hiding the rgb & hsv color format in the process
     */
    private void showHexFormat() {

        // sets the color format to HEX if that is not already the case
        if (!colorFormat.getSelectionModel().getSelectedItem().equals(HEX)) colorFormat.setValue(HEX);

        // hides the other color codes...
        hideRgb();
        hideHsv();
        // ...and makes hex code visible
        showHex();

    }

    /**
     * Displays the rgb color format, hiding the hex & hsv color format in the process
     */
    private void showRgbFormat() {

        // sets the color format to RGB if that is not already the case
        if (!colorFormat.getSelectionModel().getSelectedItem().equals(RGB)) colorFormat.setValue(RGB);

        // hides the other color codes...
        hideHex();
        hideHsv();
        // ...and makes the rgb code visible
        showRgb();

    }

    /**
     * Displays the hsv color format, hiding the hex & rgb color format in the process
     */
    private void showHsvFormat() {

        // sets the color format to HSV if that is not already the case
        if (!colorFormat.getSelectionModel().getSelectedItem().equals(HSV)) colorFormat.setValue(HSV);

        // hides the other color codes...
        hideHex();
        hideRgb();
        // ...and makes the hsv color code visible
        showHsv();

    }

    /**
     * Synchronises the color code and current color to match the hsv display's color
     */
    private void synchroniseColorToPointer() {
        synchroniseColorCodeToPointer();
        synchroniseCurrentColorToPointer();
    }

    /**
     * Synchronises the current color code to match the currently selected color in the hsv display
     */
    private void synchroniseColorCodeToPointer() {

        // determines the current color format
        final ColorFormat currentFormat = colorFormat.getSelectionModel().getSelectedItem();

        // synchronises that color format to the current color in the hsv display
        switch (currentFormat) {
            case HEX -> syncHexToPointer();
            case RGB -> syncRgbToPointer();
            case HSV -> syncHsvToPointer();
        }
    }

    /**
     * Synchronises the current color to match the currently selected color in the hsv display
     */
    private void synchroniseCurrentColorToPointer() {
        // sets the background color to match the current color in the hsv display
        currentColor.setStyle("-fx-background-color: " + hsvColorSelect.getPointerHex());
    }

    /**
     * Synchronises the hex color code to the hsv display's pointer
     */
    private void syncHexToPointer() {
        // retrieves and sets the pointer's hex code
        swapHexText(hsvColorSelect.getPointerHex());
    }

    /**
     * Synchronises the rgb color code to the hsv display's pointer
     */
    private void syncRgbToPointer() {
        // retrieves the pointer's rgb code...
        final int[] rgb = hsvColorSelect.getPointerRgb();

        // ...adds units to it...
        final String red = "r:" + rgb[0];
        final String green = "g:" + rgb[1];
        final String blue = "b:" + rgb[2];

        // ...and displays the final result
        swapRedText(red);
        swapGreenText(green);
        swapBlueText(blue);
    }

    /**
     * Synchronises the hsv color code to the hsv display's pointer
     */
    private void syncHsvToPointer() {
        // retrieves the pointer's hsv code...
        final double[] hsv = hsvColorSelect.getPointerHsv();

        // ...adds units to it...
        final String hue = "h:" + (int) hsv[0];
        final String saturation = "s:" + (int) hsv[1];
        final String value = "v:" + (int) hsv[2];

        // ...and displays the final result
        setHueText(hue);
        setSaturationText(saturation);
        setValueText(value);
    }

    // ===================================
    //            TEXT SETTERS
    // ===================================

    /**
     * Swaps the current hex text
     * @param hexText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void swapHexText(final String hexText) {
        // clears text to avoid issues with TextField TextFormatter
        hexDisplay.clear();
        // sets the new text
        hexDisplay.setText(hexText);
    }

    /**
     * Swaps the entire rbg color code
     * @param rgb (int[]): the rgb code to display
     */
    private void swapRgb(final int[] rgb) {
        // swaps the red text
        swapRedText(Integer.toString(rgb[0]));
        // swaps the green text
        swapGreenText(Integer.toString(rgb[1]));
        // swaps the blue text
        swapBlueText(Integer.toString(rgb[2]));
    }

    /**
     * Swaps the current red text
     * @param redText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void swapRedText(final String redText) {
        // clears text to avoid issues with TextField TextFormatter
        redDisplay.clear();
        // sets the new text
        redDisplay.setText(redText);
    }

    /**
     * Swaps the current green text
     * @param greenText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void swapGreenText(final String greenText) {
        // clears text to avoid issues with TextField TextFormatter
        greenDisplay.clear();
        // sets the new text
        greenDisplay.setText(greenText);
    }

    /**
     * Swaps the current blue text
     * @param blueText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void swapBlueText(final String blueText) {
        // clears text to avoid issues with TextField TextFormatter
        blueDisplay.clear();
        // sets the new text
        blueDisplay.setText(blueText);
    }

    /**
     * Swaps the entire hsv color code
     * @param hsv (double[]): the rgb code to display
     */
    private void setHsv(final double[] hsv) {
        // swaps the hue text
        setHueText(Integer.toString((int) hsv[0]));
        // swaps the saturation text
        setSaturationText(Integer.toString((int) hsv[1]));
        // swaps the value text
        setValueText(Integer.toString((int) hsv[2]));
    }

    /**
     * Swaps the current hue text
     * @param hueText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void setHueText(final String hueText) {
        // clears text to avoid issues with TextField TextFormatter
        hueDisplay.clear();
        // sets the new text
        hueDisplay.setText(hueText);
    }

    /**
     * Swaps the current saturation text
     * @param saturationText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void setSaturationText(final String saturationText) {
        // clears text to avoid issues with TextField TextFormatter
        saturationDisplay.clear();
        // sets the new text
        saturationDisplay.setText(saturationText);
    }

    /**
     * Swaps the current value text
     * @param valueText ({@link String}): the hex code to display
     * @apiNote new text <i>must be valid</i> to avoid issues
     */
    private void setValueText(final String valueText) {
        // clears text to avoid issues with TextField TextFormatter
        valueDisplay.clear();
        // sets the new text
        valueDisplay.setText(valueText);
    }

    // ===================================
    //            KEY PRESSES
    // ===================================

    /**
     * Handles key presses in the color code text fields (hex, rgb & hsv)
     * @param keyEvent ({@link KeyEvent}): event responsible for the key press
     */
    private void handleColorFieldKeyPresses(KeyEvent keyEvent) {

        // exiting current control
        if (areKeysDown(keyEvent, ESC)) looseColorCodeFocus(keyEvent);

        // user input validation
        if (areKeysDown(keyEvent, ENTER)) synchroniseDisplayToTextFields(deterMineOrigin(keyEvent));

        // copying
        if (areKeysDown(keyEvent, CTRL, SHIFT, C)) smartCopy(keyEvent);

        // pasting
        if (areKeysDown(keyEvent, CTRL, V)) smartPaste(keyEvent);

    }

    /**
     * Handles key presses outside the color code text fields
     * @param keyEvent ({@link KeyEvent}): event responsible for the key press
     */
    private void handleGlobalKeyPresses(KeyEvent keyEvent) {

        // resetting color to default
        if (areKeysDown(keyEvent, CTRL, SHIFT, R)) resetCurrentColorFormat();

    }

    /**
     * Determines the origin of a {@link KeyEvent}
     * @param keyEvent ({@link KeyEvent}): event responsible for the key press
     * @return (ColorFormat): which color format text fields the event occurred from
     */
    private ColorFormat deterMineOrigin(KeyEvent keyEvent) {

        // determines the precise origin of the keypress...
        final ColorType preciseOrigin = determinePreciseOrigin(keyEvent);

        // ...and translates it to its general origin in a color format
        return switch (preciseOrigin) {
            case HEX                    -> HEX;
            case RED, GREEN, BLUE       -> RGB;
            case HUE, SATURATION, VALUE -> HSV;
            default                     -> NULL;
        };

    }

    /**
     * Determines the precise {@link TextField} from which the {@link KeyEvent} originated
     * @param keyEvent ({@link KeyEvent}): the event responsible for calling this method
     * @return (ColorType): the color type associated to the TextField the event was fired from
     */
    private ColorType determinePreciseOrigin(KeyEvent keyEvent) {

        // determines the origin of the keypress
        final Object source = keyEvent.getSource();
        final boolean isFromHex = source.equals(hexDisplay);
        final boolean isFromRed = source.equals(redDisplay);
        final boolean isFromGreen = source.equals(greenDisplay);
        final boolean isFromBlue = source.equals(blueDisplay);
        final boolean isFromHue = source.equals(hueDisplay);
        final boolean isFromSat = source.equals(saturationDisplay);
        final boolean isFromValue = source.equals(valueDisplay);

        // returns the precise origin of the key event
        if (isFromHex) return ColorType.HEX;
        if (isFromRed) return ColorType.RED;
        if (isFromGreen) return ColorType.GREEN;
        if (isFromBlue) return ColorType.BLUE;
        if (isFromHue) return ColorType.HUE;
        if (isFromSat) return ColorType.SATURATION;
        if (isFromValue) return ColorType.VALUE;

        // origin cannot be determined
        return ColorType.NULL;

    }

    // ===================================
    //              OVERLAY
    // ===================================

    private void showOverlay() {
        overlay.toggleOverlay();
    }

    // ===================================
    //               FOCUS
    // ===================================

    /**
     * Forces the current color code to lose focus. If format is invalid at the time focus is lost, color code
     * defaults to the currently selected color in the hsv display
     * @param keyEvent ({@link KeyEvent}): the event responsible for triggering the method
     */
    private void looseColorCodeFocus(final KeyEvent keyEvent) {
        // determines from where the key event originates
        final Node source = (Node) keyEvent.getSource();

        // gets the text inside the text field
        final String text = ((TextField) source).getText();

        // checks the validity of the text inside the textField
        final ColorFormat origin = deterMineOrigin(keyEvent);
        // if text is invalid, sets it to the current pointer color instead
        switch (origin) {
            case HEX -> {
                // sets the hex to the current hex color
                if (!StringUtil.Validation.isValidHex(text)) swapHexText(hsvColorSelect.getPointerHex());
            }
            case RGB -> {
                // determines if the current rgb color is valid
                final boolean isValidRed = StringUtil.Validation.isValidRed(text);
                final boolean isValidGreen = StringUtil.Validation.isValidGreen(text);
                final boolean isValidBlue = StringUtil.Validation.isValidBlue(text);
                // determines the currently selected rgb color
                final int[] pointerRgb = hsvColorSelect.getPointerRgb();

                // updates the rgb code to the current color if it is incorrect
                if (!isValidRed) swapRedText(Integer.toString(pointerRgb[0]));
                if (!isValidGreen) swapGreenText(Integer.toString(pointerRgb[1]));
                if (!isValidBlue) swapBlueText(Integer.toString(pointerRgb[2]));
            }
            case HSV -> {
                // determines if the current hsv code is invalid
                final boolean isValidHue = StringUtil.Validation.isValidHue(text);
                final boolean isValidSaturation = StringUtil.Validation.isValidSaturation(text);
                final boolean isValidValue = StringUtil.Validation.isValidValue(text);
                // determines the currently selected hsv color
                final double[] pointerHsv = hsvColorSelect.getPointerHsv();

                // updates the hsv code to the current color if it is incorrect
                if (!isValidHue) setHueText(Integer.toString((int) pointerHsv[0]));
                if (!isValidSaturation) setSaturationText(Integer.toString((int) pointerHsv[1]));
                if (!isValidValue) setValueText(Integer.toString((int) pointerHsv[2]));
            }
        }

        // updates the display to match the TextFields
        synchroniseDisplayToTextFields(origin);

        // deselects all text in the text field
        ((TextField) source).deselect();
        // takes away the focus from the text field
        colorPicker.requestFocus();
    }

    // ===================================
    //              COPYING
    // ===================================

    /**
     * Copies the entirety of the color code<br>
     * <br>
     * <u><i>Color Formats</i></u> : <br>
     * <ul>
     *     <li>hex : #FF0000</li>
     *     <li>rgb : rgb(r:255, g:0, b:0)</li>
     *     <li>hsv : hsv(h:0, s:0, v:0)</li>
     * </ul>
     * @param keyEvent ({@link KeyEvent}): the event responsible for calling this method
     */
    private void smartCopy(KeyEvent keyEvent) {
        switch (deterMineOrigin(keyEvent)) {
            case HEX -> smartCopyHex(validateHexInput());
            case RGB -> smartCopyRgb(validateRgbInput());
            case HSV -> smartCopyHsv(validateHsvInput());
        }
    }

    /**
     * Handles copying the hex code
     * @param hex ({@link String}): the validated hex code
     */
    private void smartCopyHex(final String hex) {

        // if the hex text is invalid, uses the currently selected color instead
        if (hex.equals("")) syncHexToPointer();

        // copies hex code to the clipboard
        ClipBoardUtil.copy(hex);

        // displays popup notifying user that hex code has been copied
        final Point2D hexLocation = NodeUtil.nodeScreenCoordinates(hexDisplay).add(0, hexDisplay.getHeight());
        copyDisplay.setText("HEX code copied");
        copyDisplay.show(colorPicker.getScene().getWindow(), hexLocation.getX(), hexLocation.getY());

    }

    /**
     * Handles copying the rgb code
     * @param rgb ({@link String}[]): the validated rgb code
     */
    private void smartCopyRgb(final String[] rgb) {

        // creates a copy of the originally validated rgb code to avoid modifying it
        final String[] rgbCopy = Arrays.copyOf(rgb, 3);

        // if rgb text is invalid...
        for (String s : rgb) {
            if (s.equals("")) {
                //...uses the currently selected color instead
                syncRgbToPointer();

                // updates the rgb code to be copied
                rgbCopy[0] = redDisplay.getText();
                rgbCopy[1] = greenDisplay.getText();
                rgbCopy[2] = blueDisplay.getText();

                // breaks out of the loop to avoid doing this multiple times
                break;
            }
        }

        // extracts red, green and red texts
        final String redText = rgbCopy[0];
        final String greenText = rgbCopy[1];
        final String blueText = rgbCopy[2];

        // formats red, green and blue strings into full rgb string
        // ex: rgb(r:255, g:0, b:0)
        final String rgbString = String.format("rgb(%s, %s, %s)", redText, greenText, blueText);

        // copies the final string to the clipboard
        ClipBoardUtil.copy(rgbString);

        // displays popup notifying user that rgb code has been copied
        final Point2D redLocation = NodeUtil.nodeScreenCoordinates(redDisplay).add(0, redDisplay.getHeight());
        copyDisplay.setText("RGB code copied");
        copyDisplay.show(colorPicker.getScene().getWindow(), redLocation.getX(), redLocation.getY());
    }

    /**
     * Handles copying the Hsv code
     * @param hsv ({@link String}[]): the validated hsv code
     */
    private void smartCopyHsv(final String[] hsv) {

        // creates a copy of the originally validated hsv code to avoid modifying it
        final String[] hsvCopy = Arrays.copyOf(hsv, 3);

        // if the hsv code is invalid...
        for (String s : hsv) {
            if (s.equals("")) {
                // ...uses the currently selected color instead
                syncHsvToPointer();

                // updates the hsv code to be copied
                hsvCopy[0] = hueDisplay.getText();
                hsvCopy[1] = saturationDisplay.getText();
                hsvCopy[2] = valueDisplay.getText();

                // exits the loop to avoid doing this multiple times
                break;
            }
        }

        // extracts hue, saturation and value strings
        final String hueText = hsvCopy[0];
        final String saturationText = hsvCopy[1];
        final String valueText = hsvCopy[2];

        // formats hue, saturation and value strings into full hsv string
        // ex: hsv(h:0, s:100, v:100)
        final String hsvString = String.format("hsv(%s, %s, %s)", hueText, saturationText, valueText);

        // copies the final string to the clipboard
        ClipBoardUtil.copy(hsvString);

        // displays popup notifying user that hsv code has been copied
        final Point2D hueLocation = NodeUtil.nodeScreenCoordinates(hueDisplay).add(0, hueDisplay.getHeight());

        copyDisplay.setText("HSV code copied");
        copyDisplay.show(colorPicker.getScene().getWindow(), hueLocation.getX(), hueLocation.getY());

    }

    // ===================================
    //              PASTING
    // ===================================

    /**
     * Handles pasting values into the color code text fields
     * @param keyEvent ({@link KeyEvent}): the event responsible for calling this method
     */
    private void smartPaste(final KeyEvent keyEvent) {

        // gets the content to be pasted...
        final String content = ClipBoardUtil.getClipboardContent();

        // ...and checks to see if it matches any color code...
        if (hexPasteFormat(content)) return;
        if (rgbPasteFormat(content)) return;
        if (hsvPasteFormat(content)) return;

        // ...otherwise pastes the content into the current color format text field
        switch (deterMineOrigin(keyEvent)) {
            case HEX -> handleHexPaste();
            case RGB -> handleRgbPaste(keyEvent);
            case HSV -> handleHsvPaste(keyEvent);
        }
    }

    /**
     * Checks to see if the specified content can be directly pasted as a hex code
     * @param content ({@link String}): the current clipboard content
     * @return whether the content can be pasted as a complete hex code
     */
    private boolean hexPasteFormat(final String content) {
        // stops if content is not in the correct hex format
        if (!StringUtil.Validation.isValidHex(content)) return false;

        // otherwise, copies the content directly to the hex display and switches to that color mode
        swapHexText(content);
        showHexFormat();
        synchroniseDisplayToTextFields(HEX);

        // takes away the focus from the hex display text field
        colorPicker.requestFocus();

        return true;
    }

    /**
     * Checks to see if the specified content can be directly pasted as a rgb code
     * @param content ({@link String}): the current clipboard content
     * @return whether the content can be pasted as a complete rbg code
     */
    private boolean rgbPasteFormat(final String content) {
        // stops if content is not in valid rgb format
        if (!StringUtil.Validation.isValidRgb(content)) return false;

        // extracts the rgb values from the content
        final int[] rgb = ColorUtil.Rgb.fromRgbString(content);

        // copies the content directly to the rgb display and switches to that color mode
        swapRgb(rgb);
        showRgbFormat();
        synchroniseDisplayToTextFields(RGB);

        // takes away the focus from the rgb display text fields
        colorPicker.requestFocus();

        return true;
    }

    /**
     * Checks to see if the specified content can be directly pasted as a hsv code
     * @param content ({@link String}): the current clipboard content
     * @return whether the content can be pasted as a complete hsv code
     */
    private boolean hsvPasteFormat(final String content) {
        // stops if content is not in valid hsv format
        if (!StringUtil.Validation.isValidHsv(content)) return false;

        // extract hsv values from content
        final double[] hsv = ColorUtil.Hsv.fromHsvString(content);

        // copies the content directly to the hsv display and switches to that color mode
        setHsv(hsv);
        showHsvFormat();
        synchroniseDisplayToTextFields(HSV);

        // takes away the focus from the hsv display text fields
        colorPicker.requestFocus();

        return true;
    }

    /**
     * Tries to paste the clipboard contents into the hex display text field
     */
    private void handleHexPaste() {
        // saves the content of the text clipboard
        final String content = ClipBoardUtil.getClipboardContent();

        // sets the hex text
        swapHexText(content);
        // tries to synchronise the display with the hex text (verification happens here if text is invalid)
        synchroniseDisplayToTextFields(HEX);
    }

    /**
     * Tries to paste the clipboard content into the rgb display text fields
     * @param keyEvent ({@link KeyEvent}): the event responsible for calling this method
     */
    private void handleRgbPaste(final KeyEvent keyEvent) {
        // saves the content of the clipboard
        final String content = ClipBoardUtil.getClipboardContent();
        // determines from which text field the event originated
        final ColorType preciseOrigin = determinePreciseOrigin(keyEvent);

        // updates the text field's text
        switch (preciseOrigin) {
            case RED   -> swapRedText(content);
            case GREEN -> swapGreenText(content);
            case BLUE  -> swapBlueText(content);
        }

        // tries to synchronise the display with the rgb text (verification happens here if text is invalid)
        synchroniseDisplayToTextFields(RGB);

    }

    /**
     * Tries to paste the clipboard content into the hsv display text fields
     * @param keyEvent ({@link KeyEvent}): the event responsible for calling this method
     */
    private void handleHsvPaste(final KeyEvent keyEvent) {
        // saves the content of the clipboard
        final String content = ClipBoardUtil.getClipboardContent();
        // determines from which text field the event originated
        final ColorType preciseOrigin = determinePreciseOrigin(keyEvent);

        // updates the text field's text
        switch (preciseOrigin) {
            case HUE        -> setHueText(content);
            case SATURATION -> setSaturationText(content);
            case VALUE      -> setValueText(content);
        }

        // tries to synchronise the display with the rgb text (verification happens here if text is invalid)
        synchroniseDisplayToTextFields(HSV);
    }

    // ===================================
    //          INPUT VALIDATION
    // ===================================

    /**
     * Resets the current color format to its default color
     */
    private void resetCurrentColorFormat() {
        switch (colorFormat.getSelectionModel().getSelectedItem()) {
            case HEX -> resetHex();
            case RGB -> resetRgb();
            case HSV -> resetHsv();
        }
    }

    /**
     * Checks that the content of the Hex text field is a correct hex code
     * @return (String) the validated hex color code
     * @apiNote uses "" as a flag for an invalid hex code
     */
    private String validateHexInput() {
        final String hexText = hexDisplay.getText();

        // returns an empty string if hex code is invalid
        return StringUtil.Validation.isValidHex(hexText) ? hexText : "";
    }

    /**
     * Applies the new hex code if it is valid & synchronises the other components to it.
     * Otherwise, sets it to match the currently selected color
     * @param hex ({@link String}): the parsed hex code
     */
    private void applyHex(final String hex) {

        // if the hex code is invalid...
        if (hex.equals("")) {

            // ...gets the screen location of the hex text field...
            final Point2D hexLocation = NodeUtil.nodeScreenCoordinates(hexDisplay).add(0, hexDisplay.getHeight());

            // ...and displays a popup warning user the code is invalid...
            copyDisplay.setLifeCycle(2000);
            copyDisplay.setText("Invalid HEX code");
            copyDisplay.show(colorPicker.getScene().getWindow(), hexLocation.getX(), hexLocation.getY());

            // ...then synchronises the hex code to the current color
            syncHexToPointer();

            return;
        }

        // completes hex code if it lacks units
        if (!hex.contains("#")) swapHexText("#" + hex);

        // sets the pointer at the correct position to match the hex code
        final double[] hsv = ColorUtil.Hex.toHsv(hex);
        hsvColorSelect.setPointerHsv(hsv[0], hsv[1], hsv[2]);

        // updates the sliders accordingly
        SynchroniseSliders();

        // makes sure the color code displayed at the end is the one imputed by the user
        // (because the pointer is moving, it is possible for the text to be updated midway)
        swapHexText(hex);
    }

    /**
     * Resets the hex color code to its default value
     */
    private void resetHex() {
        hsvColorSelect.setPointerHex(DEFAULT_HEX);
    }

    /**
     * Checks that the content of each of the rgb text fields form a correct rbg code
     * @return (String[]): array of validated rgb strings
     * @apiNote uses "" as a flag for an invalid hex code
     */
    private String[] validateRgbInput() {
        // gets rgb strings from each textBox
        String redText = redDisplay.getText();
        String greenText = greenDisplay.getText();
        String blueText = blueDisplay.getText();

        // checks the validity of each rgb string
        final boolean isValidRed = StringUtil.Validation.isValidRed(redText);
        final boolean isValidGreen = StringUtil.Validation.isValidGreen(greenText);
        final boolean isValidBlue = StringUtil.Validation.isValidBlue(blueText);

        // resets rgb values if one or more are invalid
        if (!isValidRed || !isValidGreen || !isValidBlue) return new String[]{"", "", ""};

        // completes each rgb text if they lack units
        final boolean redHasUnits = redText.contains("r:");
        final boolean greenHasUnits = greenText.contains("g:");
        final boolean blueHasUnits = blueText.contains("b:");

        if (!redHasUnits) {
            redText = "r:" + redText;
            swapRedText(redText);
        }
        if (!greenHasUnits) {
            greenText = "g:" + greenText;
            swapGreenText(greenText);
        }
        if (!blueHasUnits) {
            blueText = "b:" + blueText;
            swapBlueText(blueText);
        }

        return new String[]{redText, greenText, blueText};
    }

    /**
     * Applies the new rgb code if it is valid & synchronises the other components to it.
     * Otherwise, sets it to match the currently selected color
     * @param rgb ({@link String}[]): the parsed rgb code
     */
    private void applyRgb(final String[] rgb) {

        // checks to see if the rgb code is invalid
        for (String s : rgb) {

            if (!s.equals("")) continue;

            // if the rgb code is invalid, determines the position of the red display text field...
            final Point2D rgbLocation = NodeUtil.nodeScreenCoordinates(redDisplay).add(0, redDisplay.getHeight());

            // ...and displays a popup warning user code is invalid...
            copyDisplay.setLifeCycle(2000);
            copyDisplay.setText("Invalid RGB code");
            copyDisplay.show(colorPicker.getScene().getWindow(), rgbLocation.getX(), rgbLocation.getY());

            // ...then synchronises the rgb code to match the currently selected color
            syncRgbToPointer();

            return;

        }

        // extracts red, green and red texts
        final String redText = rgb[0];
        final String greenText = rgb[1];
        final String blueText = rgb[2];

        // sets the pointer at the correct position to match the rgb code
        final int[] rgbValues = ColorUtil.Rgb.fromRgbString(redText, greenText, blueText);
        final double[] hsv = ColorUtil.Rgb.toHsv(rgbValues[0], rgbValues[1], rgbValues[2]);

        hsvColorSelect.setPointerHsv(hsv[0], hsv[1], hsv[2]);

        // updates the sliders accordingly
        SynchroniseSliders();

        // makes sure the color code displayed at the end is the one imputed by the user
        // (because the pointer is moving, it is possible for the text to be updated midway)
        swapRedText(redText);
        swapGreenText(greenText);
        swapBlueText(blueText);
    }

    /**
     * Resets the rgb code to its default value
     */
    private void resetRgb() {

        swapRedText("r:" + DEFAULT_RGB[0]);
        swapGreenText("g:" + DEFAULT_RGB[1]);
        swapBlueText("b:" + DEFAULT_RGB[2]);

        hsvColorSelect.setPointerRgb(DEFAULT_RGB[0], DEFAULT_RGB[1], DEFAULT_RGB[2]);

    }

    /**
     * Checks that the content of each of the hsv text fields form a correct hsv code
     * @return (String[]): array of validated hsv strings
     * @apiNote uses "" as a flag for an invalid hex code
     */
    private String[] validateHsvInput() {
        // gets hsv string from each text box
        String hueText = hueDisplay.getText();
        String saturationText = saturationDisplay.getText();
        String valueText = valueDisplay.getText();

        // checks the validity of each string
        final boolean isValidHue = StringUtil.Validation.isValidHue(hueText);
        final boolean isValidSaturation = StringUtil.Validation.isValidSaturation(saturationText);
        final boolean isValidValue = StringUtil.Validation.isValidValue(valueText);

        // resets hsv values if one of the strings is invalid
        if (!isValidHue || !isValidSaturation || !isValidValue) return new String[]{"", "", ""};

        // completes each hsv text if they lack units
        final boolean hueHasUnits = hueText.contains("h:");
        final boolean saturationHasUnits = saturationText.contains("s:");
        final boolean valueHasUnits = valueText.contains("v:");

        if (!hueHasUnits) {
            hueText = "h:" + hueText;
            setHueText(hueText);
        }
        if (!saturationHasUnits) {
            saturationText = "s:" + saturationText;
            setSaturationText(saturationText);
        }
        if (!valueHasUnits) {
            valueText = "v:" + valueText;
            setValueText(valueText);
        }

        return new String[]{hueText, saturationText, valueText};
    }

    /**
     * Applies the new hsv code if it is valid & synchronises the other components to it.
     * Otherwise, sets it to match the currently selected color
     * @param hsv ({@link String}[]): the parsed hsv code
     */
    private void applyHsv(final String[] hsv) {

        // checks to see if the hsv code is valid
        for (String s : hsv) {

            if (!s.equals("")) continue;

            // if the hsv code is invalid, determines the screen position of the hue display text field...
            final Point2D hsvLocation = NodeUtil.nodeScreenCoordinates(hueDisplay).add(0, hueDisplay.getHeight());

            // ...and display a popup warning the user the code is invalid...
            copyDisplay.setLifeCycle(2000);
            copyDisplay.setText("Invalid HSV code");
            copyDisplay.show(colorPicker.getScene().getWindow(), hsvLocation.getX(), hsvLocation.getY());

            // ...then synchronises the hsv color code to match the currently selected color
            syncHsvToPointer();

            return;

        }

        // extracts hue, saturation and value strings
        final String hueText = hsv[0];
        final String saturationText = hsv[1];
        final String valueText = hsv[2];

        // sets the pointer at the correct position to match hsv code
        final double[] hsvValues = ColorUtil.Hsv.fromHsvString(hueText, saturationText, valueText);
        hsvColorSelect.setPointerHsv(hsvValues[0], hsvValues[1], hsvValues[2]);

        // updates the sliders accordingly
        SynchroniseSliders();

        // makes sure the color code displayed at the end is the one imputed by the user
        // (because the pointer is moving, it is possible for the text to be updated midway)
        setHueText(hueText);
        setSaturationText(saturationText);
        setValueText(valueText);
    }

    /**
     * Resets the hsv color code to its default values
     */
    private void resetHsv() {

        setHueText("h:" + DEFAULT_HSV[0]);
        setSaturationText("s:" + DEFAULT_HSV[1]);
        setValueText("v:" + DEFAULT_HSV[2]);

        hsvColorSelect.setPointerHsv(DEFAULT_HSV[0], DEFAULT_HSV[1], DEFAULT_HSV[2]);
    }

    // ===================================
    //             RESIZING
    // ===================================

    /**
     * Handles changes in the {@link MaterialColorPicker}'s size
     */
    private void handleResize() {

        // updates the available space at the bottom of the color picker
        bottomSpace = calculateBottomSize();
        topSpace = calculateTopSize();

        // gets the available width and height
        final double width  = colorPicker.getWidth();

        // ...and resizes the hsv color spectrum accordingly
        hsvColorSelect.setMaxSize(width , topSpace);
        hsvColorSelect.setPrefSize(width, topSpace);

        // resizes the sliders
        resizeSliders();
        // resizes the current color format
        resizeColorFormat();

        // repositions all the components, taking into consideration their new sizes
        reposition();
    }

    /**
     * Handles resizing the hue and saturation sliders
     */
    private void resizeSliders() {

        // maximal space
        final Rectangle2D pickerSize = NodeUtil.getActualNodeSize(colorPickerIcon, pickerMargins);
        final Rectangle2D currentColorSize = NodeUtil.getActualNodeSize(currentColor, currentColorMargins);
        final double width = colorPicker.getWidth();

        // gets the available space for the sliders
        final double adjustX = pickerSize.getWidth() + currentColorSize.getWidth() + hueSliderMargins.getLeft();
        final double availableWidth = width - adjustX - hueSliderMargins.getRight();

        // resizes the sliders to fit the new size
        hueSlider.setMaxWidth(availableWidth);
        hueSlider.setPrefWidth(availableWidth);
        valueSlider.setMaxWidth(availableWidth);
        valueSlider.setPrefWidth(availableWidth);
    }

    /**
     * Handles resizing the color format pane
     */
    private void resizeColorFormat() {

        // determines the new size of the format pane, taking into consideration its margins
        final double width = colorPicker.getWidth();
        final double x = colorFormatMargins.getLeft();
        final double y = width - colorFormatMargins.getRight();
        final double availableWidth = y - x;

        // updates the color format pane to its new size
        formatPane.setMaxWidth(availableWidth);
        formatPane.setPrefWidth(availableWidth);
    }

    /**
     * Calculates the space available at the top of the color picker
     * @return (double): the space available for the hsv color spectrum to be displayed
     */
    private double calculateTopSize() {
        return colorPicker.getHeight() - bottomSpace;
    }

    /**
     * Calculates the space available at the bottom of the colo picker
     * @return (double): the space available for the color picker icon, current color, sliders & color format
     */
    private double calculateBottomSize() {
        // determines the actual size of each component (taking into consideration margins)
        final double hueSliderHeight = NodeUtil.getActualNodeSize(hueSlider, hueSliderMargins).getHeight();
        final double valueSliderHeight = NodeUtil.getActualNodeSize(valueSlider, valueSliderMargins).getHeight();
        final double colorFormatHeight = NodeUtil.getActualNodeSize(formatPane, colorFormatMargins).getHeight();

        // returns the final height
        return hueSliderHeight + valueSliderHeight + colorFormatHeight;
    }

    // ===================================
    //            POSITIONING
    // ===================================

    /**
     * Sets margins for components inside the {@link MaterialColorPicker}
     * @param node ({@link Node}): the node to add margins to
     * @param margin ({@link Insets}): the node's new margins
     */
    public static void setMargin(Node node, Insets margin) {
        node.getProperties().put(MARGIN_KEY, margin);
    }

    /**
     * Retrieves a node's margins inside of a {@link MaterialColorPicker}
     * @param node ({@link Node}): the node to retrieve the margins of
     * @return (Insets): the node's margins
     */
    public static Insets getMargin(Node node) {
        return (Insets) node.getProperties().get(MARGIN_KEY);
    }

    /**
     * Handles reposition each component inside the {@link MaterialColorPicker}
     */
    private void reposition() {

        // determines each component's size
        final Rectangle2D pickerSize = NodeUtil.getActualNodeSize(colorPickerIcon, pickerMargins);
        final Rectangle2D currentColorSize = NodeUtil.getActualNodeSize(currentColor   , currentColorMargins);
        final Rectangle2D hueSliderSize = NodeUtil.getActualNodeSize(hueSlider, hueSliderMargins);
        final Rectangle2D valueSliderSize = NodeUtil.getActualNodeSize(valueSlider, valueSliderMargins);
        final Rectangle2D colorFormatSize = NodeUtil.getActualNodeSize(formatPane, colorFormatMargins);

        // color picking
        repositionPicker(pickerSize, colorFormatSize);
        repositionCurrentColor(currentColorSize, pickerSize, colorFormatSize);

        // sliders
        repositionHueSlider(pickerSize, currentColorSize);
        repositionValueSlider(pickerSize, currentColorSize, hueSliderSize);

        // color format
        arrangeColorFormat();
        repositionColorFormat(hueSliderSize, valueSliderSize);
    }

    /**
     * Handles repositioning the color picker icon
     * @param pickerSize ({@link Rectangle2D}): the size of the picker icon
     * @param colorFormatSize ({@link Rectangle2D}): the size of the color format bar
     */
    private void repositionPicker(
            final Rectangle2D pickerSize,
            final Rectangle2D colorFormatSize
    ) {

        // determines the available height for the picker icon
        final double availableY = bottomSpace - colorFormatSize.getHeight() - pickerSize.getHeight();
        // determines at which height to place the picker icon
        final double y = topSpace + availableY / 2 + pickerSize.getHeight();
        // determines at which x-coordinate to place the picker icon
        final double x = pickerMargins.getLeft();

        // positions the picker icon at the correct coordinates
        NodeUtil.positionAt(colorPickerIcon, x, y);
    }

    /**
     * Handles repositioning the current color
     * @param currenColorSize ({@link Rectangle2D}): the size of the current color
     * @param pickerSize ({@link Rectangle2D}): the size of the color picker icon
     * @param colorFormatSize ({@link Rectangle2D}): the size of the color format bar
     */
    private void repositionCurrentColor(
            final Rectangle2D currenColorSize,
            final Rectangle2D pickerSize,
            final Rectangle2D colorFormatSize
    ) {

        // determines the available height for the current color icon
        final double availableY = bottomSpace - colorFormatSize.getHeight() - currenColorSize.getHeight();
        // determines at which height to place the current color
        final double y = topSpace + availableY / 2;
        // determines at which x-coordinate to place the current color
        final double x = pickerSize.getWidth() + currentColorMargins.getLeft();

        // positions the current color at the correct coordinates
        NodeUtil.positionAt(currentColor, x, y);

    }

    /**
     * Handles repositioning the hue slider
     * @param pickerSize ({@link Rectangle2D}): the size of the color picker icon
     * @param currentColorSize ({@link Rectangle2D}): the size of the current color
     */
    private void repositionHueSlider(
            final Rectangle2D pickerSize,
            final Rectangle2D currentColorSize
    ) {

        // determines at which height to place the hue slider
        final double y = topSpace + hueSliderMargins.getTop();
        // determines at which x-coordinate to place the hue slider
        final double x = pickerSize.getWidth() + currentColorSize.getWidth() + hueSliderMargins.getLeft();

        // positions the hue slider at the correct coordinates
        NodeUtil.positionAt(hueSlider, x, y);

    }

    /**
     * Handles repositioning the value slider
     * @param pickerSize ({@link Rectangle2D}): the size of the color picker icon
     * @param currentColorSize ({@link Rectangle2D}): the size of the current color
     * @param hueSliderSize ({@link java.awt.Rectangle}): the size of the hue slider
     */
    private void repositionValueSlider(
            final Rectangle2D pickerSize,
            final Rectangle2D currentColorSize,
            final Rectangle2D hueSliderSize
    ) {

        // determines at which height to place the value slider
        final double y = topSpace + hueSliderSize.getHeight() + valueSliderMargins.getTop();
        // determines at which x-coordinate to place the value slider
        final double x = pickerSize.getWidth() + currentColorSize.getWidth() + valueSliderMargins.getLeft();

        // positions the value slider at the correct coordinates
        NodeUtil.positionAt(valueSlider, x, y);
    }

    /**
     * Handles repositioning the color format CONTAINER
     * @param hueSliderSize ({@link Rectangle2D}): the size of the hue slider
     * @param valueSliderSize ({@link Rectangle2D}): the size of the value slider
     */
    private void repositionColorFormat(
            final Rectangle2D hueSliderSize,
            final Rectangle2D valueSliderSize
    ) {

        // determines at which height to place the color format container
        final double y = topSpace + hueSliderSize.getHeight() + valueSliderSize.getHeight() + colorFormatMargins.getTop();
        // determines at which x-coordinate to place the color format container
        final double x = colorFormatMargins.getLeft();

        // positions the color format container at the correct coordinates
        NodeUtil.positionAt(formatPane, x, y);
    }

    /**
     * Arranges the nodes inside the color format pane
     */
    private void arrangeColorFormat() {

        // determines the available space inside the color format pane
        final double formatPaneWidth = formatPane.getWidth();
        final double colorFormatWidth = this.colorFormat.getWidth();
        // determines the current color format
        final ColorFormat currentFormat = this.colorFormat.getSelectionModel().getSelectedItem();

        // based on the current color format, determines which components need to be arranged
        switch (currentFormat) {
            case HEX -> arrangeHex(formatPaneWidth, colorFormatWidth);
            case RGB -> arrangeRGB(formatPaneWidth, colorFormatWidth);
            case HSV -> arrangeHsv(formatPaneWidth, colorFormatWidth);
        }

    }

    /**
     * Arranges the hex text field inside the color format container
     * @param formatPaneWidth (double): the width of the color format CONTAINER
     * @param currentFormatWidth (double): the width of the current color format COMBOBOX
     */
    private void arrangeHex(final double formatPaneWidth, final double currentFormatWidth) {

        // determines the remaining width inside the color format container...
        final double availableWidth = formatPaneWidth - currentFormatWidth;

        // ...and resizes the hex text field accordingly
        hexDisplay.setMaxWidth(availableWidth);
        hexDisplay.setPrefWidth(availableWidth);
        hexDisplay.setLayoutX(currentFormatWidth);

    }

    /**
     * Arranges the rgb text field inside the color format container
     * @param formatPaneWidth (double): the width of the color format CONTAINER
     * @param currentFormatWidth (double): the width of the current color format COMBOBOX
     */
    private void arrangeRGB(final double formatPaneWidth, final double currentFormatWidth) {

        // determines the remaining width inside the color format container...
        final double availableWidth = formatPaneWidth - currentFormatWidth;
        // ...splits it equally between each of the TextFields...
        final double textFieldWidth = availableWidth / 3;
        // ...and determines where to position each of the TextFields accordingly
        final double rX = currentFormatWidth;
        final double gX = currentFormatWidth + textFieldWidth;
        final double bX = currentFormatWidth + textFieldWidth * 2;

        // sets the size & position for the red text field
        redDisplay.setMaxWidth(textFieldWidth);
        redDisplay.setPrefWidth(textFieldWidth);
        redDisplay.setLayoutX(rX);

        // sets the size & position for the green text field
        greenDisplay.setMaxWidth(textFieldWidth);
        greenDisplay.setPrefWidth(textFieldWidth);
        greenDisplay.setLayoutX(gX);

        // sets the size & position for the blue text field
        blueDisplay.setMaxWidth(textFieldWidth);
        blueDisplay.setPrefWidth(textFieldWidth);
        blueDisplay.setLayoutX(bX);
    }

    /**
     * Arranges the hsv text field inside the color format container
     * @param formatPaneWidth (double): the width of the color format CONTAINER
     * @param currentFormatWidth (double): the width of the current color format COMBOBOX
     */
    private void arrangeHsv(final double formatPaneWidth, final double currentFormatWidth) {

        // determines the remaining width inside the color format container...
        final double availableWidth = formatPaneWidth - currentFormatWidth;
        // ...splits it equally between each of the TextFields...
        final double textFieldWidth = availableWidth / 3;
        // ...and determines where to position each of the TextFields accordingly
        final double hX = currentFormatWidth;
        final double sX = currentFormatWidth + textFieldWidth;
        final double vX = currentFormatWidth + textFieldWidth * 2;

        // sets the size & position for the hue text field
        hueDisplay.setMaxWidth(textFieldWidth);
        hueDisplay.setPrefWidth(textFieldWidth);
        hueDisplay.setLayoutX(hX);

        // sets the size & position for the saturation text field
        saturationDisplay.setMaxWidth(textFieldWidth);
        saturationDisplay.setPrefWidth(textFieldWidth);
        saturationDisplay.setLayoutX(sX);

        // sets the size & position for the value text field
        valueDisplay.setMaxWidth(textFieldWidth);
        valueDisplay.setPrefWidth(textFieldWidth);
        valueDisplay.setLayoutX(vX);

    }

    // ===================================
    //              DISPLAY
    // ===================================

    /**
     * Shows the hex {@link TextField}
     */
    private void showHex() {
        hexDisplay.setVisible(true);
    }

    /**
     * Hides the hex {@link TextField}
     */
    private void hideHex() {
        hexDisplay.setVisible(false);
    }


    /**
     * Makes all the rgb {@link TextField TextFields} either visible or invisible
     * @param isVisible (boolean): whether the rgb TextFields are visible
     */
    private void setVisibleRgb(final boolean isVisible) {
        for (TextField textField : rgb) {
            textField.setVisible(isVisible);
        }
    }

    /**
     * Makes the rgb {@link TextField TextFields} visible
     */
    private void showRgb() {
        setVisibleRgb(true);
    }

    /**
     * Makes the rgb {@link TextField TextFields} invisible
     */
    private void hideRgb() {
        setVisibleRgb(false);
    }

    /**
     * Makes all the hsv {@link TextField TextFields} either visible or invisible
     * @param isVisible (boolean): whether the rgb TextFields are visible
     */
    private void setVisibleHsv(final boolean isVisible) {
        for (TextField textField : hsv) {
            textField.setVisible(isVisible);
        }
    }

    /**
     * Makes the hsv {@link TextField TextFields} visible
     */
    private void showHsv() {
        setVisibleHsv(true);
    }

    /**
     * Makes the hsv {@link TextField TextFields} invisible
     */
    private void hideHsv() {
        setVisibleHsv(false);
    }
}
