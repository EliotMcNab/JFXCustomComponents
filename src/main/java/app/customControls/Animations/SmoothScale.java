package app.customControls.Animations;

import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

public class SmoothScale implements AnimationWorkaround {

    // =======================================
    //                FIELDS
    // =======================================

    private final Timeline animation;

    // =======================================
    //              CONSTRUCTOR
    // =======================================

    public SmoothScale(
        final Scale scale,
        final Point2D toScale,
        final Duration duration
    ) {
        this(scale, toScale, new Point2D(scale.getPivotX(), scale.getPivotY()), duration);
    }

    public SmoothScale(
            final Scale scale,
            final Point2D toScale,
            final Point2D toPivot,
            final Duration duration
    ) {
        this(
                scale,
                new Point2D(scale.getX(), scale.getY()),
                toScale,
                new Point2D(scale.getPivotX(), scale.getPivotY()),
                toPivot,
                duration
        );
    }

    public SmoothScale(
            final Scale scale,
            final Point2D fromScale,
            final Point2D toScale,
            final Point2D fromPivot,
            final Point2D toPivot,
            final Duration duration
    ) {
        animation = generateAnimation(scale, fromScale, toScale, fromPivot, toPivot, duration);
        /*animation.setOnFinished(actionEvent -> {
            scale.setPivotX(toPivot.getX());
            scale.setPivotY(toPivot.getY());
            scale.setX(toScale.getX());
            scale.setY(toScale.getY());
        });*/
    }

    // =======================================
    //               ANIMATION
    // =======================================

    private Timeline generateAnimation(
            Scale scale,
            final Point2D fromScale,
            final Point2D toScale,
            final Point2D fromPivot,
            final Point2D toPivot,
            final Duration duration
    ) {
        return new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                scale.pivotXProperty(),
                                fromPivot.getX()
                        ),
                        new KeyValue(
                                scale.pivotYProperty(),
                                fromPivot.getY()
                        ),
                        new KeyValue(
                                scale.xProperty(),
                                fromScale.getX()
                        ),
                        new KeyValue(
                                scale.yProperty(),
                                fromScale.getY()
                        )
                ),
                new KeyFrame(
                        duration,
                        new KeyValue(
                                scale.pivotXProperty(),
                                toPivot.getX()
                        ),
                        new KeyValue(
                                scale.pivotYProperty(),
                                toPivot.getY()
                        ),
                        new KeyValue(
                                scale.xProperty(),
                                toScale.getX()
                        ),
                        new KeyValue(
                                scale.yProperty(),
                                toScale.getY()
                        )
                )
        );
    }

    @Override
    public Animation getAnimation() {
        return animation;
    }

}
