package app.customControls.controls.corner;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.css.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

/**
 * A visual display signaling a corner or an edge<br><br>
 * <u><i>corner types</i></u> :
 * TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT<br><br>
 * <u><i>CSS Pseudo-class</i></u> : border-line<br>
 * <br>
 * <u><i>CSS Properties</i></u> :<br>
 * <ul>
 *     <li>
 *         -fx-size: number<br>
 *         the width & height of the borderline
 *     </li>
 *     <li>
 *         -fx-line-thickness: number<br>
 *         the thickness of the line
 *     </li>
 *     <li>
 *         -fx-arc-width: number<br>
 *         arc-width for the side of the corner
 *     </li>
 *     <li>
 *         -fx-arc-height: number<br>
 *         arc-height for the side of the corner
 *     </li>
 *     <li>
 *         -fx-fill: color<br>
 *         the color of the corner
 *     </li>
 * </ul>
 */
public class BorderLine extends Region {

    // ===================================
    //              FIELDS
    // ===================================

    /*          DEFAULT VALUES          */

    public static final double DEFAULT_SIZE = 50;
    public static final double DEFAULT_THICKNESS = 10;
    public static final double DEFAULT_ARC_WIDTH = 0;
    public static final double DEFAULT_ARC_HEIGHT = 0;
    public static final Color DEFAULT_COLOR = Color.BLACK;

    /*     CSS STYLEABLE PROPERTIES     */
    private static final StyleablePropertyFactory<BorderLine> FACTORY =
            new StyleablePropertyFactory<>(Region.getClassCssMetaData());

    // size
    private static final CssMetaData<BorderLine, Number> SIZE_CSS = FACTORY.createSizeCssMetaData(
            "-fx-size", corner -> corner.size, DEFAULT_SIZE, false);
    private final StyleableProperty<Number> size;

    // thickness
    private static final CssMetaData<BorderLine, Number> THICKNESS_CSS = FACTORY.createSizeCssMetaData(
            "-fx-line-thickness", corner -> corner.thickness, DEFAULT_THICKNESS, false
    );
    private final StyleableProperty<Number> thickness;

    // arc width
    private static final CssMetaData<BorderLine, Number> ARC_WIDTH_CSS = FACTORY.createSizeCssMetaData(
            "-fx-arc-width", corner -> corner.arcWidth, DEFAULT_ARC_WIDTH, false
    );
    private final StyleableProperty<Number> arcWidth;

    // arc height
    private static final CssMetaData<BorderLine, Number> ARC_HEIGHT_CSS = FACTORY.createSizeCssMetaData(
            "-fx-arc-height", corner -> corner.arcHeight, DEFAULT_ARC_HEIGHT, false
    );
    private final StyleableProperty<Number> arcHeight;

    // color
    private static final CssMetaData<BorderLine, Color> COLOR_CSS = FACTORY.createColorCssMetaData(
            "-fx-fill", corner -> corner.color, DEFAULT_COLOR, false
    );
    private final StyleableProperty<Color> color;

    /*              DISPLAY             */

    // canvas
    private Canvas canvas;
    private GraphicsContext context;

    // rectangle coordinates
    private Point2D[] rectangleCoordinates;

    // orientation
    private final ObjectProperty<Orientation> orientation;

    /*             LISTENERS            */

    private final ChangeListener<Number> sizeListener;
    private final ChangeListener<Number> thicknessListener;
    private final ChangeListener<Number> arcWidthListener;
    private final ChangeListener<Number> archHeightListener;
    private final ChangeListener<Color>  colorListener;

    // ===================================
    //           CONSTRUCTOR
    // ===================================

    /**
     * Constructor for the Corner class
     * @param orientation (Orientation): which way the corner is facing
     */
    public BorderLine(final Orientation orientation) {
        this(orientation, DEFAULT_SIZE, DEFAULT_THICKNESS, DEFAULT_COLOR);
    }

    /**
     * Constructor for the corner class
     * @param size (double): width & height of the corner
     * @param thickness (double): thickness of the corner lines, cannot be greater than size
     * @param orientation (Orientation): which way the corner is facing
     */
    public BorderLine(final Orientation orientation, final double size, final double thickness) {
        this(orientation, size, thickness, DEFAULT_COLOR);
    }

