package app.customControls.controls.movementPane;

import app.customControls.Animations.BezierCurve;
import app.customControls.Animations.PauseAnimation;
import app.customControls.Animations.SmoothScale;
import app.customControls.Images;
import app.customControls.controls.resizePanel.ResizePanel;
import app.customControls.controls.time.TimedAnimation;
import app.customControls.utilities.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.*;
import javafx.util.Duration;

import java.util.HashSet;

import static app.customControls.utilities.KeyboardUtil.*;
import static app.customControls.utilities.KeyboardUtil.Letter.*;
import static app.customControls.utilities.KeyboardUtil.Modifier.CTRL;
import static app.customControls.utilities.KeyboardUtil.Modifier.SHIFT;

public class MovementPaneSkin extends SkinBase<MovementPane> implements Skin<MovementPane> {

    // =======================================
    //                FIELDS
    // =======================================

    // constants

    private static final long DELAY = 10;
    private static final int MAX_SIZE = 10000;
    private static final double GROWTH = 1.01;
    private static final double MOVEMENT_RATIO = 0.01; // ratio of max scroll bar max value used when adding to the scroll bar
    private static final long ZOOM_DURATION = 100;
    private static final long RESET_DURATION = 300;
    private static final double ZOOM_AMOUNT = 1.07;
    private static final double MAX_ZOOM = 5.0;
    private static final double MIN_ZOOM = 0.3;
    private static final String CSS_LOCATION = "/app/customControls/style/movement-pane.css";

    // effects

    private static final Color SHADOW_COLOR = Color.rgb(0, 0, 0, 0.3);
    private static final DropShadow NODE_PICKUP_SHADOW = EffectsUtil.generateDropShadow(
            BlurType.GAUSSIAN,
            SHADOW_COLOR,
            20,
            0.3,
            10,
            10
    );
    private static final DropShadow NODE_IDLE_SHADOW = EffectsUtil.generateDropShadow(
            BlurType.GAUSSIAN,
            SHADOW_COLOR,
            20,
            0.3,
            5,
            5
    );

    // movement pane

    private final MovementPane movementPane;

    // movement

    private final Translate drag;
    private final Scale zoom;
    private final Scale pickupScale;
    private Point2D lastMousePosition;
    private Point2D lastProgress;
    private double lastHValue;
    private double lastVValue;
    private final HashSet<KeyboardUtil.Key> keyPresses;
    private final TimedAnimation leftCollision;
    private final TimedAnimation rightCollision;
    private final TimedAnimation topCollision;
    private final TimedAnimation bottomCollision;
    private boolean pickup;
    private boolean dragging;
    private boolean panning;

    // animation

    private BezierCurve scaleInterpolation;
    private BezierCurve resetPivotXInterpolation;
    private BezierCurve resetPivotYInterpolation;
    private BezierCurve centerXInterpolation;
    private BezierCurve centerYInterpolation;
    private BezierCurve centerPivotXInterpolation;
    private BezierCurve centerPivotYInterpolation;
    private BezierCurve resetXInterpolation;
    private BezierCurve resetYInterpolation;
    private BezierCurve widthInterpolation;
    private BezierCurve heightInterpolation;
    private BezierCurve resizeTranslateXInterpolation;
    private BezierCurve resizeTranslateYInterpolation;
    private final PauseAnimation resetAnimation;
    private final PauseAnimation centerAnimation;
    private Rectangle2D originalNodeSize;

    // components

    private final ScrollBar hScroll;
    private final ScrollBar vScroll;
    private final Pane viewPort;
    private final Pane container;
    private final ResizePanel resizePanel;

    // properties

    private final BooleanProperty horizontalCollision;
    private final BooleanProperty verticalCollision;

    // listeners

    private final InvalidationListener resizeListener;
    private final InvalidationListener hValueListener;
    private final InvalidationListener vValueListener;
    private final InvalidationListener nodeChangeListener;
    private final InvalidationListener collisionListener;
    private final ChangeListener<Boolean> hCollisionListener;
    private final ChangeListener<Boolean> vCollisionListener;
    private final EventHandler<MouseEvent> mousePressListener;
    private final EventHandler<MouseEvent> mouseReleaseListener;
    private final EventHandler<MouseEvent> dragListener;
    private final EventHandler<KeyEvent> keyPressListener;
    private final EventHandler<KeyEvent> keyReleaseListener;
    private final EventHandler<ScrollEvent> scrollListener;

    // =======================================
    //              CONSTRUCTOR
    // =======================================

