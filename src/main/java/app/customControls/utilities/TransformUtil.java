package app.customControls.utilities;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class TransformUtil {
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

    public static Scale scaleAndTranslate(final Scale scale, final Translate translate) {
        final double newPivotX = scale.getPivotX() + (scale.getX() == 1 ? 0 : translate.getX() / (1 - scale.getX()));
        final double newPivotY = scale.getPivotY() + (scale.getY() == 1 ? 0 : translate.getY() / (1 - scale.getY()));
        final double newPivotZ = scale.getPivotZ() + (scale.getZ() == 1 ? 0 : translate.getZ() / (1 - scale.getZ()));
        return new Scale(scale.getX(), scale.getY(), scale.getZ(), newPivotX, newPivotY, newPivotZ);
    }
}
