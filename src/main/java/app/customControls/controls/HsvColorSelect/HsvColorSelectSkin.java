package app.customControls.controls.HsvColorSelect;

import app.customControls.handlers.movementHandler.CoordinateConverter;
import app.customControls.handlers.movementHandler.MovementBounds;
import app.customControls.handlers.movementHandler.MovementHandler;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
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
    private Region  pointer;
    private Canvas hsvDisplay;
    private GraphicsContext context;

    private final HsvColorSelect hsvColorSelect;

    /*             MOVEMENT             */

    private final MovementHandler pointerMovement;
    private final CoordinateConverter converter;

    /*             LISTENERS            */

    private final InvalidationListener resizeListener;
    private final InvalidationListener movementListener;
    private final InvalidationListener hueListener;
    private final InvalidationListener saturationListener;
    private final InvalidationListener valueListener;
    private final EventHandler<MouseEvent> repositionListener;

    /*              GRADIENT            */
    private final HsvGradient gradient;

    // ===================================
    //            CONSTRUCTOR
    // ===================================
    protected HsvColorSelectSkin(HsvColorSelect hsvColorSelect, Region pointer, MovementHandler pointerMovement) {
        super(hsvColorSelect);

        this.hsvColorSelect     = hsvColorSelect;

        this.resizeListener = observable -> resize();
        this.hueListener = observable -> updateValues(HUE);
        this.saturationListener = observable -> updateValues(SATURATION);
        this.valueListener = observable -> updateValues(VALUE);
        this.movementListener = this::onMovement;
        this.repositionListener = this::repositionOnClick;

        this.converter          = this::convertCoordinates;

        this.gradient = new HsvGradient(
                (int) hsvColorSelect.getWidth(),
                (int) hsvColorSelect.getHeight(),
                (int) hsvColorSelect.getHue(),
                (int) hsvColorSelect.getSaturation(),
                (int) hsvColorSelect.getValue()
        );

        this.pointer = pointer;
        this.pointerMovement = pointerMovement;
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

    private void initialise() {
        // color select
        hsvColorSelect.getStyleClass().add("hsv-color-select");

        // pointer
        pointer.getStyleClass().add("pointer");
    }

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

    private void registerListeners() {
        // resizing
        hsvColorSelect.widthProperty()     .addListener(resizeListener);
        hsvColorSelect.heightProperty()    .addListener(resizeListener);

        // movement
        hsvDisplay.setOnMousePressed(repositionListener);
        pointer.layoutXProperty()          .addListener(movementListener);
        pointer.layoutYProperty()          .addListener(movementListener);

        // value changes
        hsvColorSelect.hueProperty()       .addListener(hueListener);
        hsvColorSelect.saturationProperty().addListener(saturationListener);
        hsvColorSelect.valueProperty()     .addListener(valueListener);
    }

    // ===================================
    //              BOUNDS
    // ===================================

    private void initialiseBounds() {
        pointerMovement.setMovementBounds(calculateBounds());
        pointerMovement.bindToSurroundings();

        pointerMovement.moveToMin();
        pointerMovement.setCoordinateConverter(converter);

        hsvColorSelect.validateGeneration();
    }

    private double[] calculateBoundsX() {
        final double centerX    = pointer.getWidth() / 2;
        final double minX       = -centerX;
        final double maxX       = hsvDisplay.getWidth() - centerX;

        return new double[]{minX, maxX};
    }

    private double[] calculateBoundsY() {
        final double centerY    = pointer.getHeight() / 2;
        final double minY       = -centerY;
        final double maxY       = hsvDisplay.getHeight() - centerY;

        return new double[]{minY, maxY};
    }

    private MovementBounds calculateBounds() {

        final double[] boundsX = calculateBoundsX();
        final double[] boundsY = calculateBoundsY();

        return new MovementBounds(boundsX[0], boundsX[1], boundsY[0], boundsY[1]);
    }

    // ===================================
    //             RESIZING
    // ===================================

    private void resize() {

        final Point2D oldSize = sizeSnapshot();

        final double width  = hsvColorSelect.getWidth();
        final double height = hsvColorSelect.getHeight();

        hsvDisplay.setWidth(width);
        hsvDisplay.setHeight(height);

        displayPane.setClip(new Rectangle(width, height));

        gradient.setWidth((int) width);
        gradient.setHeight((int) height);

        updateBounds();
        repositionPointer(oldSize);
        redraw();
    }

    private Point2D sizeSnapshot() {
        return new Point2D(hsvDisplay.getWidth(), hsvDisplay.getHeight());
    }

    private void updateBounds() {
        pointerMovement.setMovementBounds(calculateBounds());
    }

    private void repositionPointer(final Point2D oldSize) {

        final double pointerX   = pointerMovement.getPointerX();
        final double pointerY   = pointerMovement.getPointerY();
        final double ratioX     = pointerX / oldSize.getX();
        final double ratioY     = pointerY / oldSize.getY();
        final double scaledX    = hsvDisplay.getWidth() * ratioX;
        final double scaledY    = hsvDisplay.getHeight() * ratioY;

        pointerMovement.moveTo(scaledX, scaledY);

    }

    // ===================================
    //              DISPLAY
    // ===================================

    private void redraw() {

        resetDisplay();

        PixelWriter writer                         = context.getPixelWriter();
        int[] slice                                = gradient.sliceOf((int) hsvColorSelect.getHue());

        final int width                            = (int) hsvColorSelect.getWidth();
        final int height                           = (int) hsvColorSelect.getHeight();
        WritablePixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbInstance();

        writer.setPixels(0, 0, width, height, pixelFormat, slice, 0, width);

    }

    private void resetDisplay() {
        context.clearRect(0, 0, hsvDisplay.getWidth(), hsvDisplay.getHeight());
    }

    private void updateValues(final String valueType) {
        switch (valueType) {
            case "hue" -> gradient.setHue((int) hsvColorSelect.getHue());
            case "sat" -> gradient.setSaturation((int) hsvColorSelect.getSaturation());
            case "val" -> gradient.setValue((int) hsvColorSelect.getValue());
        }
        redraw();
    }

    // ===================================
    //            POSITIONING
    // ===================================

    private void repositionOnClick(MouseEvent mouseEvent) {
        pointerMovement.moveTo(mouseEvent.getX(), mouseEvent.getY());
    }

    private Point2D convertCoordinates(Point2D coordinates) {

        final double x = coordinates.getX();
        final double y = coordinates.getY();
        final double deltaX = pointer.getWidth() / 2;
        final double deltaY = pointer.getHeight() / 2;

        return new Point2D(x + deltaX, y + deltaY);
    }

    private void onMovement(Observable observable) {

    }

    // ===================================
    //             ACCESSORS
    // ===================================

}
