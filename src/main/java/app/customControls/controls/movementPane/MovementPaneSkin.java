package app.customControls.controls.movementPane;

import app.customControls.controls.time.TimedAnimation;
import app.customControls.utilities.EffectsUtil;
import app.customControls.utilities.KeyboardUtil;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.transform.Translate;

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
    private static final long PICKUP_DURATION = 100;
    private static final double PICKUP_SCALE = 1.05;
    private static final double SCALE_GROWTH = 1.01;
    private static final long MOVEMENT_DURATION = 500;

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

    private Translate nodeMovement;
    private Point2D lastMousePosition;
    private Point2D lastProgression;
    private double lastHValue;
    private double lastVValue;
    private final HashSet<KeyboardUtil.Key> keyPresses;
    private final TimedAnimation leftCollision;
    private final TimedAnimation rightCollision;
    private final TimedAnimation topCollision;
    private final TimedAnimation bottomCollision;
    private boolean pickup;

    // zooming

    private double scale;

    // components

    private final ScrollBar hScroll;
    private final ScrollBar vScroll;
    private final Pane viewPort;
    private final Pane container;

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
    private final EventHandler<MouseEvent> scrollClickListener;
    private final EventHandler<KeyEvent> keyPressListener;
    private final EventHandler<KeyEvent> keyReleaseListener;
    private final EventHandler<ScrollEvent> scrollListener;

    // =======================================
    //              CONSTRUCTOR
    // =======================================

    protected MovementPaneSkin(MovementPane movementPane) {
        super(movementPane);

        this.movementPane = movementPane;

        // initialises components

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

        // initialise properties

        this.horizontalCollision = new SimpleBooleanProperty(this, "horizontalCollision", false);
        this.verticalCollision = new SimpleBooleanProperty(this, "verticalCollision", false);

        // initialise listeners

        this.resizeListener = observable -> resize();
        this.hValueListener = observable -> handleHValueChange();
        this.vValueListener = observable -> handleVValueChange();
        this.nodeChangeListener = observable -> bindNode(movementPane.getAssociatedNode());
        this.collisionListener = observable -> detectCollision();
        this.hCollisionListener = (observableValue, aBoolean, t1) -> horizontalCollision();
        this.vCollisionListener = (observableValue, aBoolean, t1) -> verticalCollision();
        this.mousePressListener = this::handleMousePress;
        this.mouseReleaseListener = this::handleMouseRelease;
        this.dragListener = this::handleDrag;
        this.scrollClickListener = this::handleSliderClick;
        this.keyPressListener = this::handleKeyPress;
        this.keyReleaseListener = this::handleKeyRelease;
        this.scrollListener = this::handleScroll;

        // initialising variable

        this.nodeMovement = new Translate();
        this.keyPresses = new HashSet<>();
        this.lastMousePosition = new Point2D(0, 0);
        this.lastProgression = new Point2D(0, 0);
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
        movementPane.getStyleClass().add("movement-pane");
    }

    private void populate() {
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

        // zooming
        viewPort.setOnScroll(scrollListener);

        // collisions
        horizontalCollision.addListener(hCollisionListener);
        verticalCollision.addListener(vCollisionListener);

        // sliders
        hScroll.setOnMouseClicked(scrollClickListener);
    }

    // =======================================
    //               USER INPUT
    // =======================================

    private void handleMousePress(final MouseEvent mouseEvent) {
        // determines and saves the current mouse position
        final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        lastMousePosition = mousePosition;

        // teleports the node if user it is not being dragged
        if (!areKeysDown(keyPresses, SPACE)) moveTo(centerOnNode(mousePosition));
        else pickupNode();
    }

    private void handleMouseRelease(final MouseEvent mouseEvent) {
        dropNode();
    }

    private void handleDrag(final MouseEvent mouseEvent) {
        // checks that the SPACE key is being pressed to allow for drag
        if (!areKeysDown(keyPresses, SPACE)) return;

        // determines the current position of the mouse
        final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());

        // determines by how much the mouse has moved since last time
        final Point2D dragAmount = mousePosition.subtract(lastMousePosition);

        // applies the drag to the node
        moveTo(getNodePosition().add(dragAmount));

        // saves the current mouse position
        lastMousePosition = mousePosition;
    }

    private void handleKeyPress(final KeyEvent keyEvent) {
        // if the key is not already pressed, adds it to the currently pressed keys
        saveKeyPress(keyEvent, keyPresses);

        // dragging
        if (areKeysDown(keyPresses, SPACE)) movementPane.setCursor(Cursor.CLOSED_HAND);

        // centering
        if (areKeysDown(keyPresses, CTRL, SHIFT, R)) center(true);
    }

    private void handleKeyRelease(final KeyEvent keyEvent) {
        // tries to remove the key for the set of pressed keys once it is released
        removeKeyPress(keyEvent, keyPresses);

        // if user stopped dragging node, stops adding space
        if (keyEvent.getCode().equals(KeyCode.SPACE)) {
            stopHorizontalCollisions();
            stopVerticalCollisions();
            // dropNode();
            movementPane.setCursor(Cursor.DEFAULT);
        }
    }

    private void handleScroll(final ScrollEvent scrollEvent) {
        if (scrollEvent.isShiftDown()) horizontalScroll(scrollEvent);
        else verticalScroll(scrollEvent);
    }

    // =======================================
    //              SCROLL BARS
    // =======================================

    private void handleSliderClick(MouseEvent mouseEvent) {
        // determines the origin of the click
        final Node origin = (Node) mouseEvent.getSource();

        // moves the node accordingly
        moveScrollToClick((ScrollBar) origin, mouseEvent);
    }

    private void moveScrollToClick(ScrollBar scrollBar, MouseEvent mouseEvent) {
        // determines the progression of the mouse along the scroll bar
        final double scrollProgress = mouseEvent.getX() / scrollBar.getWidth();
        final double mouseValue = scrollProgress * scrollBar.getMax();

        // updates the scroll bar to the new value
        scrollBar.setValue(mouseValue);
    }

    private void handleHValueChange() {
        // makes sure that tne node is not being dragged at the same time
        if (areKeysDown(keyPresses, SPACE)) return;

        // determines the current slider value
        final double currentValue = hScroll.getValue();

        // calculates the node's new position based on the slider's movement
        final double deltaX = currentValue - lastHValue;
        final Point2D target = getNodePosition().subtract(deltaX, 0);
        // moves the node to the target position
        moveTo(target);

        // updates the slider's last value
        lastHValue = currentValue;
    }

    private void handleVValueChange() {
        // makes sure that tne node is not being dragged at the same time
        if (areKeysDown(keyPresses, SPACE)) return;

        // determines the current slider value
        final double currentValue = vScroll.getValue();

        // calculates the node's new position based on the slider's movement
        final double deltaY = currentValue - lastVValue;
        final Point2D target = getNodePosition().subtract(0, deltaY);
        // moves the node to the target position
        moveTo(target);

        // updates the slider's last value
        lastVValue = currentValue;
    }

    // =======================================
    //               MOVEMENT
    // =======================================

    private void pickupNode() {
        // checks for collisions
        detectCollision();

        // if node has already been picked exits method
        if (pickup) return;

        // gets the associated node
        final Node associatedNode = movementPane.getAssociatedNode();

        // picks up the node
        final ScaleTransition pickupScale = EffectsUtil.generateScaleTransition(
                PICKUP_DURATION,
                PICKUP_SCALE,
                PICKUP_SCALE,
                associatedNode
        );
        pickupScale.play();

        // adds a drop shadow effect to signal the node has been picked up
        associatedNode.setEffect(NODE_PICKUP_SHADOW);

        // marks the node as having been picked up
        pickup = true;
    }

    private void dropNode() {
        // if the node has not been picked up, exits the method
        if (!pickup) return;

        // gets the node associated to the movement pane
        final Node associatedNode = movementPane.getAssociatedNode();

        // applies the inverse scaling as compared to picking up the node
        final ScaleTransition dropScale = EffectsUtil.generateScaleTransition(
                PICKUP_DURATION,
                1,
                1,
                associatedNode
        );
        dropScale.play();

        // resets the node's drop shadow
        associatedNode.setEffect(NODE_IDLE_SHADOW);

        // resets the node's scales
        associatedNode.setScaleX(1);
        associatedNode.setScaleY(1);

        // resets the node's pickup status
        pickup = false;
    }

    private void center(final boolean animated) {
        // centers the scroll bars
        hScroll.setValue(hScroll.getMax() / 2);
        vScroll.setValue(vScroll.getMax() / 2);

        // determines the size of the viewport
        final double width = viewPort.getWidth();
        final double height = viewPort.getHeight();

        // determines the position of the node at the center
        final double centerX = (width - getNodeWidth()) / 2;
        final double centerY = (height - getNodeHeight()) / 2;

        // moves the node to the new position
        final Point2D target = new Point2D(centerX, centerY);
        if (animated) moveToAnimated(target);
        else moveTo(target);
    }

    private void moveTo(final Point2D target) {

        // gets the node associated to the movement pane
        final Node associatedNode = movementPane.getAssociatedNode();

        // determines the path to the target
        final Point2D pathTo = target.subtract(getNodePosition());

        // calculates the displacement
        final Translate translate = new Translate();
        translate.setX(pathTo.getX());
        translate.setY(pathTo.getY());

        // adds the displacement to those previously applied to the node
        nodeMovement = (Translate) nodeMovement.createConcatenation(translate);

        // clears the node's transformations and applies the new, simplified displacement
        associatedNode.getTransforms().clear();
        associatedNode.getTransforms().add(nodeMovement);

        // saves the node's progress
        updateProgress();
    }

    private void moveToAnimated(final Point2D target) {
        // gets the associated node
        final Node associatedNode = movementPane.getAssociatedNode();

        // determines the path to the specified target
        final Point2D pathTo = target.subtract(getNodePosition());

        // applies the translation
        final TranslateTransition animatedTranslation = new TranslateTransition();
        animatedTranslation.setNode(associatedNode);
        animatedTranslation.setFromX(associatedNode.getTranslateX());
        animatedTranslation.setFromY(associatedNode.getTranslateY());
        animatedTranslation.setByX(pathTo.getX());
        animatedTranslation.setByY(pathTo.getY());
        animatedTranslation.play();

        // saves the node's progress once the animation has finished
        // & resets its translation
        animatedTranslation.setOnFinished(actionEvent -> {
            updateProgress();
            final Point2D nodePosition = getNodePosition();
            associatedNode.setTranslateX(0);
            associatedNode.setTranslateY(0);
            moveTo(nodePosition);
        });

    }

    private Point2D boundNodeMovement(final Point2D target) {
        // determines the current position of the node
        final Point2D nodePosition = getNodePosition();

        // determines the size of the displays
        final double availableWidth = viewPort.getWidth();
        final double availableHeight = viewPort.getHeight();
        // determines the size of the node
        final double nodeWidth = getNodeWidth();
        final double nodeHeight = getNodeHeight();

        // determines the node's final position
        final Point2D finalPosition = nodePosition.add(target);

        // determines of the node will overlap
        final boolean overlapLeft = finalPosition.getX() < 0;
        final boolean overlapRight = finalPosition.getX() > availableWidth - nodeWidth;
        final boolean overlapTop = finalPosition.getY() < 0;
        final boolean overlapBottom = finalPosition.getY() > availableHeight - nodeHeight;

        final double deltaX;
        final double deltaY;

        // handles overlap in final node position
             if (overlapLeft)   deltaX = -nodePosition.getX();
        else if (overlapRight)  deltaX = availableWidth - nodePosition.getX() - nodeWidth;
        else                    deltaX = target.getX();

             if (overlapTop)    deltaY = -nodePosition.getY();
        else if (overlapBottom) deltaY = availableHeight - nodePosition.getY() - nodeHeight;
        else                    deltaY = target.getY();

        // returns transition after having taken care of overlap
        return new Point2D(deltaX, deltaY);
    }

    private void updateProgress() {
        // determines the size of the display
        final double width = movementPane.getWidth();
        final double height = movementPane.getHeight();
        // determines the progression of the node
        final Point2D nodePosition = getNodePosition().add(getNodeCenter());
        final double xProgress = nodePosition.getX() / width;
        final double yProgress = nodePosition.getY() / height;
        // saves the node's current progression
        lastProgression = new Point2D(xProgress, yProgress);
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


    // =======================================
    //               COLLISION
    // =======================================

    private void detectCollision() {
        // gets the node's current position
        final Point2D nodePosition = getNodePosition();

        // gets the size of the display
        final double width = viewPort.getWidth();
        final double height = viewPort.getHeight();

        // checks for collisions
        final boolean leftCollision = nodePosition.getX() <= 0;
        final boolean rightCollision = nodePosition.getX() >= width - getNodeWidth() - 1;
        final boolean topCollision = nodePosition.getY() <= 0;
        final boolean bottomCollision = nodePosition.getY() >= height - getNodeHeight() - 1;

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

        // does not consider collision if node is not being dragged
        if (!areKeysDown(keyPresses, SPACE)) return;

        // determines if the collision is to the left or the right
        final boolean isLeft = getNodeX() <= 0;
        final boolean isRight = getNodeX() >= viewPort.getWidth() - getNodeWidth();

        if (isLeft) leftCollision.start();
        else if (isRight) rightCollision.start();
        else stopHorizontalCollisions();
    }

    private void verticalCollision() {

        // does not consider collision if node is not being dragged
        if (!areKeysDown(keyPresses, SPACE)) return;

        // determines if the collision is to the top or the bottom
        final boolean isTop = getNodeY() <= 0;
        final boolean isBottom = getNodeY() >= viewPort.getHeight() - getNodeHeight() - 1;

        if (isTop) topCollision.start();
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

    private boolean getHorizontalCollision() {
        return horizontalCollision.get();
    }

    private void setVerticalCollision(final boolean collision) {
        verticalCollision.set(collision);
    }

    private boolean getVerticalCollision() {
        return verticalCollision.get();
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
        if (movementPane.getAssociatedNode() != null) {
            final double adjustX = lastProgression.getX() * width;
            final double adjustY = lastProgression.getY() * height;
            final Point2D target = new Point2D(adjustX, adjustY);
            moveTo(target.subtract(getNodeCenter()));
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

    private void bindNode(final Node node) {
        viewPort.getChildren().clear();
        viewPort.getChildren().add(node);
        node.boundsInParentProperty().addListener(collisionListener);
        node.setEffect(NODE_IDLE_SHADOW);
        node.getTransforms().add(nodeMovement);
        Platform.runLater(() -> center(false));
    }

    private Rectangle2D getNodeSize() {
        final Node associatedNode = movementPane.getAssociatedNode();

        // checks that the associated node is not null
        if (associatedNode == null) return new Rectangle2D(0, 0, 0, 0);

        final Bounds bounds = associatedNode.getBoundsInLocal();
        return new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY());
    }

    private double getNodeHeight() {
        return getNodeSize().getHeight();
    }

    private double getNodeWidth() {
        return getNodeSize().getWidth();
    }

    private Point2D getNodePosition() {
        final Node associatedNode = movementPane.getAssociatedNode();

        // checks that the associated node is not null
        if (associatedNode == null) return new Point2D(0, 0);

        final Bounds bounds = associatedNode.getBoundsInParent();
        return new Point2D(bounds.getMinX(), bounds.getMinY());
    }

    private double getNodeX() {
        return getNodePosition().getX();
    }

    private double getNodeY() {
        return getNodePosition().getY();
    }

    private Point2D centerOnNode(final Point2D target) {
        final Node associatedNode = movementPane.getAssociatedNode();

        // checks that the associated node is not null
        if (associatedNode == null) return target;

        final Point2D nodeCenter = getNodeCenter();
        final double adjustedX = target.getX() - nodeCenter.getX();
        final double adjustedY = target.getY() - nodeCenter.getY();
        return new Point2D(adjustedX, adjustedY);
    }

    private Point2D getNodeCenter() {
        final Node associatedNode = movementPane.getAssociatedNode();

        // checks that the associated node is not null
        if (associatedNode == null) return new Point2D(0, 0);

        final Bounds bounds = associatedNode.getBoundsInLocal();
        return new Point2D(bounds.getWidth() / 2, bounds.getHeight() / 2);
    }

    private double getNodeCenterX() {
        return getNodeCenter().getX();
    }

    private double getNodeCenterY() {
        return getNodeCenter().getY();
    }

}
