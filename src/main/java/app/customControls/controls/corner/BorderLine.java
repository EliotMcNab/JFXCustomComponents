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

/**
 * A visual display signaling a corner or an edge<br><br>
 * <u><i>corner types</i></u> :
 * TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT<br><br>
 * <u><i>css properties</i></u> :<br>
 * <list>
 *     <li>-fx-size: borderline width & height</li>
 *     <li>-fx-line-thickness: line thickness</li>
 *     <li>-fx-arc-width/-fx-arc-height: rounding for corners</li>
 *     <li>-fx-fill: fill color for the line</li>
 * </list>
 */
public class BorderLine extends Region {

    // ===================================
    //              FIELDS
    // ===================================

    /*          DEFAULT VALUES          */
    public static final double DEFAULT_SIZE        = 50;
    public static final double DEFAULT_THICKNESS   = 10;
    public static final double DEFAULT_ARC_WIDTH   = 0;
    public static final double DEFAULT_ARC_HEIGHT  = 0;
    public static final Color  DEFAULT_COLOR       = Color.BLACK;

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
    private ObjectProperty<Orientation> orientation;

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

        SimpleDoubleProperty test = new SimpleDoubleProperty();

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

    private boolean shouldDrawHorizontal() {
        return isHorizontal() || isCorner();
    }

    private boolean shouldDrawVertical() {
        return isVertical() || isCorner();
    }

    private boolean isCorner() {
        return getOrientation().equals(Orientation.TOP_LEFT)
                || getOrientation().equals(Orientation.TOP_RIGHT)
                || getOrientation().equals(Orientation.BOTTOM_RIGHT)
                || getOrientation().equals(Orientation.BOTTOM_LEFT);
    }

    private boolean isHorizontal() {
        return getOrientation().equals(Orientation.TOP)
                || getOrientation().equals(Orientation.BOTTOM);
    }

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

    // size
    public DoubleProperty sizeProperty() {
        return (DoubleProperty) size;
    }

    public double getSize() {
        return size.getValue().doubleValue();
    }

    public void setSize(final double newSize) {
        final boolean WIDTH_BOUNDS_SET = getMinWidth() != -1 && getMaxWidth() != -1;
        if (WIDTH_BOUNDS_SET && (newSize < getMinWidth() || newSize > getMaxWidth())) return;

        size.setValue(newSize);

        if (canvas != null) {
            canvas.setWidth(newSize);
            canvas.setHeight(newSize);
        }

        setPrefSize(getSize(), getSize());
        refreshDisplay();
    }

    // thickness
    public DoubleProperty thicknessProperty() {
        return (DoubleProperty) thickness;
    }

    public double getThickness() {
        return thickness.getValue().doubleValue();
    }

    public void setThickness(final double newThickness) {
        if (newThickness < 0 || newThickness > getSize()) return;

        thickness.setValue(newThickness);
        refreshDisplay();
    }

    // arc width
    public DoubleProperty arcWidthProperty() {
        return (DoubleProperty) arcWidth;
    }

    public double getArcWidth() {
        return arcWidth.getValue().doubleValue();
    }

    public void setArcWidth(final double newArcWidth) {
        if (newArcWidth < 0) return;
        arcWidth.setValue(newArcWidth);
        refreshDisplay();
    }

    // arc height
    public DoubleProperty arcHeightProperty() {
        return (DoubleProperty) arcHeight;
    }

    public double getArcHeight() {
        return arcHeight.getValue().doubleValue();
    }

    public void setArcHeight(final double newArcHeight) {
        if (newArcHeight < 0) return;
        arcHeight.setValue(newArcHeight);
        refreshDisplay();
    }

    // color
    public ObjectProperty<Color> colorProperty() {
        return (ObjectProperty<Color>) color;
    }

    public Color getColor() {
        return color.getValue();
    }

    public void setColor(final Color newColor) {
        if (newColor == null) {
            color.setValue(DEFAULT_COLOR);
        } else {
            color.setValue(newColor);
        }
        refreshDisplay();
    }

    // orientation
    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public void setOrientation(Orientation newOrientation) {
        orientation.set(newOrientation);
        determineRectangleCoordinates(newOrientation);
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
