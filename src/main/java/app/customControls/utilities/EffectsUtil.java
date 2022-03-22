package app.customControls.utilities;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.util.Duration;

public class EffectsUtil {

    public static DropShadow generateDropShadow(
            final BlurType blurType,
            final Color color,
            final int radius,
            final double spread,
            final double offsetX,
            final double offsetY) {

        DropShadow dropShadow = new DropShadow();
        dropShadow.setBlurType(blurType);
        dropShadow.setColor(color);
        dropShadow.setRadius(radius);
        dropShadow.setSpread(spread);
        dropShadow.setOffsetX(offsetX);
        dropShadow.setOffsetY(offsetY);

        return dropShadow;
    }

    public static ScaleTransition generateScaleTransition(
            final long duration,
            final double xScale,
            final double yScale,
            final Node node
    ) {
        final ScaleTransition scaleTransition = new ScaleTransition();

        scaleTransition.setDuration(Duration.millis(duration));
        scaleTransition.setToX(xScale);
        scaleTransition.setToY(yScale);
        scaleTransition.setNode(node);

        return scaleTransition;
    }

    public static TranslateTransition generateTranslateTransition(
            final long duration,
            final Point2D target,
            final Node node
    ) {
        final TranslateTransition translateTransition = new TranslateTransition();

        translateTransition.setDuration(Duration.millis(duration));
        translateTransition.setNode(node);
        translateTransition.setFromX(node.getTranslateX());
        translateTransition.setFromY(node.getTranslateY());
        translateTransition.setToX(target.getX());
        translateTransition.setToY(target.getY());

        return translateTransition;
    }

    public static Scale transformToScale(final Transform transform) {
        final double x = transform.getMxx();
        final double y = transform.getMyy();
        final double tX = transform.getTx();
        final double tY = transform.getTy();
        final double pivotX = tX / (1 - x);
        final double pivotY = tY / (1 - y);

        final Scale scale = new Scale();
        scale.setX(x);
        scale.setY(y);
        scale.setPivotX(pivotX);
        scale.setPivotY(pivotY);

        return scale;
    }

    public static Scale concatenateScale(final Point2D defaultPivot, final Scale... scales) {
        Scale totalScale = new Scale();
        for (Scale scale : scales) {
            totalScale = concatenateScaleImpl(defaultPivot, totalScale, scale);
        }
        return totalScale;
    }

    private static Scale concatenateScaleImpl(final Point2D defaultPivot, final Scale scale1, final Scale scale2) {
        final double x1 = scale1.getX();
        final double x2 = scale2.getX();
        final double y1 = scale1.getY();
        final double y2 = scale2.getY();
        final double pivotX1 = scale1.getPivotX();
        final double pivotX2 = scale2.getPivotX();
        final double pivotY1 = scale1.getPivotY();
        final double pivotY2 = scale2.getPivotY();
        final double X = x1 * x2;
        final double Y = y1 * y2;
        final double pivotX = (x1 * (1 - x2) * pivotX2 + (1 - x1) * pivotX1) / (1 - X);
        final double pivotY = (y1 * (1 - y2) * pivotY2 + (1 - y1) * pivotY1) / (1 - Y);

        final boolean invalidPivotX = Double.isNaN(pivotX) || Double.isInfinite(pivotX);
        final boolean invalidPivotY = Double.isNaN(pivotY) || Double.isInfinite(pivotY);;

        final Scale scale = new Scale();
        scale.setX(X);
        scale.setY(Y);
        scale.setPivotX(invalidPivotX ? defaultPivot.getX() : pivotX);
        scale.setPivotY(invalidPivotY ? defaultPivot.getY() : pivotY);

        return scale;
    }


}
