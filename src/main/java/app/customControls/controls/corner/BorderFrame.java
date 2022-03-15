package app.customControls.controls.corner;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.css.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.List;

/**
 * Visual overlay made up of {@link BorderLine Borderlines} displaying the corners of a container with the possibility
 * to display text along its edges. Text will be automatically hidden when there is not enough space to display it<br><br>
 *
 * <u><i>text locations</i></u>: "top", "right", "bottom", "left", "center"<br><br>
 *
 * <u><i>css properties</i></u> :<br>
 * <list>
 *     <li>-fx-text-location: where to display text in the frame</li>
 * </list>
 */
public class BorderFrame extends Region {

    // ===================================
    //              FIELDS
    // ===================================

    /*          DEFAULT VALUES          */
    public static final double DEFAULT_FONT_SIZE     = 12;
    public static final String DEFAULT_TEXT = "";
    public static final Font DEFAULT_FONT = Font.getDefault();
    public static final String DEFAULT_TEXT_LOCATION = "top";

    /*     CSS STYLEABLE PROPERTIES     */
    private static final StyleablePropertyFactory<BorderFrame> FACTORY = new StyleablePropertyFactory<>(
            Region.getClassCssMetaData());

    // text location
    private static final CssMetaData<BorderFrame, String> TEXT_LOCATION_CSS = FACTORY.createStringCssMetaData(
            "-fx-text-location", borderFrame -> borderFrame.textLocation, DEFAULT_TEXT_LOCATION, false
    );
    private final StyleableProperty<String> textLocation;

    /*          JAVA PROPERTIES         */
    // font size
    private final SimpleDoubleProperty textSize;

    // font type
    private final SimpleObjectProperty<Font> fontType;

    // text
    private final SimpleStringProperty text;

    /*            COMPONENTS            */
    private final BorderLine[] corners = new BorderLine[4];
    private final Label label = new Label();

    /*             LISTENERS            */
    private final InvalidationListener resizeListener;
    private final ChangeListener<String> locationListener;

    /*          INITIALISATION          */
    private boolean hasInitialised = false;

    // ===================================
    //           CONSTRUCTOR
    // ===================================

    /**
     * BorderFrame class constructor
     * @param cornerSize (double): width & height of each corner
     * @param cornerThickness (double): line thickness for corners, cannot be greater than cornerSize
     */
    public BorderFrame(final double cornerSize, final double cornerThickness) {
        this(cornerSize, cornerThickness, null);
    }

    /**
     * BorderFrame class constructor
     * @param cornerSize (double): width & height of each corner
     * @param cornerThickness (double): line thickness for corners, cannot be greater than cornerSize
     * @param cornerColor (Color): color applied to the corners
     */
    public BorderFrame(final double cornerSize, final double cornerThickness, final Color cornerColor) {
        this(cornerSize, cornerThickness, cornerColor, DEFAULT_TEXT);
    }

    /**
     * BorderFrame class constructor
     * @param cornerSize (double): width & height of each corner
     * @param cornerThickness (double): line thickness for corners, cannot be greater than cornerSize
     * @param cornerColor (Color): color applied to the corners
     * @param text (String): text to display
     */
    public BorderFrame(final double cornerSize, final double cornerThickness, final Color cornerColor,
                       final String text) {
        this(cornerSize, cornerThickness, cornerColor, text, DEFAULT_TEXT_LOCATION);
    }

    /**
     * BorderFrame class constructor
     * @param cornerSize (double): width & height of each corner
     * @param cornerThickness (double): line thickness for corners, cannot be greater than cornerSize
     * @param cornerColor (Color): color applied to the corners
     * @param text (String): text to display
     * @param textLocation (String): "top", "right", "bottom", "left" or "center"
     */
    public BorderFrame(final double cornerSize, final double cornerThickness, final Color cornerColor,
                       final String text, final String textLocation) {
        this(cornerSize, cornerThickness, cornerColor, text, textLocation, DEFAULT_FONT, DEFAULT_FONT_SIZE);
    }

    /**
     * BorderFrame class constructor
     * @param cornerSize (double): width & height of each corner
     * @param cornerThickness (double): line thickness for corners, cannot be greater than cornerSize
     * @param cornerColor (Color): color applied to the corners
     * @param text (String): text to display
     * @param textLocation (String): "top", "right", "bottom", "left" or "center"
     * @param textFont (Font): font used to display text
     * @param fontSize (double): size of the font
     */
    public BorderFrame(final double cornerSize, final double cornerThickness, final Color cornerColor,
                       final String text, final String textLocation, final Font textFont, final double fontSize) {

        // initialises component properties
        this.text = new SimpleStringProperty(this, "text", DEFAULT_TEXT);
        this.textSize = new SimpleDoubleProperty(this, "font-size", DEFAULT_FONT_SIZE);
        this.fontType = new SimpleObjectProperty<>(this, "font-type", DEFAULT_FONT);
        this.textLocation = new SimpleStyleableStringProperty(TEXT_LOCATION_CSS, this, "-text-location");

        // initialises listeners
        this.resizeListener = observable -> refreshDisplay();
        this.locationListener = (observableValue, oldLocation, newLocation) -> setTextLocation(newLocation);

        // creates the corners
        initialiseCorners(cornerSize, cornerThickness, cornerColor);

        // sets constructor values for properties
        setCornerSize(cornerSize);
        setCornerThickness(cornerThickness);
        setColor(cornerColor);
        setText(text);
        setFont(textFont);
        setTextSize(fontSize);
        setTextLocation(textLocation);

        // initialises the BorderFrame & adds the required components to it
        initialise();
        populate();

        // registers resizing listeners & css property listeners
        registerListeners();

        hasInitialised = true;

        // displays the BorderFrame
        refreshDisplay();

    }

