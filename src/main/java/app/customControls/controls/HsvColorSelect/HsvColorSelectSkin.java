package app.customControls.controls.HsvColorSelect;

import app.customControls.handlers.movementHandler.CoordinateConverter;
import app.customControls.handlers.movementHandler.MovementBounds;
import app.customControls.handlers.movementHandler.MovementHandler;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import java.nio.IntBuffer;

/**
 * Handles display logic for {@link HsvColorSelect} class
 */
public class HsvColorSelectSkin extends SkinBase<HsvColorSelect> implements Skin<HsvColorSelect> {

    // ===================================
    //              FIELDS
    // ===================================

    /*           DEFAULT VALUES         */

    private static final double DEFAULT_POINTER_SIZE = 10;
    private static final String HUE = "hue";
    private static final String SATURATION = "sat";
    private static final String VALUE = "val";

    /*            COMPONENTS            */

    private Pane displayPane;
    private final Region pointer;
    private final Canvas hsvDisplay;
    private final GraphicsContext context;

    private final HsvColorSelect hsvColorSelect;

    /*             MOVEMENT             */

    private final MovementHandler pointerMovement;
    private final CoordinateConverter converter;

    /*             LISTENERS            */

    private final InvalidationListener resizeListener;
    private final InvalidationListener hueListener;
    private final InvalidationListener saturationListener;
    private final InvalidationListener valueListener;
    private final EventHandler<MouseEvent> repositionListener;

    /*              GRADIENT            */
    private final HsvGradient gradient;

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    /**
     * {@link HsvColorSelectSkin} constructor
     * @param hsvColorSelect ({@link HsvColorSelect}): HsvColorSelect the skin is associated to
     * @param pointer ({@link Region}): the pointer used to select the current color
     * @param pointerMovement ({@link MovementHandler}): MovementHandler responsible for the pointer's movement
     */
    protected HsvColorSelectSkin(HsvColorSelect hsvColorSelect, Region pointer, MovementHandler pointerMovement) {
        super(hsvColorSelect);

        // saves the associated HsvColorSelect
        this.hsvColorSelect     = hsvColorSelect;

        // initialises listeners
        this.resizeListener = observable -> resize();
        this.hueListener = observable -> SynchroniseGradientHsv(HUE);
        this.saturationListener = observable -> SynchroniseGradientHsv(SATURATION);
        this.valueListener = observable -> SynchroniseGradientHsv(VALUE);
        this.repositionListener = this::repositionOnClick;

        // specifies the CoordinateConverter to be used for the pointer's MovementHandler
        this.converter          = this::convertCoordinates;

        // initialises the HsvColorSelect's gradient
        this.gradient = new HsvGradient(
                (int) hsvColorSelect.getWidth(),
                (int) hsvColorSelect.getHeight(),
                (int) hsvColorSelect.getHue(),
                (int) hsvColorSelect.getSaturation(),
                (int) hsvColorSelect.getValue()
        );

        // saves the pointer & its movement handler
        this.pointer = pointer;
        this.pointerMovement = pointerMovement;
        // initialises the canvas where the hsv spectrum will be drawn
        this.hsvDisplay = new Canvas();
        this.context = hsvDisplay.getGraphicsContext2D();

        initialise();
        populate();

        registerListeners();

        redraw();

        Platform.runLater(this::initialiseBounds);

    }

    // ===================================
    //          INITIALISATION
    // ===================================

    /**
     * Adds the necessary style classes to components
     */
    private void initialise() {
        // color select
        hsvColorSelect.getStyleClass().add("hsv-color-select");

        // pointer
        pointer.getStyleClass().add("pointer");
    }

    /**
     * Registers the components as children
     */
    private void populate() {

        // pointer
        pointer.setMinSize(DEFAULT_POINTER_SIZE, DEFAULT_POINTER_SIZE);
        pointer.setPrefSize(DEFAULT_POINTER_SIZE, DEFAULT_POINTER_SIZE);

        // display
        hsvDisplay.setWidth(hsvColorSelect.getWidth());
        hsvDisplay.setHeight(hsvColorSelect.getHeight());

        // registering nodes as children
        displayPane = new Pane();
        displayPane.getChildren().addAll(hsvDisplay, pointer);
        getChildren().add(displayPane);
    }

