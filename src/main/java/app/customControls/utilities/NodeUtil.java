package app.customControls.utilities;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

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

    public static void setNodeSize(final Node node, final double width, final double height) {
        // resizes the node depending on its type
        if (node instanceof Region) {
            ((Region) node).setPrefWidth(Math.abs(width));
            ((Region) node).setPrefHeight(Math.abs(height));
        } else if (node instanceof Rectangle) {
            ((Rectangle) node).setWidth(Math.abs(width));
            ((Rectangle) node).setHeight(Math.abs(height));
        } else if (node instanceof Circle) {
            ((Circle) node).setRadius(Math.abs(width / 2));
        }
    }
}