    protected MovementPaneSkin(MovementPane movementPane) {
        super(movementPane);

        // initialising transformations

        this.drag = new Translate();
        this.zoom = new Scale();
        this.pickupScale = new Scale();

        // initialises components

        this.movementPane = movementPane;

        this.hScroll = new ScrollBar();
        this.hScroll.setOrientation(Orientation.HORIZONTAL);
        this.hScroll.setMin(movementPane.getHorizontalMin());
        this.hScroll.setMax(movementPane.getHorizontalMax());
        this.hScroll.setVisibleAmount(movementPane.getHorizontalVisibleAmount());
        this.hScroll.setValue(movementPane.getHValue());

        this.vScroll = new ScrollBar();
        this.vScroll.setOrientation(Orientation.VERTICAL);
        this.vScroll.setMin(movementPane.getVerticalMin());
        this.vScroll.setMax(movementPane.getVerticalMax());
        this.vScroll.setVisibleAmount(movementPane.getVerticalVisibleAmount());
        this.vScroll.setValue(movementPane.getVValue());

        this.viewPort = new Pane();
        this.container = new Pane();
        this.resizePanel = new ResizePanel();
        this.resizePanel.setEffect(NODE_IDLE_SHADOW);
        this.resizePanel.getTransforms().addAll(drag, pickupScale);

        // initialising animations

        final Rectangle2D nodeSize = getNodeSize();
        this.originalNodeSize = new Rectangle2D(
                0,
                0,
                nodeSize.getWidth(),
                nodeSize.getHeight()
        );
        updateInterpolation(INTERPOLATION_MODE.ALL);

        this.resetAnimation = new PauseAnimation(Duration.millis(RESET_DURATION)) {
            @Override
            public void tick(long now) {
                handleResetAnimation(now);
            }
        };
        this.resetAnimation.setOnEnd(this::handleResetEnd);

        this.centerAnimation = new PauseAnimation(Duration.millis(RESET_DURATION)) {
            @Override
            public void tick(long now) {
                handleCenterAnimation(now);
            }
        };
        this.centerAnimation.setOnEnd(this::handleCenterEnd);

        // initialise properties

        this.horizontalCollision = new SimpleBooleanProperty(this, "horizontalCollision", false);
        this.verticalCollision = new SimpleBooleanProperty(this, "verticalCollision", false);

        // initialise listeners

        this.resizeListener = observable -> resize();
        this.hValueListener = observable -> handleScrollBarUpdate(hScroll);
        this.vValueListener = observable -> handleScrollBarUpdate(vScroll);
        this.nodeChangeListener = observable -> bindNode(movementPane.getMovementNode());
        this.collisionListener = observable -> detectCollision();
        this.hCollisionListener = (observableValue, aBoolean, t1) -> horizontalCollision();
        this.vCollisionListener = (observableValue, aBoolean, t1) -> verticalCollision();
        this.mousePressListener = this::handleMousePress;
        this.mouseReleaseListener = this::handleMouseRelease;
        this.dragListener = this::handleDrag;
        this.keyPressListener = this::handleKeyPress;
        this.keyReleaseListener = this::handleKeyRelease;
        this.scrollListener = this::handleScroll;

        // initialising variables

        this.keyPresses = new HashSet<>();
        this.lastMousePosition = new Point2D(0, 0);
        this.lastProgress = new Point2D(0, 0);
        this.lastHValue = hScroll.getValue();
        this.lastVValue = vScroll.getValue();

        this.leftCollision = new TimedAnimation(DELAY) {
            @Override
            public void run() {decreaseAnimation(hScroll);}
        };
        this.rightCollision = new TimedAnimation(DELAY) {
            @Override
            public void run() {increaseAnimation(hScroll);}
        };
        this.topCollision = new TimedAnimation(DELAY) {
            @Override
            public void run() {decreaseAnimation(vScroll);}
        };
        this.bottomCollision = new TimedAnimation(DELAY) {
            @Override
            public void run() {increaseAnimation(vScroll);}
        };

        style();
        populate();
        registerListeners();
    }

    // =======================================
    //             INITIALISATION
    // =======================================

    private void style() {
        container.getStyleClass().add("background");
        movementPane.getStyleClass().add("movement-pane");
        movementPane.setCursor(Cursor.DEFAULT);
        resizePanel.setCursor(Cursor.DEFAULT);

        try {
            String css = MovementPane.class.getResource(CSS_LOCATION).toExternalForm();
            movementPane.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.out.printf("Impossible to add stylesheet at location %s\n", CSS_LOCATION);
        }

    }

    private void populate() {
        viewPort.getChildren().add(resizePanel);
        container.getChildren().addAll(viewPort, hScroll, vScroll);
        getChildren().add(container);
    }

    // =======================================
    //               LISTENERS
    // =======================================

    private void registerListeners() {
        // resizing
        movementPane.widthProperty().addListener(resizeListener);
        movementPane.heightProperty().addListener(resizeListener);
        movementPane.associatedNodeProperty().addListener(nodeChangeListener);

        // panning
        hScroll.valueProperty().addListener(hValueListener);
        vScroll.valueProperty().addListener(vValueListener);

        // dragging
        viewPort.setOnMousePressed(mousePressListener);
        viewPort.setOnMouseReleased(mouseReleaseListener);
        viewPort.setOnMouseDragged(dragListener);
        movementPane.setOnKeyPressed(keyPressListener);
        movementPane.setOnKeyReleased(keyReleaseListener);

        // resizePanel.setMouseTransparent(true);

        // moving
        resizePanel.setOnMousePressed(mousePressListener);
        resizePanel.setOnMouseDragged(dragListener);
        resizePanel.setOnMouseReleased(mouseReleaseListener);

        // zooming
        viewPort.setOnScroll(scrollListener);

        // collisions
        horizontalCollision.addListener(hCollisionListener);
        verticalCollision.addListener(vCollisionListener);
        resizePanel.boundsInParentProperty().addListener(collisionListener);
    }

