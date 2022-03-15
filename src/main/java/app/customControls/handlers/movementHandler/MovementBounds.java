package app.customControls.handlers.movementHandler;

import javafx.geometry.Point2D;

/**
 * Specifies bounding limits to a node's movement inside a {@link MovementHandler}
 * @implNote Class was mainly created for learning purposes, reusing the existing JFX {@link javafx.geometry.Bounds Bounds}
 * would have been much more judicious
 */
public class MovementBounds implements Cloneable{

    // ===================================
    //              FIELDS
    // ===================================

    /*             CONSTANTS            */
    private static final double DEFAULT_MIN = 0;

    /*              BOUNDS              */
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    public MovementBounds(final double maxX, final double maxY) {
        this(DEFAULT_MIN, maxX, DEFAULT_MIN, maxY);
    }

    public MovementBounds(final double minX, final double maxX, final double minY, final double maxY) {

        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;

    }

    // ===================================
    //             ACCESSORS
    // ===================================

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    // ===================================
    //            BOUNDARIES
    // ===================================

    public boolean isInBounds(final Point2D point2D) {
        return isInBounds(point2D.getX(), point2D.getY());
    }

    public boolean isInBounds(final double x, final double y) {

        final boolean xInBounds = (x >= minX && x <= maxX);
        final boolean yInBounds = (y >= minY && y <= maxY);

        return xInBounds && yInBounds ;
    }

    // ===================================
    //              CLONING
    // ===================================

    public MovementBounds clone() {
        return new MovementBounds(maxX, maxY, minX, minY);
    }

}
