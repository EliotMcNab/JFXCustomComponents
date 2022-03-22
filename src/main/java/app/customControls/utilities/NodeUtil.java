package app.customControls.utilities;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
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

    public static void optimiseTransformations(final Node node, final Transform... newTransforms) {
        // gets the node's current transformation
        final ObservableList<Transform> currentTransforms = node.getTransforms();
        Transform totalTransform = new Translate();

        // adds all new transforms to the total transformations
        for (Transform transform : newTransforms) {
            totalTransform = totalTransform.createConcatenation(transform);
        }

        // adds all previous transforms to the total transformations
        for (Transform transform : currentTransforms) {
            totalTransform = totalTransform.createConcatenation(transform);
        }

        // removes previous transformations and adds the concatenated sum of all transformations
        // (avoids having several hundred transformations applied to the node by optimising them into a single Transform)
        currentTransforms.clear();
        currentTransforms.add(totalTransform);
    }

}
