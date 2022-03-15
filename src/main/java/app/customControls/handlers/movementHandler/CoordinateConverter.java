package app.customControls.handlers.movementHandler;

import javafx.geometry.Point2D;

/**
 * Functional Interface used in {@link MovementHandler} class
 */
public interface CoordinateConverter {
    Point2D convertCoordinates(final Point2D coordinates);
}