    /**
     * Registers listeners for the various components making up the {@link HsvColorSelect}
     */
    private void registerListeners() {
        // resizing
        hsvColorSelect.widthProperty()     .addListener(resizeListener);
        hsvColorSelect.heightProperty()    .addListener(resizeListener);

        // movement
        hsvDisplay.setOnMousePressed(repositionListener);

        // value changes
        hsvColorSelect.hueProperty()       .addListener(hueListener);
        hsvColorSelect.saturationProperty().addListener(saturationListener);
        hsvColorSelect.valueProperty()     .addListener(valueListener);
    }

    // ===================================
    //              BOUNDS
    // ===================================

    /**
     * Calculates the {@link MovementBounds} for the pointer's {@link MovementHandler}
     */
    private void initialiseBounds() {
        // sets the new movement bounds...
        pointerMovement.setMovementBounds(calculateBounds());
        // ...and activates them
        pointerMovement.bindToSurroundings();

        // moves the pointer to its starting position
        pointerMovement.moveToMin();
        // sets the coordinate converter to take into account bounds not being aligned with the canvas
        pointerMovement.setCoordinateConverter(converter);

        // marks the HsvColorSelect as having finished generating
        hsvColorSelect.validateGeneration();
    }

    /**
     * Calculates the {@link MovementBounds} for the hsv pointer
     * @return (MovementBounds): the hsv pointer's movement bounds
     * @implNote goal is to have the center of the pointer being able to touch the edges & corners of the hsv spectrum.
     * This means that the pointer is actually able to move into the negatives, since its position is relative to its
     * top-left corner. It cannot either move the full length of the hsv spectrum, since that would result in it exiting
     * the spectrum entirely.
     */
    private MovementBounds calculateBounds() {

        // calculates the min & max bounds along the x-axis
        final double[] boundsX = calculateBoundsX();
        // calculates the min & max bounds along the y-axis
        final double[] boundsY = calculateBoundsY();

        // returns the complete movement bounds
        return new MovementBounds(boundsX[0], boundsX[1], boundsY[0], boundsY[1]);
    }

    /**
     * Calculates the hsv pointer min & max movement range along the x-axis
     * @return (double[]): the hsv pointer's x-axis min & max movement range
     */
    private double[] calculateBoundsX() {
        final double centerX = pointer.getWidth() / 2;
        final double minX = -centerX;
        final double maxX = hsvDisplay.getWidth() - centerX;

        return new double[]{minX, maxX};
    }

    /**
     * Calculates the hsv pointer min & max movement range along the y-axis
     * @return (double[]): the hsv pointer's y-axis min & max movement range
     */
    private double[] calculateBoundsY() {
        final double centerY    = pointer.getHeight() / 2;
        final double minY       = -centerY;
        final double maxY       = hsvDisplay.getHeight() - centerY;

        return new double[]{minY, maxY};
    }

    // ===================================
    //             RESIZING
    // ===================================

    /**
     * Handles resizing the various {@link HsvColorSelect} components
     */
    private void resize() {

        // saves the HsvColorSelect's previous size
        final Rectangle2D oldSize = sizeSnapshot();

        // determines the HsvColorSelect's new size
        final double width  = hsvColorSelect.getWidth();
        final double height = hsvColorSelect.getHeight();

        // resizes the canvas to fit the new size
        hsvDisplay.setWidth(width);
        hsvDisplay.setHeight(height);

        // resizes the clip to hide the any object outside the canvas (the pointer in this case)
        displayPane.setClip(new Rectangle(width, height));

        // sets the size of the gradient to match the size of the canvas
        gradient.setWidth((int) width);
        gradient.setHeight((int) height);

        // updates the pointer's movement bounds to still allow it to move normally along the hsv spectrum
        updateBounds();
        // repositions the pointer proportionally to conserve previous color
        repositionPointer(oldSize);
        // redraws the gradient
        redraw();
    }

