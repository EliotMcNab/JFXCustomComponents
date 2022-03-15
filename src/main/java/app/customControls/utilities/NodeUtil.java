package app.customControls.utilities;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

/**
 * Collection of helper methods for handling JavaFx {@link Node Nodes}
 */
public class NodeUtil {
    public static void positionAt(final Node node, final Point2D coordinates) {
        positionAt(node, coordinates.getX(), coordinates.getY());
    }

    public static void positionAt(final Node node, final double x, final double y) {
        node.setLayoutX(x);
        node.setLayoutY(y);
    }

    public static Rectangle2D getActualNodeSize(final Node node, Insets insets) {
        final double width        = node.getBoundsInParent().getWidth();
        final double height       = node.getBoundsInParent().getHeight();

        final double actualWidth  = width + insets.getLeft() + insets.getRight();
        final double actualHeight = height + insets.getTop() + insets.getBottom();

        return new Rectangle2D(0, 0, actualWidth, actualHeight);
    }

    public static Point2D nodeScreenCoordinates(final Node node) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        return new Point2D(bounds.getMinX(), bounds.getMinY());
    }
}
