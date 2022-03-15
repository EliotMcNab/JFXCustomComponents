package app.customControls.controls.colorpickerOverlay;

import app.customControls.utilities.ScreenUtil;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A transparent stage which can be used for UI overlays
 * @see ColorPickerOverlay ColorPickerOverlay
 */
public class TransparentWindow extends Stage {

    // ===============================
    //             FIELDS
    // ===============================

    final Scene windowScene;

    TransparentWindow(Parent root) {
        this(root,true);
    }

    // ===============================
    //          CONSTRUCTORS
    // ===============================

    TransparentWindow(Parent root, boolean fitToScreen) {
        super();

        windowScene = new Scene(root);

        format(fitToScreen);
        populate();
        style(root);

        requestFocus();
    }

    private void format(boolean fitToScreen) {
        initStyle(StageStyle.TRANSPARENT);

        if (fitToScreen) {
            Rectangle2D screenBounds = ScreenUtil.getScreenSize();
            setWidth(screenBounds.getWidth());
            setHeight(screenBounds.getHeight() - ScreenUtil.getTaskbarSize());
        }
    }

    // ===============================
    //         INITIALISATION
    // ===============================

    private void populate() {
        setScene(windowScene);
    }

    private void style(Parent root) {

        windowScene.setFill(Color.TRANSPARENT);
        root.setStyle(
                "-fx-background-color: transparent;"
        );

    }

}