    /**
     * Saves the current size of the {@link HsvColorSelect}
     * @return (Point2D): the current size of the HsvColorSelect
     */
    private Rectangle2D sizeSnapshot() {
        return new Rectangle2D(0, 0, hsvDisplay.getWidth(), hsvDisplay.getHeight());
    }

    /**
     * Updates the pointer's movement bounds to match a change in size of the HsvColorSelect
     */
    private void updateBounds() {
        pointerMovement.setMovementBounds(calculateBounds());
    }

    /**
     * Repositions the pointer proportionally to resize so its stays focused on the same color
     * @param oldSize ({@link javafx.geometry.Rectangle2D Rectangle2D}): previous size of the {@link HsvColorSelect}
     */
    private void repositionPointer(final Rectangle2D oldSize) {

        // gets the pointer's current position
        final double pointerX = pointerMovement.getPointerX();
        final double pointerY = pointerMovement.getPointerY();
        // calculates the pointer's progression along the hsv gradient before the resize
        final double ratioX = pointerX / oldSize.getWidth();
        final double ratioY = pointerY / oldSize.getHeight();
        // determines where to place the pointer after the resize to conserve that progression
        final double scaledX = hsvDisplay.getWidth() * ratioX;
        final double scaledY = hsvDisplay.getHeight() * ratioY;

        // moves the pointer to the correct position
        pointerMovement.moveTo(scaledX, scaledY);

    }

    // ===================================
    //              DISPLAY
    // ===================================

    /**
     * Handles redrawing the hsv gradient
     */
    private void redraw() {

        // wipes the hsv gradient
        resetDisplay();

        // gets the canvas' pixel writer
        PixelWriter writer = context.getPixelWriter();
        // gets and integer buffer corresponding to the current slice of the hsv spectrum
        int[] slice = gradient.sliceOf((int) hsvColorSelect.getHue());

        // determines the size of the HsvColorSelect
        final int width = (int) hsvColorSelect.getWidth();
        final int height = (int) hsvColorSelect.getHeight();

        // gets the correct pixel format for writing
        WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbInstance();

        // write the integer buffer to the canvas
        writer.setPixels(0, 0, width, height, pixelFormat, slice, 0, width);

    }

    /**
     * Wipes the hsv gradient blank
     */
    private void resetDisplay() {
        context.clearRect(0, 0, hsvDisplay.getWidth(), hsvDisplay.getHeight());
    }

    /**
     * Synchronises the gradient's Hsv values to match that of the {@link HsvColorSelect}
     * @param valueType ({@link String}): the type of value to tbe updated ["hue" | "sat" | "val" ]
     */
    private void SynchroniseGradientHsv(final String valueType) {
        // updates the gradient's hue, saturation or value
        switch (valueType) {
            case "hue" -> gradient.setHue((int) hsvColorSelect.getHue());
            case "sat" -> gradient.setSaturation((int) hsvColorSelect.getSaturation());
            case "val" -> gradient.setValue((int) hsvColorSelect.getValue());
        }

        // redraws the gradient to match the changes
        redraw();
    }

    // ===================================
    //            POSITIONING
    // ===================================

    /**
     * Sets the pointer to the mouse's position when the user clicks on the hsv spectrum
     * @param mouseEvent ({@link MouseEvent}): the event resulting from clicking the mouse
     */
    private void repositionOnClick(MouseEvent mouseEvent) {
        pointerMovement.moveTo(mouseEvent.getX(), mouseEvent.getY());
    }

    /**
     * Handles converting the pointer's coordinates to take into consideration offset {@link MovementBounds}
     * @param coordinates ({@link Point2D}): actual current pointer coordinates
     * @return (Point2D): adjusted pointer coordinates to take into account offset
     */
    private Point2D convertCoordinates(Point2D coordinates) {

        final double x = coordinates.getX();
        final double y = coordinates.getY();
        final double deltaX = pointer.getWidth() / 2;
        final double deltaY = pointer.getHeight() / 2;

        return new Point2D(x + deltaX, y + deltaY);
    }

}