    // ===================================
    //          INITIALISATION
    // ===================================

    private void initialise() {
        // frame
        getStyleClass().setAll("border-frame");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private void populate() {

        resize();

        Pane pane = new Pane();
        pane.getChildren().addAll(corners);
        pane.getChildren().add(label);

        getChildren().add(pane);

    }

    private void initialiseCorners(final double cornerSize, final double cornerThickness, final Color cornerColor) {

        corners[0] = new BorderLine(Orientation.TOP_LEFT, cornerSize, cornerThickness, cornerColor);
        corners[1] = new BorderLine(Orientation.TOP_RIGHT, cornerSize, cornerThickness, cornerColor);
        corners[2] = new BorderLine(Orientation.BOTTOM_RIGHT, cornerSize, cornerThickness, cornerColor);
        corners[3] = new BorderLine(Orientation.BOTTOM_LEFT, cornerSize, cornerThickness, cornerColor);

    }

    private void registerListeners() {
        // resizing
        widthProperty().addListener(resizeListener);
        heightProperty().addListener(resizeListener);

        // styleable properties
        textLocationProperty().addListener(locationListener);
    }

    // ===================================
    //             ACCESSORS
    // ===================================

    // corner size

    public DoubleProperty cornerSizeProperty() {
        return corners[0].sizeProperty();
    }

    /**
     * Gets the size of corners making up the {@link BorderFrame}
     * @return (double): size of each corner
     */
    public double getCornerSize() {
        return corners[0].getSize();
    }

    /**
     * Sets the size of the corners making up the {@link BorderFrame}
     * @param newCornerSize (double): new corner width & height
     */
    public void setCornerSize(final double newCornerSize) {
        if (newCornerSize < 0) return;
        corners[0].setSize(newCornerSize);

        for (BorderLine corner : corners) {
            corner.setSize(newCornerSize);
        }

        refreshDisplay();
    }

    // corner thickness
    public DoubleProperty cornerThicknessProperty() {
        return corners[0].thicknessProperty();
    }

    /**
     * Gets the thickness of the {@link BorderLine BorderLines} making up the {@link BorderFrame}
     * @return (double): BorderLine thickness
     */
    public double getCornerThickness() {
        return corners[0].getThickness();
    }

    /**
     * Sets the thickness of each {@link BorderLine} making up the {@link BorderFrame}
     * @param newCornerThickness (double): new line thickness
     */
    public void setCornerThickness(final double newCornerThickness) {
        if (newCornerThickness < 0 || newCornerThickness > getCornerSize()) return;
        corners[0].setThickness(newCornerThickness);

        for (BorderLine corner : corners) {
            corner.setThickness(newCornerThickness);
        }

        refreshDisplay();
    }

    // corner color
    public ObjectProperty<Color> colorProperty() {
        return corners[0].colorProperty();
    }

    /**
     * Gets the {@link Color} of the {@link BorderLine BorderLines} making up the {@link BorderFrame}
     * @return
     */
    public Color getColor() {
        return corners[0].getColor();
    }

    /**
     * Sets the {@link Color} of the {@link BorderLine BorderLines} making up the {@link BorderFrame}
     * @param newColor (Color): new line color
     */
    public void setColor(final Color newColor) {
        corners[0].setColor(newColor);

        for (BorderLine corner : corners) {
            corner.setColor(newColor);
        }
        label.setTextFill(newColor);

        refreshDisplay();
    }

    // text
    public StringProperty textProperty() {
        return text;
    }

    /**
     * Gets the text displayed by the {@link BorderFrame}'s {@link Label}
     * @return (String): the text being displayed
     */
    public String getText() {
        return text.get();
    }

    /**
     * Sets the text to be displayed by the {@link BorderFrame}'s {@link Label}
     * @param newText (String): new text to be displayed
     */
    public void setText(final String newText) {
        text.setValue(newText);
        label.setText(newText);
        refreshDisplay();
    }

    // font
    public ObjectProperty<Font> fontProperty() {
        return fontType;
    }

    /**
     * Gets the {@link Font} of the text displayed by the {@link BorderFrame}
     * @return (String): currently displayed text
     */
    public Font getFont() {
        return fontType.getValue();
    }

    /**
     * Sets the {@link Font} of the text to be displayed by the {@link BorderFrame}
     * @param newFont (Font): the new font
     */
    public void setFont(final Font newFont) {
        fontType.setValue(newFont);
        label.setFont(newFont);
        refreshDisplay();
    }

    // font size
    public DoubleProperty textSizeProperty() {
        return textSize;
    }

    /**
     * Gets the size of the text currently displayed by the {@link BorderFrame}
     * @return (double): size of the current text
     */
    public double getTextSize() {
        return textSize.get();
    }

    /**
     * Sets the size of the text currently displayed by the {@link BorderFrame}
     * @param newFontSize (double): new text size
     */
    public void setTextSize(final double newFontSize) {
        textSize.setValue(newFontSize);

        final String fontFamily = label.getFont().getFamily();
        final Font resizedFont  = new Font(fontFamily, newFontSize);
        label.setFont(resizedFont);

        refreshDisplay();
    }

    // text location
    public StringProperty textLocationProperty() {
        return (StringProperty) textLocation;
    }

    /**
     * Gets where text is being displayed in the {@link BorderFrame}
     * @return (String): "top", "right", "bottom", "left" or "center
     */
    public String getTextLocation() {
        return textLocation.getValue();
    }

    /**
     * Sets the text being displayed by the {@link BorderFrame}
     * @param newTextLocation (String): "top", "right", "bottom", "left", "center
     */
    public void setTextLocation(String newTextLocation) {
        textLocation.setValue(newTextLocation);
        refreshDisplay();
    }

    // ===================================
    //             RESIZING
    // ===================================

    private void resize() {
        final double minSize = corners[0].getSize() * 2;
        setMinSize(minSize, minSize);
    }

    // ===================================
    //            POSITIONING
    // ===================================

    private void refreshDisplay() {

        // waits for complete initialisation
        if (!hasInitialised) return;

        positionCorners();
        if (canDisplayText()) positionText(); else hideText();
    }

    private void positionCorners() {
        final double width = getWidth();
        final double height = getHeight();
        final double cornerSize = getCornerSize();

        final double deltaX     = width - cornerSize;
        final double deltaY     = height - cornerSize;

        // top right corner
        corners[1].setLayoutX(deltaX);
        corners[1].setLayoutY(0);

        // bottom right corner
        corners[2].setLayoutX(deltaX);
        corners[2].setLayoutY(deltaY);

        // bottom left corner
        corners[3].setLayoutX(0);
        corners[3].setLayoutY(deltaY);
    }

    private void positionText() {
        showText();

        final double frameWidth = getWidth();
        final double textWidth = label.getWidth();
        final double deltaX = (frameWidth - textWidth) / 2;

        switch (getTextLocation()) {
            case "top"    -> label.setLayoutX(deltaX);
            case "center" -> {
                final double frameHeight = getHeight();
                final double textHeight = label.getHeight();
                final double deltaY = (frameHeight - textHeight) / 2;

                label.setLayoutX(deltaX);
                label.setLayoutY(deltaY);
            }
            case "bottom" -> {
                final double frameHeight = getHeight();
                final double textHeight = label.getHeight();
                final double deltaY = frameHeight - textHeight;

                label.setLayoutX(deltaX);
                label.setLayoutY(deltaY);
            }
            case "left" -> {
                final double frameHeight = getHeight();
                final double textHeight = label.getHeight();
                final double deltaY = (frameHeight - textHeight) / 2;

                label.setLayoutY(deltaY);
            }
            case "right" -> {
                final double frameHeight = getHeight();
                final double textHeight = label.getHeight();
                final double deltaY = (frameHeight - textHeight) / 2;
                final double deltaXRight = frameWidth - textWidth;

                label.setLayoutX(deltaXRight);
                label.setLayoutY(deltaY);
            }
        }
    }

    private boolean canDisplayText() {

        return switch (getTextLocation()) {
            case "top", "center", "bottom" -> {
                final double textWidth      = label.getWidth();
                final double frameWidth     = getWidth();
                final double availableSpace = frameWidth - getCornerSize() * 2;

                yield  availableSpace >= textWidth && textWidth != 0;
            }
            case "left", "right" -> {
                final double textHeight     = label.getHeight();
                final double frameHeight    = getHeight();
                final double availableSpace = frameHeight - getCornerSize() * 2;

                yield availableSpace >= textHeight && textHeight != 0;
            }
            default -> false;
        };
    }

    /**
     * Hides the text displayed by the {@link BorderFrame}
     */
    public void hideText() {
        label.setVisible(false);
    }

    /**
     * Shows the text displayed by the BorderFrame
     */
    public void showText() {
        label.setVisible(true);
    }

    // ===================================
    //               CSS
    // ===================================


    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }
}