    /**
     * Constructor for the corner class
     * @param size (double): width & height of the corner
     * @param thickness (double): line thickness, cannot be greater than size
     * @param color (Color): line color
     * @param orientation (Orientation): which way the corner is facing
     */
    public BorderLine(final Orientation orientation, final double size, final double thickness, final Color color) {

        // initialises the display attributes
        this.size = new SimpleStyleableDoubleProperty(SIZE_CSS, this, "size");
        this.thickness = new SimpleStyleableDoubleProperty(THICKNESS_CSS, this, "thickness");
        this.arcHeight = new SimpleStyleableDoubleProperty(ARC_HEIGHT_CSS, this, "arc-height");
        this.arcWidth = new SimpleStyleableDoubleProperty(ARC_WIDTH_CSS, this, "arc-width");
        this.color = new SimpleStyleableObjectProperty<>(COLOR_CSS, this, "fill");
        this.orientation = new SimpleObjectProperty<>();

        // initialises listeners for css properties
        this.sizeListener = (observableValue, oldSize, newSize)           -> setSize(newSize.doubleValue());
        this.thicknessListener = (observableValue, oldThickness, newThickness) -> setThickness(newThickness.doubleValue());
        this.arcWidthListener = (observableValue, oldArc, newArc)             -> setArcWidth(newArc.doubleValue());
        this.archHeightListener = (observableValue, oldArc, newArc)             -> setArcHeight(newArc.doubleValue());
        this.colorListener = (observableValue, oldColor, newColor)         -> setColor(newColor);

        // saves the rectangle's format
        setSize(size);
        setThickness(thickness);
        setColor(color);
        setOrientation(orientation);

        // determines how to draw the corner
        determineRectangleCoordinates(orientation);

        // initialises the corner
        initialise();
        populate();

        // adds listeners for css property changes
        registerListeners();

        // displays the corner
        refreshDisplay();

    }

    // ===================================
    //           INITIALISING
    // ===================================

    /**
     * Sets up the corner
     */
    private void initialise() {
        getStyleClass().add("corner");
    }

    /**
     * Adds the necessary components to the corner
     */
    private void populate() {

        // canvas initialisation
        canvas = new Canvas(getSize(), getSize());
        context = canvas.getGraphicsContext2D();

        // pane
        Pane pane = new Pane(canvas);
        getChildren().add(pane);

    }

    /**
     * Registers css property listeners for every styleable property
     */
    private void registerListeners() {
        sizeProperty()      .addListener(sizeListener);
        thicknessProperty() .addListener(thicknessListener);
        arcWidthProperty()  .addListener(arcWidthListener);
        arcHeightProperty() .addListener(archHeightListener);
        colorProperty()     .addListener(colorListener);
    }

    // ===================================
    //              DISPLAY
    // ===================================

    /**
     * Determines where to place the rectangles making up the corner
     * @param orientation (Orientation): which way the corner is facing
     */
    private void determineRectangleCoordinates(Orientation orientation) {

        switch (orientation) {
            case TOP_LEFT, TOP -> {
                final Point2D hCoordinates = new Point2D(0, 0);
                final Point2D vCoordinates = new Point2D(0, 0);
                rectangleCoordinates = new Point2D[]{hCoordinates, vCoordinates};
            }
            case TOP_RIGHT, RIGHT -> {
                final Point2D hCoordinates = new Point2D(0, 0);
                final Point2D vCoordinates = new Point2D(getSize() - getThickness(), 0);
                rectangleCoordinates = new Point2D[]{hCoordinates, vCoordinates};
            }
            case BOTTOM_RIGHT, BOTTOM -> {
                final Point2D hCoordinates = new Point2D(0, getSize() - getThickness());
                final Point2D vCoordinates = new Point2D(getSize() - getThickness(), 0);
                rectangleCoordinates = new Point2D[]{hCoordinates, vCoordinates};
            }
            case BOTTOM_LEFT, LEFT -> {
                final Point2D hCoordinates = new Point2D(0, getSize() - getThickness());
                final Point2D vCoordinates = new Point2D(0, 0);
                rectangleCoordinates = new Point2D[]{hCoordinates, vCoordinates};
            }
        }

    }

    /**
     * Updates the corner's visuals
     */
    private void refreshDisplay() {

        // checks for graphical context initialisation
        if (context == null) return;

        // clears any previous graphics
        clearDisplay();

        // gets where to place the rectangles
        final Point2D hCoords = rectangleCoordinates[0];
        final Point2D vCoords = rectangleCoordinates[1];

        // sets the fill color
        context.setFill(getColor());

        // if rectangles have rounded corners
        if (shouldRound()) {
            // horizontal rectangles
            if (shouldDrawHorizontal()) drawRoundedHorizontalRect(hCoords);
            // vertical rectangle
            if (shouldDrawVertical())   drawRoundedVerticalRect(vCoords);
        }
        // normal rectangles
        else {
            // horizontal rectangle
            if (shouldDrawHorizontal()) drawHorizontalRect(hCoords);
            // vertical rectangle
            if (shouldDrawVertical())   drawVerticalRect(vCoords);
        }

    }

    /**
     * Resets the {@link BorderLine} canvas
     */
    private void clearDisplay() {
        context.clearRect(0, 0, getSize(), getSize());
    }