    // =======================================
    //               USER INPUT
    // =======================================

    private void handleMousePress(final MouseEvent mouseEvent) {

        final Node source = (Node) mouseEvent.getSource();

        if (source.equals(viewPort)) handleViewPortMousePress(mouseEvent);
        else if (source.equals(resizePanel)) handleResizePanelMousePress(mouseEvent);
    }

    private void handleViewPortMousePress(final MouseEvent mouseEvent) {
        // determines and saves the current mouse position
        final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        lastMousePosition = viewPort.localToScreen(mousePosition);

        // teleports the node if user it is not being dragged
        if (!areKeysDown(keyPresses, SPACE)) {
            resizePanel.setSelected(false);
            moveTo(centerOnResizePanel(mousePosition));
        }
        else pickupNode();
    }

    private void handleResizePanelMousePress(final MouseEvent mouseEvent) {
        // determines and saves the current mouse position within the resize node
        lastMousePosition = resizePanel.localToScreen(new Point2D(mouseEvent.getX(), mouseEvent.getY()));

        // updates the cursor's appearance
        if (resizePanel.isSelected() && resizePanel.getCursor().equals(Cursor.DEFAULT))
            resizePanel.setCursor(Images.Cursor.MOVE_CURSOR);
    }

    private void handleMouseRelease(final MouseEvent mouseEvent) {
        final Node source = (Node) mouseEvent.getSource();

        // updates the node's progress
        updateProgress();

        dropNode();
        dragging = false;
        panning = false;

        if (source.equals(resizePanel)) handleResizePanelMouseRelease();
    }

    private void handleResizePanelMouseRelease() {
        stopHorizontalCollisions();
        stopVerticalCollisions();
    }

    private void handleDrag(final MouseEvent mouseEvent) {

        final Node source = (Node) mouseEvent.getSource();

        if (source.equals(viewPort)) handleViewportDrag(mouseEvent);

        if (source.equals(resizePanel)) handleResizePanelDrag(mouseEvent);
    }

    private void handleViewportDrag(final MouseEvent mouseEvent) {
        // checks that the SPACE key is being pressed to allow for drag
        if (!areKeysDown(keyPresses, SPACE)) return;

        // starts dragging
        panning = true;

        // determines the current position of the mouse
        final Point2D mousePosition = viewPort.localToScreen(new Point2D(mouseEvent.getX(), mouseEvent.getY()));

        // determines by how much the mouse has moved since last time
        final Point2D dragAmount = mousePosition.subtract(lastMousePosition);

        // applies the drag to the node
        moveTo(getResizePanelPosition().add(dragAmount));

        // saves the current mouse position
        lastMousePosition = mousePosition;
    }

    private void handleResizePanelDrag(final MouseEvent mouseEvent) {
        // determines whether the resize panel is currently resizing its associated node
        final boolean isResizing = !resizePanel.getCursor().equals(Images.Cursor.MOVE_CURSOR);

        // if drag event was triggered because resize panel is resizing associated node, exits method
        if (isResizing) return;

        // starts dragging
        dragging = true;

        // tries to pick up the associated node
        // (does not work if node is already picker up)
        pickupNode();

        // determines the current position of the mouse
        final Point2D mousePosition = resizePanel.localToScreen(new Point2D(mouseEvent.getX(), mouseEvent.getY()));

        // determines difference in mouse position since last drag
        final Point2D deltaPosition = mousePosition.subtract(lastMousePosition);

        // applies the drag to the node
        moveTo(getResizePanelPosition().add(deltaPosition));

        // updates mouse position
        lastMousePosition = mousePosition;
    }

    private void handleKeyPress(final KeyEvent keyEvent) {
        // if the key is not already pressed, adds it to the currently pressed keys
        saveKeyPress(keyEvent, keyPresses);

        if (areKeysDown(keyPresses, ESC))
            resizePanel.setSelected(false);

        // dragging
        if (areKeysDown(keyPresses, SPACE) && movementPane.getCursor().equals(Cursor.DEFAULT))
            movementPane.setCursor(Cursor.CLOSED_HAND);

        // centering
        if (areKeysDown(keyPresses, CTRL, SHIFT, O)) center(true);

        // resetting
        if (areKeysDown(keyPresses, CTRL, SHIFT, R)) reset();
    }

