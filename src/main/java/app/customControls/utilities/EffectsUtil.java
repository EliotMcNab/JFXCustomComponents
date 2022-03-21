package app.customControls.utilities;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
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
        translateTransition.setToX(target.getX());
        translateTransition.setToY(target.getY());

        return translateTransition;
    }

}
