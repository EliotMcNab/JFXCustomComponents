package app.customControls.handlers.movementHandler;

import app.customControls.handlers.delay.DelayHandler;
import app.customControls.utilities.MathUtil;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Handles dragging a {@link Node} around its {@link MovementBounds}
 */
public class MovementHandler {

    // ===================================
    //              FIELDS
    // ===================================

    /*             CONSTANTS            */

    private static final long MOVEMENT_DELAY = 10; // delay between movement updates, in millis
    private static final boolean BOUND_BY_DEFAULT = false;
    private static final boolean CENTERED_BY_DEFAULT = false;
    private static final MovementBounds DEFAULT_BOUNDS = new MovementBounds(Double.MAX_VALUE, Double.MAX_VALUE);

    /*            CONVERSION            */
    private static final CoordinateConverter DEFAULT_CONVERTER = coordinates -> coordinates;
    private CoordinateConverter converter;

    /*          ASSOCIATED NODE         */
    private final Node associatedNode;

    /*              BOUNDS              */
    private MovementBounds bounds;
    private boolean isBound;
    private boolean isCentered;

    /*             LISTENERS            */
    private final EventHandler<MouseEvent> dragListener;

    /*               DELAY              */
    private final DelayHandler delay = new DelayHandler(MOVEMENT_DELAY);

    // ===================================
    //            CONSTRUCTOR
    // ===================================s

    public MovementHandler(final Node associatedNode) {
        this(associatedNode, DEFAULT_BOUNDS, BOUND_BY_DEFAULT);
    }

    public MovementHandler(final Node associatedNode, final MovementBounds bounds) {
        this(associatedNode, bounds, true);
    }

    public MovementHandler(final Node associatedNode, final MovementBounds bounds, final boolean isBound) {
        this(associatedNode, bounds, isBound, CENTERED_BY_DEFAULT);
    }

    public MovementHandler(final Node associatedNode, final MovementBounds bounds, final boolean isBound, final boolean isCentered) {

        this.associatedNode = associatedNode;
        this.bounds         = bounds;
        this.isBound        = isBound;
        this.isCentered     = isCentered;

        this.dragListener   = this::moveTo;

        registerListeners();

    }

    // ===================================
    //             LISTENERS
    // ===================================

    private void registerListeners() {associatedNode.setOnMouseDragged(dragListener);}

    // ===================================
    //             BOUNDARIES
    // ===================================

    public void bindToSurroundings() {
        isBound = true;
    }

    public void unBind() {
        isBound = false;
    }

    public boolean isBound() {
        return isBound;
    }

    private Point2D boundCoordinates(Point2D parentCoords) {

        final double x      = parentCoords.getX();
        final double y      = parentCoords.getY();

        final double boundX = MathUtil.clamp(x, bounds.getMinX(), bounds.getMaxX());
        final double boundY = MathUtil.clamp(y, bounds.getMinY(), bounds.getMaxY());

        return new Point2D(boundX, boundY);

    }

    // ===================================
    //             MOVEMENT
    // ===================================

    private void moveTo(MouseEvent mouseEvent) {

        if (!delay.hasElapsed()) return;

        final double localX             = mouseEvent.getX();
        final double localY             = mouseEvent.getY();
        Point2D parentCoords            = associatedNode.localToParent(localX, localY);

        if (isCentered) parentCoords    = centerOnMouse(parentCoords);
        if (isBound) parentCoords       = boundCoordinates(parentCoords);

        associatedNode.setLayoutX(parentCoords.getX());
        associatedNode.setLayoutY(parentCoords.getY());

    }

    private Point2D centerOnMouse(final Point2D target) {
        final double center = associatedNode.getLayoutBounds().getCenterX();
        return target.subtract(center, center);
    }

    public void moveToX(final double x) {
        final Point2D newPosition = new Point2D(x, bounds.getMinY());
        // TODO: get bounds to work again
        // if (!bounds.isInBounds(newPosition)) return;

        Point2D adjustedPosition         = newPosition;
        if (isCentered) adjustedPosition = centerOnMouse(newPosition);

        associatedNode.setLayoutX(adjustedPosition.getX());
    }

    public void moveToY(final double y) {
        final Point2D newPosition = new Point2D(bounds.getMinX(), y);
        // TODO: get bounds to work again
        // if (!bounds.isInBounds(newPosition)) return;

        Point2D adjustedPosition         = newPosition;
        if (isCentered) adjustedPosition = centerOnMouse(newPosition);

        associatedNode.setLayoutY(adjustedPosition.getY());
    }

    public void moveTo(final double x, final double y) {
        moveTo(new Point2D(x, y));
    }

    public void moveTo(final Point2D newPosition) {
        if (!bounds.isInBounds(newPosition)) return;

        Point2D adjustedPosition         = newPosition;
        if (isCentered) adjustedPosition = centerOnMouse(newPosition);

        associatedNode.setLayoutX(adjustedPosition.getX());
        associatedNode.setLayoutY(adjustedPosition.getY());
    }

    public void moveToMinX() {
        associatedNode.setLayoutX(bounds.getMinX());
    }

    public void moveToMaxX() {
        associatedNode.setLayoutX(bounds.getMaxX());
    }

    public void moveToMinY() {
        associatedNode.setLayoutY(bounds.getMinY());
    }

    public void moveToMaxY() {
        associatedNode.setLayoutY(bounds.getMaxY());
    }

    public void moveToMin() {
        moveToMinX();
        moveToMinY();
    }

    public void moveToMax() {
        moveToMaxX();
        moveToMaxY();
    }

    // ===================================
    //             ACCESSORS
    // ===================================

    public CoordinateConverter getConverter() {
        return converter;
    }

    public void setCoordinateConverter(CoordinateConverter newConverter) {
        converter = newConverter;
    }

    public MovementBounds getBounds() {
        return bounds.clone();
    }

    public void setMovementBounds(final MovementBounds newBounds) {
        bounds = newBounds;
    }

    public boolean isCentered() {
        return isCentered;
    }

    public void setCentered(final boolean isCentered) {
        this.isCentered = isCentered;
    }

    public double getPointerX() {
        return getPointerPosition().getX();
    }

    public double getPointerY() {
        return getPointerPosition().getY();
    }

    public Point2D getPointerPosition() {

        final double x = associatedNode.getLayoutX();
        final double y = associatedNode.getLayoutY();
        final Point2D position = new Point2D(x, y);

        if (converter == null)  return position;
        else                    return converter.convertCoordinates(position);
    }

}
