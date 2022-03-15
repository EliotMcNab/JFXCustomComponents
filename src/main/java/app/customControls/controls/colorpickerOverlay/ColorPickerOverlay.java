package app.customControls.controls.colorpickerOverlay;

import app.customControls.controls.colorPicker.MaterialColorPicker;
import app.customControls.controls.corner.BorderFrame;
import app.customControls.handlers.delay.DelayHandler;
import app.customControls.utilities.NodeUtil;
import app.customControls.utilities.ScreenUtil;
import javafx.animation.PauseTransition;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import static app.customControls.utilities.KeyboardUtil.Letters.ESC;
import static app.customControls.utilities.KeyboardUtil.areKeysDown;

/**
 * Handles displaying the color picking UI overlay for a {@link MaterialColorPicker}
 */
public class ColorPickerOverlay extends TransparentWindow {

    // ===============================
    //            FIELDS
    // ===============================

    /*          CONSTANTS           */

    private static final int RECTANGLE_SIZE = 30;
    private static final int DEFAULT_CORNER_SIZE = 50;
    private static final int DEFAULT_CORNER_WIDTH = 10;
    private static final int MOVEMENT_DELAY = 10;
    private static final Point2D MOUSE_OFFSET = new Point2D(10, 10);

    /*          COMPONENTS          */

    private final MaterialColorPicker colorPicker;
    private final BorderFrame borderFrame;
    private final Rectangle hoverColor;
    private final BorderPane root;

    /*          VISIBILITY          */

    private boolean isVisible = false;
    private final Stage colorPickerStage;
    private final Stage overlayStage;
    private PixelReader screenShot;

    /*           LISTENERS          */

    private final InvalidationListener focusListener;
    private final EventHandler<MouseEvent> movementListener;
    private final EventHandler<MouseEvent> colorSelectionListener;
    private final EventHandler<KeyEvent> keyListener;

    /*            TIMING            */

    private final DelayHandler movementDelay;

    // ===============================
    //          CONSTRUCTORS
    // ===============================

    public ColorPickerOverlay(MaterialColorPicker colorPicker) {
        super(new BorderPane());

        this.colorPicker = colorPicker;
        // components
        this.borderFrame = new BorderFrame(DEFAULT_CORNER_SIZE, DEFAULT_CORNER_WIDTH, Color.WHITE);
        this.hoverColor = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE);
        this.root = (BorderPane) getScene().getRoot();
        // stages
        this.colorPickerStage = (Stage) colorPicker.getScene().getWindow();
        this.overlayStage = (Stage) getScene().getWindow();
        // timing
        this.movementDelay = new DelayHandler(MOVEMENT_DELAY);
        // listeners
        this.focusListener = observable -> handleFocusGain();
        this.movementListener = this::repositionColor;
        this.colorSelectionListener = mouseEvent -> selectCurrentColor();
        this.keyListener = this::handleKeyPresses;

        style();
        populate();
        registerListeners();
    }

    // ===============================
    //         INITIALISATION
    // ===============================

    private void style() {
        // style classes
        root.getStyleClass().setAll("color-picker-overlay");
        hoverColor.getStyleClass().setAll("hover-color");

        // css stylesheets
        final String css = getClass().getResource("/app/customControls/style/color-picker-overlay.css").toExternalForm();
        root.getStylesheets().add(css);
    }

    private void populate() {

        borderFrame.setText("press ESC to exit");
        borderFrame.setOpacity(0.5);

        root.setCenter(borderFrame);

        root.getChildren().add(hoverColor);

    }

    private void registerListeners() {
        // focus gain
        borderFrame.focusedProperty().addListener(focusListener);

        // mouse movement
        borderFrame.setOnMouseMoved(movementListener);

        // color selection
        borderFrame.setOnMousePressed(colorSelectionListener);

        // key presses
        getScene().setOnKeyPressed(keyListener);
    }

    // ===============================
    //             FOCUS
    // ===============================

    private void handleFocusGain() {

        // moves the hover color display to the mouse's curren position
        moveColorToMouse(ScreenUtil.getMousePosition().add(MOUSE_OFFSET));

        // delay taking into consideration the closing of the color picker
        PauseTransition pauseTransition = new PauseTransition(Duration.millis(250));
        // takes the screenshot only once the color picker has disappeared
        pauseTransition.setOnFinished(actionEvent -> {
            // takes a screenshot
            screenShot = ScreenUtil.getScreenShot().getPixelReader();

            // updates the hover color to mach the screenshot
            updateHoverColor();
        });
        // starts the delay
        pauseTransition.play();
    }

    // ===============================
    //          HOVER COLOR
    // ===============================

    private void repositionColor(final MouseEvent mouseEvent) {

        // waits for delay between movements
        if (!movementDelay.hasElapsed()) return;

        final Point2D target = ScreenUtil.getMousePosition().add(MOUSE_OFFSET);
        moveColorToMouse(target);

        // updates the current color upon mouse movement
        updateHoverColor();
    }

    private void moveColorToMouse(final Point2D mouseCoordinates) {;
        NodeUtil.positionAt(hoverColor, mouseCoordinates);
    }

    private void updateHoverColor() {
        if (screenShot == null) return;
        hoverColor.setFill(getCurrentColor());
    }

    private void selectCurrentColor() {
        colorPicker.setColor(getCurrentColor());
        toggleOverlay();
    }

    private Color getCurrentColor() {
        final Point2D mouseCoordinates = ScreenUtil.getMousePosition();
        final double adjustedX = Math.max(mouseCoordinates.getX() - 15, 0);
        final double adjustedY = Math.max(mouseCoordinates.getY() - 15, 0);

        return screenShot.getColor((int) adjustedX, (int) adjustedY);
    }

    // ===============================
    //          KEY PRESSES
    // ===============================

    private void handleKeyPresses(final KeyEvent keyEvent) {
        // user wants to exit overlay
        if (areKeysDown(keyEvent, ESC)) toggleOverlay();
    }

    // ===============================
    //            DISPLAY
    // ===============================

    /**
     *  Hides overlay if it was visible, shows it if it was hidden
     */
    public void toggleOverlay() {
        if (isVisible) hideOverlay();
        else showOverlay();
    }

    private void showOverlay() {
        colorPickerStage.hide();
        overlayStage.show();
        borderFrame.requestFocus();
        isVisible = true;

        updateCursor();
    }

    private void hideOverlay() {
        hoverColor.requestFocus();
        overlayStage.hide();
        colorPickerStage.show();
        isVisible = false;
    }

    private void updateCursor() {

        final String path = "/app/customControls/pictures/eyedropper_white_no_crosshair_32.png";
        final String imageString = getClass().getResource(path).toExternalForm();
        final Image cursor = new Image(imageString);

        getScene().setCursor(new ImageCursor(cursor, 32, 32));
    }
}