    /**
     * Returns true if arc width & arc height have been specified
     * @return (boolean): whether corner should be rounded
     */
    private boolean shouldRound() {
        return getArcHeight() != 0 || getArcWidth() != 0;
    }

    /**
     * Determines whether to draw the horizontal rectangle making up the {@link BorderLine}
     * @return (boolean): whether to draw the Borderline's horizontal rectangle
     */
    private boolean shouldDrawHorizontal() {
        return isHorizontal() || isCorner();
    }

    /**
     * Determines whether to draw the vertical rectangle making up the {@link BorderLine}
     * @return (boolean): whether to draw the BorderLine's vertical rectangle
     */
    private boolean shouldDrawVertical() {
        return isVertical() || isCorner();
    }

    /**
     * Determines if the {@link BorderLine} is a corner
     * @return (boolean): whether the BorderLine is a corner
     */
    private boolean isCorner() {
        return getOrientation().equals(Orientation.TOP_LEFT)
                || getOrientation().equals(Orientation.TOP_RIGHT)
                || getOrientation().equals(Orientation.BOTTOM_RIGHT)
                || getOrientation().equals(Orientation.BOTTOM_LEFT);
    }

    /**
     * Determines whether the {@link BorderLine} is a purely horizontal line<br>
     * (ie): it is situated at the top or the bottom
     * @return (boolean): whether the Borderline is a horizontal line
     */
    private boolean isHorizontal() {
        return getOrientation().equals(Orientation.TOP)
                || getOrientation().equals(Orientation.BOTTOM);
    }

    /**
     * Determines whether the {@link BorderLine} is a purely vertical line<br>
     * (ie): it is situated to the right or the left
     * @return (boolean): whether the BorderLine is a vertical line
     */
    private boolean isVertical() {
        return getOrientation().equals(Orientation.RIGHT)
                || getOrientation().equals(Orientation.LEFT);
    }

    /**
     * Handles drawing the horizontal part of the corner WITH ROUNDED EDGES
     * @param hCoords (Point2D): coordinates at which to draw the rectangle
     */
    private void drawRoundedHorizontalRect(final Point2D hCoords) {
        context.fillRoundRect(
                hCoords.getX(),
                hCoords.getY(),
                getSize(),
                getThickness(),
                getArcWidth(),
                getArcHeight()
        );
    }

    /**
     * Handles drawing the vertical part of the corner ROUNDED EDGES
     * @param vCoords (Point2D): coordinates at which to draw the rectangle
     */
    private void drawRoundedVerticalRect(final Point2D vCoords) {
        context.fillRoundRect(
                vCoords.getX(),
                vCoords.getY(),
                getThickness(),
                getSize(),
                getArcWidth(),
                getArcHeight()
        );
    }

    /**
     * Handles drawing the horizontal part of the corner
     * @param hCoords (Point2D): coordinates at which to draw the rectangle
     */
    private void drawHorizontalRect(final Point2D hCoords) {
        context.fillRect(
                hCoords.getX(),
                hCoords.getY(),
                getSize(),
                getThickness()
        );
    }

    /**
     * Handles drawing the vertical part of the corner
     * @param vCoords (Point2D): coordinates at which to draw the rectangle
     */
    private void drawVerticalRect(final Point2D vCoords) {
        context.fillRect(
                vCoords.getX(),
                vCoords.getY(),
                getThickness(),
                getSize()
        );
    }

    // ===================================
    //             ACCESSORS
    // ===================================

    /*     CSS STYLEABLE PROPERTIES     */

    /**
     * Size {@link Property} for the {@link BorderLine}
     * @return (DoubleProperty): the Borderline's size property
     */
    public DoubleProperty sizeProperty() {
        return (DoubleProperty) size;
    }

    /**
     * Getter for the {@link BorderLine}'s size<br>
     * (ie): its width & height
     * @return (double): the Borderline's size
     */
    public double getSize() {
        return size.getValue().doubleValue();
    }

    /**
     * Setter for the {@link BorderLine}'s size<br>
     * (ie): its width & height
     * @param newSize (double): the Borderline's new size
     */
    public void setSize(final double newSize) {
        // determines if a minimal width & height have already been set to the BorderLine...
        final boolean WIDTH_BOUNDS_SET = getMinWidth() != -1 && getMaxWidth() != -1;
        // ...and if that is the case, makes sure that the Borderline's new size is smaller than its max size
        if (WIDTH_BOUNDS_SET && (newSize < getMinWidth() || newSize > getMaxWidth())) return;

        // if the size is valid sets it
        size.setValue(newSize);

        // updates the canvas' size if it has been initialised
        if (canvas != null) {
            canvas.setWidth(newSize);
            canvas.setHeight(newSize);
        }

        // updates the Borderline's preferred size...
        setPrefSize(getSize(), getSize());
        // ...and updates the canvas
        refreshDisplay();
    }

