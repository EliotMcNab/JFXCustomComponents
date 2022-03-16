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
 * @see ColorPickerOverlay
 */
public class TransparentWindow extends Stage {

    // ===============================
    //             FIELDS
    // ===============================

    final Scene windowScene;

    // ===============================
    //          CONSTRUCTORS
    // ===============================

    /**
     * {@link TransparentWindow} constructor
     * @param root ({@link Parent}): root node to be displayed inside the window
     */
    TransparentWindow(Parent root) {
        this(root,true);
    }

    /**
     * {@link TransparentWindow} constructor
     * @param root ({@link Parent}): root node to be displayed inside the window
     * @param fitToScreen (boolean): whether the window should take up all available screen space
     */
    TransparentWindow(Parent root, boolean fitToScreen) {
        super();

        // saves the root scene
        windowScene = new Scene(root);

        format(fitToScreen);
        populate();
        style(root);

        requestFocus();
    }

    /**
     * Handles initialising the style & size of the screen
     * @param fitToScreen (boolean): whether the window takes up all the screen space
     */
    private void format(boolean fitToScreen) {
        // makes the window transparent
        initStyle(StageStyle.TRANSPARENT);

        // grows the window to fit the screen if specified
        if (fitToScreen) {
            Rectangle2D screenBounds = ScreenUtil.getScreenSize();
            setWidth(screenBounds.getWidth());
            setHeight(screenBounds.getHeight() - ScreenUtil.getTaskbarSize());
        }
    }

    // ===============================
    //         INITIALISATION
    // ===============================

    /**
     * Sets the window's scene
     */
    private void populate() {
        setScene(windowScene);
    }

    /**
     * Makes the window background transparent
     * @param root ({@link Parent}): root node to be displayed inside the window
     */
    private void style(Parent root) {

        // makes the window & the root transparent
        windowScene.setFill(Color.TRANSPARENT);
        root.setStyle(
                "-fx-background-color: transparent;"
        );

    }

}