    private void handleKeyRelease(final KeyEvent keyEvent) {
        // tries to remove the key for the set of pressed keys once it is released
        removeKeyPress(keyEvent, keyPresses);

        // if user stopped dragging node, resets the cursor
        if (keyEvent.getCode().equals(KeyCode.SPACE)) movementPane.setCursor(Cursor.DEFAULT);
    }

    private void handleScroll(final ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) zoom(scrollEvent);
        else if (scrollEvent.isShiftDown()) horizontalScroll(scrollEvent);
        else verticalScroll(scrollEvent);
    }

    // =======================================
    //              SCROLL BARS
    // =======================================

    private void handleScrollBarUpdate(final ScrollBar scrollBar) {

        // if node is being dragged, does not update its position
        if (dragging || panning) return;

        // determines the current scroll bar value
        final double currentValue = scrollBar.getValue();

        // calculates the node's displacement along the x-axis based on the slider's current value
        final double deltaX;
        if (scrollBar.equals(hScroll)) {
            deltaX = currentValue - lastHValue;
            lastHValue = currentValue;
        } else {
            deltaX = 0;
        }

        // calculates the node's displacement along the y-axis based on the slider's current value
        final double deltaY;
        if (scrollBar.equals(vScroll)) {
            deltaY = currentValue - lastVValue;
            lastVValue = currentValue;
        } else {
            deltaY = 0;
        }

        // moves the node to the target position
        final Point2D target = getResizePanelPosition().subtract(deltaX, deltaY);
        moveTo(target);
    }

    // =======================================
    //               MOVEMENT
    // =======================================

    private void pickupNode() {
        // if node has already been picked exits method
        if (pickup) return;

        // deselects the resize panel
        resizePanel.setSelected(false);

        // checks for collisions
        detectCollision();

        // generates the node pickup animation
        final Point2D fromScale = new Point2D(1, 1);
        final Point2D toScale = new Point2D(ZOOM_AMOUNT, ZOOM_AMOUNT);
        final Point2D pivot = getNodeCenter();
        final Duration duration = Duration.millis(ZOOM_DURATION);

        final Animation pickupAnimation = new SmoothScale(pickupScale, fromScale, toScale, pivot, pivot, duration).getAnimation();

        // plays the animation
        pickupAnimation.play();

        // adds a drop shadow effect to signal the node has been picked up
        resizePanel.setEffect(NODE_PICKUP_SHADOW);

        // marks the node as having been picked up
        pickup = true;
    }

    private void dropNode() {
        // if the node has not been picked up, exits the method
        if (!pickup) return;

        // selects the resize panel again
        resizePanel.setSelected(true);

        // generates the node dropping animation
        final Point2D toScale = new Point2D(1, 1);
        final Point2D toPivot = getNodeCenter();
        final Duration duration = Duration.millis(ZOOM_DURATION);

        final Animation pickupAnimation = new SmoothScale(
                pickupScale,
                toScale,
                toPivot,
                duration
        ).getAnimation();

        // plays the animation
        pickupAnimation.play();

        // resets the node's drop shadow
        resizePanel.setEffect(NODE_IDLE_SHADOW);

        // resets the node's pickup status
        pickup = false;
    }

    private void reset() {
        // drops the node in case the user is holding it
        dropNode();

        updateInterpolation(INTERPOLATION_MODE.RESET);

        // starts the reset animation
        if (resetAnimation.isRunning()) return;

        resetAnimation.start();
        animateScrollBarCentering(RESET_DURATION).play();
    }

    private void center(final boolean animated) {
        // centers the scroll bars & moves the node to the center
        if (animated && !centerAnimation.isRunning()) {
            updateInterpolation(INTERPOLATION_MODE.CENTER);
            centerAnimation.start();
            animateScrollBarCentering(RESET_DURATION).play();
        }
        else {
            hScroll.setValue(hScroll.getMax() / 2);
            vScroll.setValue(vScroll.getMax() / 2);

            moveTo(getResizeCenterInViewport().subtract(15, 15));
        }
    }

    private void moveTo(final Point2D target) {

        // determines the path to the target
        final Point2D pathTo = target.subtract(getResizePanelPosition());

        // updates the nodes translation
        drag.setX(drag.getX() + pathTo.getX());
        drag.setY(drag.getY() + pathTo.getY());

        // updates the node's progress
        updateProgress();
    }

    private void updateProgress() {
        // TODO: add commenting here

        final Bounds viewBounds = viewPort.getBoundsInParent();
        final double viewWidth = viewBounds.getWidth();
        final double viewHeight = viewBounds.getHeight();

        final Point2D resizeCenter = getResizePanelPosition().add(getResizePanelCenter()).add(15, 15);

        final double xProgress = resizeCenter.getX() / viewWidth;
        final double yProgress = resizeCenter.getY() / viewHeight;

        lastProgress = new Point2D(
                xProgress,
                yProgress
        );
    }

    private void horizontalScroll(ScrollEvent scrollEvent) {
        // determines if the user is scrolling to the right or the left
        final boolean scrollLeft = scrollEvent.getDeltaX() > 0;
        final boolean scrollRight = scrollEvent.getDeltaX() < 0;

        // applies scrolling in the desired direction
        if (scrollLeft) hScroll.setValue(Math.max(0, hScroll.getValue() - hScroll.getMax() * MOVEMENT_RATIO));
        else if (scrollRight) hScroll.setValue(Math.min(hScroll.getMax(), hScroll.getValue() + hScroll.getMax() * MOVEMENT_RATIO));
    }

    private void verticalScroll(ScrollEvent scrollEvent) {
        // determines if the user is scrolling to the top or the bottom
        final boolean scrollUp = scrollEvent.getDeltaY() > 0;
        final boolean scrollDown = scrollEvent.getDeltaY() < 0;

        // applies scrolling in the desired direction
        if (scrollUp) vScroll.setValue(Math.max(0, vScroll.getValue() - vScroll.getMax() * MOVEMENT_RATIO));
        else if (scrollDown) vScroll.setValue(Math.min(vScroll.getMax(), vScroll.getValue() + vScroll.getMax() * MOVEMENT_RATIO));
    }

    // =======================================
    //               ZOOMING
    // =======================================

    private void zoom(ScrollEvent scrollEvent) {
        // determines whether user is zooming in or zooming out
        final boolean zoomIn = scrollEvent.getDeltaY() > 0;
        final boolean zoomOut = scrollEvent.getDeltaY() < 0;
        final boolean noZoom = !zoomIn && !zoomOut;
        // determines if user has reached zoom limits
        final boolean maxZoomReached = zoom.getX() >= MAX_ZOOM;
        final boolean minZoomReached = zoom.getX() <= MIN_ZOOM;

        // exits method if no zoom is being applied or if scrolling would overstep zoom limits
        if (noZoom || (zoomIn && maxZoomReached) || (zoomOut && minZoomReached)) return;

        // determines zoom along the x-axis
        final double scaleX;
        if (zoomIn) scaleX = ZOOM_AMOUNT;
        else scaleX = 1 / ZOOM_AMOUNT;

        // determines zoom along the y-axis
        final double scaleY;
        if (zoomIn) scaleY = ZOOM_AMOUNT;
        else scaleY = 1 / ZOOM_AMOUNT;

        // gets the mouse's position & the zoom to apply
        final Point2D zoomAmount = new Point2D(scaleX, scaleY);

        // applies the zoom to the node
        zoomTo(zoomAmount);
    }

    private void zoomTo(final Point2D zoomAmount) {

        // gets the mouse's position relative to the associated node
        final Node associatedNode = movementPane.getMovementNode();
        final Point2D localPosition = associatedNode.screenToLocal(ScreenUtil.getMousePosition());

        // creates the zoom transformation
        final Scale newZoom = new Scale();
        newZoom.setPivotX(localPosition.getX()); // centers the zoom on the mouse's x-position
        newZoom.setPivotY(localPosition.getY()); // centers the zoom on the mouse's y-position
        newZoom.setX(zoomAmount.getX()); // sets the zoom's x-scale
        newZoom.setY(zoomAmount.getY()); // sets the zoom's y-scale

        // concatenates the new zoom to the previous zoom
        // final Scale totalZoom = EffectsUtil.transformToScale(zoom.createConcatenation(newZoom));
        final Scale totalZoom = EffectsUtil.concatenateScale(localPosition, zoom, newZoom);

        // updates zoom to match total zoom
        zoom.setX(totalZoom.getX());
        zoom.setY(totalZoom.getY());
        zoom.setPivotX(totalZoom.getPivotX());
        zoom.setPivotY(totalZoom.getPivotY());

        // applies zoom to the resize panel
        resizePanel.setZoom(totalZoom.getX(), totalZoom.getY(), totalZoom.getPivotX(), totalZoom.getPivotY());

        // updates the node's progress
        updateProgress();
    }

    // =======================================
    //               ANIMATION
    // =======================================

    // region INTERPOLATION

    private enum INTERPOLATION_MODE {
        RESET,
        CENTER,
        ALL
    }

    private void updateInterpolation(final INTERPOLATION_MODE mode) {
        // TODO: refactor the shit out of this

        if (mode.equals(INTERPOLATION_MODE.RESET) || mode.equals(INTERPOLATION_MODE.ALL)) {
            updateScaleInterpolation();
            updateResetXInterpolation();
            updateResetYInterpolation();
            updateResetPivotXInterpolation();
            updateResetPivotYInterpolation();
            updateResetWidthInterpolation();
            updateResetHeightInterpolate();
            updateResizeTranslateXInterpolate();
            updateResizeTranslateYInterpolate();
        }
        if (mode.equals(INTERPOLATION_MODE.CENTER) || mode.equals(INTERPOLATION_MODE.ALL)) {
            updateCenterXInterpolation();
            updateCenterYInterpolation();
            updateCenterPivotXInterpolation();
            updateCenterPivotYInterpolation();
            updateResizeTranslateXInterpolate();
            updateResizeTranslateYInterpolate();
        }
    }

    private void updateScaleInterpolation() {
        final double min = 1.0;
        final double max = zoom.getX();

        this.scaleInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResetXInterpolation() {
        final double min = getOriginalResizeCenterInViewport().getX();
        final double max = drag.getX();

        this.resetXInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResetYInterpolation() {
        final double min = getOriginalResizeCenterInViewport().getY();
        final double max = drag.getY();

        this.resetYInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResetPivotXInterpolation() {
        final double min = originalNodeSize.getWidth() / 2;
        final double max = zoom.getPivotX();

        this.resetPivotXInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResetPivotYInterpolation() {
        final double min = originalNodeSize.getHeight() / 2;
        final double max = zoom.getPivotY();

        this.resetPivotYInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResetWidthInterpolation() {
        final double min = originalNodeSize.getWidth();
        final double max = getNodeSize().getWidth();

        this.widthInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResetHeightInterpolate() {
        final double min = originalNodeSize.getHeight();
        final double max = getNodeSize().getHeight();

        this.heightInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateCenterXInterpolation() {
        final double min = getResizeCenterInViewport().getX();
        final double max = drag.getX();

        this.centerXInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateCenterYInterpolation() {
        final double min = getResizeCenterInViewport().getY();
        final double max = drag.getY();

        this.centerYInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateCenterPivotXInterpolation() {
        final double min = 0;
        final double max = zoom.getPivotX();

        this.centerPivotXInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateCenterPivotYInterpolation() {
        final double min = 0;
        final double max = zoom.getPivotY();

        this.centerPivotYInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResizeTranslateXInterpolate() {
        final double min = 0;
        final double max = resizePanel.translateProperty().get().getX();

        this.resizeTranslateXInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    private void updateResizeTranslateYInterpolate() {
        final double min = 0;
        final double max = resizePanel.translateProperty().get().getY();

        this.resizeTranslateYInterpolation = new BezierCurve(
                max,
                min,
                min,
                RESET_DURATION
        );
    }

    // endregion

    private void handleResetAnimation(final long now) {
        final double zoom = scaleInterpolation.interpolateAt(now);
        final double pivotX = resetPivotXInterpolation.interpolateAt(now);
        final double pivotY = resetPivotYInterpolation.interpolateAt(now);
        final double dragX = resetXInterpolation.interpolateAt(now);
        final double dragY = resetYInterpolation.interpolateAt(now);
        final double width = widthInterpolation.interpolateAt(now);
        final double height = heightInterpolation.interpolateAt(now);
        final double resizeX = resizeTranslateXInterpolation.interpolateAt(now);
        final double resizeY = resizeTranslateYInterpolation.interpolateAt(now);

        resizePanel.setResizeNodeSize(width, height);

        resizePanel.setZoom(zoom, zoom, pivotX, pivotY);
        drag.setX(dragX);
        drag.setY(dragY);

        final Translate resizeTranslate = resizePanel.translateProperty().get();
        resizeTranslate.setX(resizeX);
        resizeTranslate.setY(resizeY);
    }

    private void handleCenterAnimation(final long now) {
        final double pivotX = centerPivotXInterpolation.interpolateAt(now);
        final double pivotY = centerPivotYInterpolation.interpolateAt(now);
        final double dragX = centerXInterpolation.interpolateAt(now);
        final double dragY = centerYInterpolation.interpolateAt(now);

        resizePanel.setZoom(zoom.getX(), zoom.getY(), pivotX, pivotY);
        drag.setX(dragX);
        drag.setY(dragY);
    }

    private void handleResetEnd(final ActionEvent actionEvent) {
        // TODO: add commenting here

        zoom.setX(1);
        zoom.setY(1);
        zoom.setPivotX(originalNodeSize.getWidth() / 2);
        zoom.setPivotY(originalNodeSize.getHeight() / 2);

        final Point2D resizePanelCenter = getOriginalResizeCenterInViewport();
        drag.setX(resizePanelCenter.getX());
        drag.setY(resizePanelCenter.getY());

        updateProgress();
    }

    private void handleCenterEnd(final ActionEvent actionEvent) {

        zoom.setPivotX(0);
        zoom.setPivotY(0);

        final Point2D resizePanelCenter = getResizeCenterInViewport();
        drag.setX(resizePanelCenter.getX());
        drag.setY(resizePanelCenter.getY());

        updateProgress();
    }

    private Animation animateScrollBarCentering(final long duration) {
        // animates the scrollbars being centered
        return new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                hScroll.valueProperty(),
                                hScroll.getValue()
                        ),
                        new KeyValue(
                                vScroll.valueProperty(),
                                vScroll.getValue()
                        )
                ),
                new KeyFrame(
                        Duration.millis(duration),
                        new KeyValue(
                                hScroll.valueProperty(),
                                hScroll.getMax() / 2
                        ),
                        new KeyValue(
                                vScroll.valueProperty(),
                                vScroll.getMax() / 2
                        )
                )
        );
    }

    // =======================================
    //               COLLISION
    // =======================================

    private void detectCollision() {

        // does noe check for collisions if user is panning

        // gets the node's current position
        final Point2D resizePanelPosition = getResizePanelPosition();

        // gets the size of the display
        final double width = viewPort.getWidth();
        final double height = viewPort.getHeight();

        // checks for collisions
        final boolean leftCollision = resizePanelPosition.getX() <= 0;
        final boolean rightCollision = resizePanelPosition.getX() >= width - getResizePanelWidth() - 1;
        final boolean topCollision = resizePanelPosition.getY() <= 0;
        final boolean bottomCollision = resizePanelPosition.getY() >= height - getResizePanelHeight() - 1;

        // checks for horizontal collisions
        if (leftCollision || rightCollision) {
            setHorizontalCollision(true);
            horizontalCollision();
        }
        else setHorizontalCollision(false);
        // checks for vertical collisions
        if (topCollision || bottomCollision) {
            setVerticalCollision(true);
            verticalCollision();
        }
        else setVerticalCollision(false);
    }

    private void horizontalCollision() {

        // does not check for collisions if user in not dragging node
        if (!dragging) return;

        // gets the position of the resize panel
        final Point2D resizePosition = getResizePanelPosition();

        // determines if the collision is to the left or the right
        final boolean isLeft = resizePosition.getX() <= 0;
        final boolean isRight = resizePosition.getX() >= viewPort.getWidth() - getResizePanelWidth();

        if      (isLeft) leftCollision.start();
        else if (isRight) rightCollision.start();
        else stopHorizontalCollisions();
    }

    private void verticalCollision() {

        // does not check for collisions if user in not dragging node
        if (!dragging) return;

        // gets the position of the resize panel
        final Point2D resizePosition = getResizePanelPosition();

        // determines if the collision is to the top or the bottom
        final boolean isTop = resizePosition.getY() <= 0;
        final boolean isBottom = resizePosition.getY() >= viewPort.getHeight() - getResizePanelHeight() - 1;

        if      (isTop) topCollision.start();
        else if (isBottom) bottomCollision.start();
        else stopVerticalCollisions();
    }

    private void decreaseAnimation(final ScrollBar scrollBar) {
        // if the scroll takes up all visible space, sets its value to its minimum instead
        if (scrollBar.getVisibleAmount() == scrollBar.getMax()) scrollBar.setValue(scrollBar.getMin());

        // determines if there is space on the top
        final boolean topSpace = scrollBar.getValue() != 0;

        // moves the slider to the top if there is space left...
        if (topSpace) scrollBar.setValue(Math.max(0, scrollBar.getValue() - scrollBar.getMax() * MOVEMENT_RATIO));
            // ...otherwise increases available space
        else scrollBar.setMax(Math.min(scrollBar.getMax() * GROWTH, MAX_SIZE));

        // updates the scroll bar's last value
        if (scrollBar.equals(vScroll)) lastVValue = scrollBar.getValue();
        else lastHValue = scrollBar.getValue();

    }

    private void increaseAnimation(final ScrollBar scrollBar) {
        // if the scroll takes up all visible space, sets its value to its maximum instead
        if (scrollBar.getVisibleAmount() == scrollBar.getMax()) scrollBar.setValue(scrollBar.getMax());

        // determines if there is space to the bottom
        final boolean topSpace = scrollBar.getValue() != scrollBar.getMax();

        // moves the slider to the bottom if there is space left...
        if (topSpace) scrollBar.setValue(Math.min(scrollBar.getMax(), scrollBar.getValue() + scrollBar.getMax() * MOVEMENT_RATIO));
            // ...otherwise increases available space and makes sure scroll bar stays to the bottom
        else {
            scrollBar.setMax(Math.min(scrollBar.getMax() * GROWTH, MAX_SIZE));
            scrollBar.setValue(scrollBar.getMax());
        }

        if (scrollBar.equals(vScroll)) lastVValue = scrollBar.getValue();
        else lastHValue = scrollBar.getValue();

    }

    private void stopHorizontalCollisions() {
        leftCollision.stop();
        rightCollision.stop();
    }

    private void stopVerticalCollisions() {
        topCollision.stop();
        bottomCollision.stop();
    }

    private void setHorizontalCollision(final boolean collision) {
        horizontalCollision.set(collision);
    }

    private void setVerticalCollision(final boolean collision) {
        verticalCollision.set(collision);
    }

    // =======================================
    //         RESIZING / POSITIONING
    // =======================================

    private void resize() {

        // determines the size of the MovementPane
        final double width = movementPane.getWidth();
        final double height = movementPane.getHeight();

        // determines the current thickness of the sliders
        final double sliderThickness = hScroll.getHeight();

        // determines the new size of the sliders
        final double hScrollLength = width - sliderThickness;
        final double vScrollLength = height - sliderThickness;

        // updates the size of the sliders
        hScroll.setPrefWidth(hScrollLength);
        vScroll.setPrefHeight(vScrollLength);

        // updates the size of the viewport
        viewPort.setPrefWidth(hScrollLength);
        viewPort.setPrefHeight(vScrollLength);

        // determines the new position of the node
        if (movementPane.getMovementNode() != null) {
            Platform.runLater(() -> {
                final Bounds viewBounds = viewPort.getBoundsInParent();
                final double xPosition = lastProgress.getX() * viewBounds.getWidth();
                final double yPosition = lastProgress.getY() * viewBounds.getHeight();

                final Point2D target = new Point2D(xPosition, yPosition)
                        .subtract(getResizePanelCenter())
                        .subtract(15, 15);

                moveTo(target);
            });
        }

        // updates the viewport's clip to match the new size
        viewPort.setClip(new Rectangle(hScrollLength, vScrollLength));

        // repositions the nodes accordingly
        reposition();
    }

    private void reposition() {

        // determines the size of the MovementPane
        final double width = movementPane.getWidth();
        final double height = movementPane.getHeight();

        // determines the thickness of the sliders
        final double sliderThickness = hScroll.getHeight();

        // determines where to place the horizontal slider
        final double deltaYHorizontal = height - sliderThickness;

        // determines where to place the vertical slider
        final double deltaXVertical = width - sliderThickness;

        // repositions the sliders
        hScroll.setLayoutY(deltaYHorizontal);
        vScroll.setLayoutX(deltaXVertical);
    }

    // =======================================
    //            ASSOCIATED NODE
    // =======================================

    protected void bindNode(final Node node) {
        resizePanel.setResizeNode(node);

        final Rectangle2D nodeSize = getNodeSize();
        originalNodeSize = new Rectangle2D(
                0,
                0,
                nodeSize.getWidth(),
                nodeSize.getHeight()
        );

        Platform.runLater(() -> center(false));
    }

    private Rectangle2D getNodeSize() {
        // gets the associated node
        final Node associatedNode = movementPane.getMovementNode();

        // checks that the associated node is not null
        if (associatedNode == null) return new Rectangle2D(0, 0, 0, 0);

        // gets the node's bounds...
        final Bounds bounds = associatedNode.getBoundsInParent();
        // ...and determines its size
        final double nodeWidth = bounds.getWidth();
        final double nodeHeight = bounds.getHeight();

        // returns the computed size
        return new Rectangle2D(
                0,
                0,
                nodeWidth,
                nodeHeight
        );
    }

    // =======================================
    //             RESIZE PANEL
    // =======================================

    private Point2D getResizePanelPosition() {
        final Bounds bounds = resizePanel.getBoundsInParent();
        return new Point2D(bounds.getMinX(), bounds.getMinY());
    }

    private double getResizePanelWidth() {
        return getNodeSize().getWidth() + resizePanel.getArrowLength() * 2 + resizePanel.getArrowSpace() * 2;
    }

    private double getResizePanelHeight() {
        return getNodeSize().getHeight() + resizePanel.getArrowLength() * 2 + resizePanel.getArrowSpace() * 2;
    }

    private double getOriginalResizePanelWidth() {
        return originalNodeSize.getWidth() + resizePanel.getArrowLength() * 2 + resizePanel.getArrowSpace() * 2;
    }

    private double getOriginalResizePanelHeight() {
        return originalNodeSize.getHeight() + resizePanel.getArrowLength() * 2 + resizePanel.getArrowSpace() * 2;
    }

    private Point2D centerOnResizePanel(final Point2D target) {
        return target.subtract(getResizePanelCenter());
    }

    // =======================================
    //          CENTERING COORDINATES
    // =======================================

    private Point2D getNodeCenter() {
        final Node associatedNode = movementPane.getMovementNode();
        final Bounds bounds = associatedNode.getBoundsInParent();
        return new Point2D(bounds.getWidth() / 2, bounds.getHeight() / 2);
    }

    private Point2D getResizePanelCenter() {
        final double width = getResizePanelWidth();
        final double height = getResizePanelHeight();
        return new Point2D(width / 2, height / 2);
    }

    private Point2D getOriginalResizePanelCenter() {
        final double originalWidth = getOriginalResizePanelWidth();
        final double originalHeight = getOriginalResizePanelHeight();
        return new Point2D(originalWidth / 2, originalHeight / 2);
    }

    private Point2D getViewportCenter() {
        // determines the size of the viewport
        final double width = viewPort.getBoundsInParent().getWidth();
        final double height = viewPort.getBoundsInParent().getHeight();

        // determines the center of the viewport
        return new Point2D(width / 2, height / 2);
    }

    private Point2D getResizeCenterInViewport() {
        return getViewportCenter().subtract(getResizePanelCenter());
    }

    private Point2D getOriginalResizeCenterInViewport() {
        return getViewportCenter().subtract(getOriginalResizePanelCenter());
    }

}