    /**
     * Thickness {@link Property} for the {@link BorderLine}
     * @return (DoubleProperty): the Borderline's thickness property
     */
    public DoubleProperty thicknessProperty() {
        return (DoubleProperty) thickness;
    }

    /**
     * Getter for the {@link BorderLine}'s thickness
     * @return (double): the BorderLine's line thickness
     */
    public double getThickness() {
        return thickness.getValue().doubleValue();
    }

    /**
     * Setter for the {@link BorderLine}'s thickness
     * @param newThickness (double): the Borderline's new thickness
     */
    public void setThickness(final double newThickness) {
        // verifies that the BorderLine's new thickness is within the bounds of its size
        if (newThickness < 0 || newThickness > getSize()) return;

        // if the new thickness is valid, updates the BorderLines thickness to it...
        thickness.setValue(newThickness);
        // ...and updates the canvas
        refreshDisplay();
    }

    /**
     * Arc width {@link Property} for the {@link BorderLine}
     * @return (DoubleProperty): the BorderLine's war width property
     */
    public DoubleProperty arcWidthProperty() {
        return (DoubleProperty) arcWidth;
    }

    /**
     * Getter for the {@link BorderLine}'s arc width
     * @return (double): the Borderline's arc width
     */
    public double getArcWidth() {
        return arcWidth.getValue().doubleValue();
    }

    /**
     * Setter for the {@link BorderLine}'s arc width
     * @param newArcWidth (double): new arc height for the BorderLine
     */
    public void setArcWidth(final double newArcWidth) {
        // checks the arc width is valid...
        if (newArcWidth < 0) return;
        // ...and if that is the case applies it...
        arcWidth.setValue(newArcWidth);
        // ...and updates the canvas
        refreshDisplay();
    }

    /**
     * Arc height {@link Property} for the {@link BorderLine}
     * @return (DoubleProperty): the {@link BorderLine}'s arc height property
     */
    public DoubleProperty arcHeightProperty() {
        return (DoubleProperty) arcHeight;
    }

    /**
     * Getter for the {@link BorderLine}'s arc height
     * @return (double): the Borderline's arc height
     */
    public double getArcHeight() {
        return arcHeight.getValue().doubleValue();
    }

    /**
     * Setter for the {@link BorderLine}'s arc height
     * @param newArcHeight (double): the BorderLine's new arc height
     */
    public void setArcHeight(final double newArcHeight) {
        // checks the new arc height is valid...
        if (newArcHeight < 0) return;
        // ...if so applies it...
        arcHeight.setValue(newArcHeight);
        // ...and updates the canvas
        refreshDisplay();
    }

    /**
     * {@link Color} {@link Property} for the {@link BorderLine}
     * @return (ObjectProperty(Color)): the BorderLine's color property
     */
    public ObjectProperty<Color> colorProperty() {
        return (ObjectProperty<Color>) color;
    }

    /**
     * Getter for the {@link BorderLine}'s {@link Color}
     * @return (Color): the BorderLine's Color
     */
    public Color getColor() {
        return color.getValue();
    }

    /**
     * Setter for the {@link BorderLine}'s {@link Color}
     * @param newColor ({@link Color}): the BorderLine's new color
     */
    public void setColor(final Color newColor) {
        // checks that the color is valid, if so sets it as the BorderLine's new color...
        color.setValue(Objects.requireNonNullElse(newColor, DEFAULT_COLOR));

        // ...and updates the canvas
        refreshDisplay();
    }

    /**
     * {@link Orientation} {@link Property} for the {@link BorderLine}
     * @return (ObjectProperty(Orientation)): the BorderLine's orientation property
     */
    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    /**
     * Getter for the {@link BorderLine}'s {@link Orientation}
     * @return (Orientation): the BorderLine's orientation
     */
    public Orientation getOrientation() {
        return orientation.get();
    }

    /**
     * Setter for the {@link BorderLine}'s {@link Orientation}
     * @param newOrientation ({@link Orientation}): the Borderline's new orientation
     */
    public void setOrientation(Orientation newOrientation) {
        // updates the Borderline's orientation
        orientation.set(newOrientation);
        // recalculates where to draw the Borderline's rectangle...
        determineRectangleCoordinates(newOrientation);
        // ...and updates the canvas
        refreshDisplay();
    }

    // ===================================
    //              STYLING
    // ===================================

    /*          CSS STYLESHEETS         */

    @Override
    public String getUserAgentStylesheet() {
        return BorderLine.class.getResource("/app/customControls/style/corner.css").toExternalForm();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    /**
     * Used for extension by child classes
     * @return (List&lt CssMetaData&lt ? extends Styleable &gt&gt): css metadata for this class
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

}
