package app.customControls.controls.colorpickerOverlay;

import app.customControls.controls.colorPicker.MaterialColorPicker;
import app.customControls.controls.corner.BorderFrame;
import app.customControls.controls.shapes.BorderLine;
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

import static app.customControls.utilities.KeyboardUtil.Letter.ESC;
import static app.customControls.utilities.KeyboardUtil.areKeysDown;

/**
 * Handles displaying the color picking UI overlay for a {@link MaterialColorPicker} & hiding the color picker.
 * Works by taking a screenshot whenever the {@link ColorPickerOverlay} comes into focus & reading its pixels
 * as the user hovers over the screen<br>
 * <br>
 * <u><i>CSS Pseudo-class</i></u> : color-picker-overlay<br>
 * <br>
 * <u><i>Substructure</i></u> : <br>
 * <ul>
 *     <li>border-frame: {@link BorderFrame}</li>
 *     <ul>
 *         <li>border-line: {@link BorderLine BorderLine}</li>
 *     </ul>
 *     <li>hover-Color: Rectangle</li>
 * </ul>
 * <u><i>Features</i></u> :<br>
 * <ul>
 *     <li>handles toggling between the color picker and only displaying the overlay</li>
 *     <li>allows picking any color on the screen except the taskbar</li>
 *     <li>displays clear boundary outline as to in which area a color can be picked</li>
 *     <li>uses a custom eye-picker icon for the cursor</li>
 *     <li>adjusts cursor position for easier targeting of individual pixels</li>
 *     <li>displays the current hove color next to the cursor</li>
 * </ul>
 * @implNote Does not allow selecting colors along the trackbar & overlay can obfuscate colors below it
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

    /**
     * {@link ColorPickerOverlay} constructor
     * @param colorPicker ({@link MaterialColorPicker}): color picker associated to the overlay
     */
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

    /**
     * Initialises style classes & adds the necessary stylesheets to the {@link ColorPickerOverlay}
     */
    private void style() {
        // style classes
        root.getStyleClass().setAll("color-picker-overlay");
        hoverColor.getStyleClass().setAll("hover-color");

        // css stylesheets
        final String css = getClass().getResource("/app/customControls/style/color-picker-overlay.css").toExternalForm();
        root.getStylesheets().add(css);
    }

    /**
     * Adds all the components as children of the {@link ColorPickerOverlay}'s root
     */
    private void populate() {

        // text to display at the top of the overlay
        borderFrame.setText("press ESC to exit");
        // makes the overlay slightly transparent
        borderFrame.setOpacity(0.5);

        // adds the components to the root
        root.setCenter(borderFrame);
        root.getChildren().add(hoverColor);

    }

    /**
     * Registers the listeners for each of the {@link ColorPickerOverlay}'s components
     */
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

    /**
     * Handles updating the screenshot & hover color position when the {@link ColorPickerOverlay} is focused
     */
    private void handleFocusGain() {

        // moves the hover color display to the mouse's curren position
        NodeUtil.positionAt(hoverColor, ScreenUtil.getMousePosition().add(MOUSE_OFFSET));

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

    /**
     * Repositions the hover color to be beside the mouse
     * @param mouseEvent ({@link MouseEvent}): the event triggered by the mouse being dragged
     */
    private void repositionColor(final MouseEvent mouseEvent) {

        // waits for delay between movements
        if (!movementDelay.hasElapsed()) return;

        // gets the mouse's position along the screen & translates the hover color to the bottom right
        final Point2D target = ScreenUtil.getMousePosition().add(MOUSE_OFFSET);
        NodeUtil.positionAt(hoverColor, target);

        // updates the current color upon mouse movement
        updateHoverColor();
    }

    /**
     * Updates the hover color to match the currently hovered color
     */
    private void updateHoverColor() {
        // checks that the screenshot has been taken or otherwise exits the method
        if (screenShot == null) return;
        // updates the hover color
        hoverColor.setFill(getCurrentColor());
    }

    /**
     * Saves the current hover color to the {@link MaterialColorPicker} & toggles back to the color picker display
     */
    private void selectCurrentColor() {
        // checks that the screenshot has been taken or otherwise exits the method
        if (screenShot == null) return;
        // saves the color to the color picker
        colorPicker.setColor(getCurrentColor());
        // returns to the color picker overlay
        toggleOverlay();
    }

    /**
     * Gets the currently hovered color
     * @return (Color): the currently hovered color
     */
    private Color getCurrentColor() {
        // gets the current mouse coordinates
        final Point2D mouseCoordinates = ScreenUtil.getMousePosition();
        // adjusts the mouse coordinates along the x-axis & y-axis to make it easier to select pixels
        final double adjustedX = Math.max(mouseCoordinates.getX() - 15, 0);
        final double adjustedY = Math.max(mouseCoordinates.getY() - 15, 0);

        // determines the color at the mouse's ADJUSTED position
        return screenShot.getColor((int) adjustedX, (int) adjustedY);
    }

    // ===============================
    //          KEY PRESSES
    // ===============================

    /**
     * Handles user key presses
     * @param keyEvent ({@link KeyEvent}): the event responsible for calling the method
     */
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

    /**
     * Shows the {@link ColorPickerOverlay} & hides its associated {@link MaterialColorPicker}
     */
    private void showOverlay() {
        // transitions visible stages
        colorPickerStage.hide();
        overlayStage.show();
        borderFrame.requestFocus();
        isVisible = true;

        // changes the default cursor to be an eye picker
        updateCursor();
    }

    /**
     * Hides the {@link ColorPickerOverlay} & shows its associated {@link MaterialColorPicker}
     */
    private void hideOverlay() {
        // transitions visible stages
        hoverColor.requestFocus();
        overlayStage.hide();
        colorPickerStage.show();
        isVisible = false;
    }

    /**
     * Updates the cursor to be an eye picker instead of default
     */
    private void updateCursor() {

        // gets the path to the cursor graphic & saves it as an image
        final String path = "/app/customControls/pictures/eyedropper_white_no_crosshair_32.png";
        final String imageString = getClass().getResource(path).toExternalForm();
        final Image cursor = new Image(imageString);

        // updates the cursor
        getScene().setCursor(new ImageCursor(cursor, 32, 32));
    }
}
