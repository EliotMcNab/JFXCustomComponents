package app.customControls.controls.movementPane;

import app.customControls.controls.time.TimedAnimation;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;

import java.util.HashSet;

import static app.customControls.utilities.KeyboardUtil.Letters.SPACE;
import static app.customControls.utilities.KeyboardUtil.areKeysDown;

public class MovementPaneSkin extends SkinBase<MovementPane> implements Skin<MovementPane> {

    // =======================================
    //                FIELDS
    // =======================================

    // constants

    private static final long ANIMATION_DELAY = 10;

    // movement pane

    private final MovementPane movementPane;

    // movement

    private Point2D lastPosition;
    private Point2D lastProgression;
    private final HashSet<KeyCode> keyPresses;
    private final TimedAnimation leftCollision;

    // components

    private final ScrollBar hScroll;
    private final ScrollBar vScroll;
    private final Pane background;
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
    private final EventHandler<MouseEvent> dragListener;
    private final EventHandler<KeyEvent> keyPressListener;
    private final EventHandler<KeyEvent> keyReleaseListener;

    // =======================================
    //              CONSTRUCTOR
    // =======================================

    protected MovementPaneSkin(MovementPane movementPane) {
        super(movementPane);

        this.movementPane = movementPane;

        // initialising variable

        this.keyPresses = new HashSet<>();
        this.lastPosition = new Point2D(0, 0);
        this.lastProgression = new Point2D(0, 0);

        this.leftCollision = new TimedAnimation(ANIMATION_DELAY) {
            @Override
            public void run() {
                hScroll.setVisibleAmount(Math.max(1, hScroll.getVisibleAmount() * 0.99));
            }
        };

        // initialises components

        this.hScroll = new ScrollBar();
        this.hScroll.setOrientation(Orientation.HORIZONTAL);
        this.hScroll.setMin(movementPane.getHorizontalMin());
        this.hScroll.setMax(movementPane.getHorizontalMax());
        this.hScroll.setVisibleAmount(movementPane.getHorizontalMax());
        this.hScroll.setValue(movementPane.getHValue());

        this.vScroll = new ScrollBar();
        this.vScroll.setOrientation(Orientation.VERTICAL);
        this.vScroll.setMin(movementPane.getVerticalMin());
        this.vScroll.setMax(movementPane.getVerticalMax());
        this.vScroll.setVisibleAmount(movementPane.getVerticalMax());
        this.vScroll.setValue(movementPane.getVValue());

        this.background = new Pane();
        this.container = new Pane();

        // initialise properties

        this.horizontalCollision = new SimpleBooleanProperty(this, "horizontalCollision", false);
        this.verticalCollision = new SimpleBooleanProperty(this, "verticalCollision", false);

        // initialise listeners

        this.resizeListener = observable -> resize();
        this.hValueListener = observable -> handleHValueChange();
        this.vValueListener = observable -> handleVValueChange();
        this.nodeChangeListener = observable -> bindNode(movementPane.getAssociatedNode());
        this.collisionListener = observable -> collisionDetection();
        this.hCollisionListener = (observableValue, aBoolean, t1) -> horizontalCollision();
        this.vCollisionListener = (observableValue, aBoolean, t1) -> verticalCollision();
        this.mousePressListener = this::handleMousePress;
        this.dragListener = this::handleDrag;
        this.keyPressListener = this::handleKeyPress;
        this.keyReleaseListener = this::handleKeyRelease;

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
        container.getChildren().addAll(background, hScroll, vScroll);
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
        background.setOnMousePressed(mousePressListener);
        background.setOnMouseDragged(dragListener);
        movementPane.setOnKeyPressed(keyPressListener);
        movementPane.setOnKeyReleased(keyReleaseListener);

        // collisions
        horizontalCollision.addListener(hCollisionListener);
        verticalCollision.addListener(vCollisionListener);
    }

    // =======================================
    //               USER INPUT
    // =======================================

    private void handleMousePress(final MouseEvent mouseEvent) {
        final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());

        // teleports the node if user it is not being dragged
        if (!areKeysDown(keyPresses, SPACE)) {
            lastPosition = getNodePosition();
            moveTo(mousePosition);
            updateProgress();
        } else {
            lastPosition = centerOnNode(mousePosition);
        }
    }

    private void handleDrag(final MouseEvent mouseEvent) {
        // checks that the SPACE key is being pressed to allow for drag
        if (!areKeysDown(keyPresses, SPACE)) return;
        // determines the current position of the mouse
        final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        moveTo(mousePosition);

        updateProgress();
    }

    private void handleKeyPress(final KeyEvent keyEvent) {
        final KeyCode keyCode = keyEvent.getCode();
        // if the key is not already pressed, adds it to the currently pressed keys
        if (!keyPresses.contains(keyCode)) keyPresses.add(keyCode);

        if (keyCode.equals(KeyCode.C)) center();
    }

    private void handleKeyRelease(final KeyEvent keyEvent) {
        // tries to remove the key for the set of pressed keys once it is released
        keyPresses.remove(keyEvent.getCode());
    }

    // =======================================
    //               MOVEMENT
    // =======================================

    private void handleHValueChange() {
        System.out.println(hScroll.getValue());
    }

    private void handleVValueChange() {

    }

    private void center() {

        // determines the size of the background
        final double width = background.getWidth();
        final double height = background.getHeight();

        // determines the position of the node at the center
        final double centerX = (width - getNodeWidth()) / 2;
        final double centerY = (height - getNodeHeight()) / 2;

        // moves the node to the new position
        lastPosition = getNodePosition();
        moveTo(new Point2D(centerX, centerY));

    }

    private void moveTo(final Point2D target) {

        // centers the target onto the node
        final Point2D adjustedTarget = centerOnNode(target);

        // determines the path to the target & makes sur they would not bring the node out of bounds
        final Point2D boundTarget = boundNodeMovement(adjustedTarget.subtract(lastPosition));

        // applies the displacement
        final Translate translate = new Translate();
        translate.setX(boundTarget.getX());
        translate.setY(boundTarget.getY());
        movementPane.getAssociatedNode().getTransforms().add(translate);

        // saves the new position
        lastPosition = adjustedTarget;
    }

    private Point2D boundNodeMovement(final Point2D target) {
        // determines the current position of the node
        final Point2D nodePosition = getNodePosition();

        // determines the size of the displays
        final double availableWidth = background.getWidth();
        final double availableHeight = background.getHeight();
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

    // =======================================
    //               COLLISION
    // =======================================

    private void collisionDetection() {
        // gets the node's current position
        final Point2D nodePosition = getNodePosition();

        // gets the size of the display
        final double width = background.getWidth();
        final double height = background.getHeight();

        // checks for collisions
        final boolean leftCollision = nodePosition.getX() <= 0;
        final boolean rightCollision = nodePosition.getX() >= width - getNodeWidth() - 1;
        final boolean topCollision = nodePosition.getY() <= 0;
        final boolean bottomCollision = nodePosition.getY() >= height - getNodeHeight() - 1;

        // checks for horizontal collisions
        if (leftCollision || rightCollision) setHorizontalCollision(true);
        else setHorizontalCollision(false);
        // checks for vertical collisions
        if (topCollision || bottomCollision) setVerticalCollision(true);
        else setVerticalCollision(false);
    }

    private void horizontalCollision() {
        // determines if the collision is to the left or the right
        final boolean isLeft = getNodeX() <= 0;
        final boolean isRight = getNodeX() >= background.getWidth() - getNodeWidth();

        // gets the space to the left & the right of the node
        // final boolean leftSpace =

        if (isLeft) leftCollision.start();
        else leftCollision.stop();
    }

    private void verticalCollision() {
        // System.out.printf("vertical collision:%s\n", getVerticalCollision());
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

        // updates the size of the background
        background.setPrefWidth(hScrollLength);
        background.setPrefHeight(vScrollLength);

        // determines the new position of the node
        if (movementPane.getAssociatedNode() != null) {
            final double adjustX = lastProgression.getX() * width;
            final double adjustY = lastProgression.getY() * height;
            final Point2D target = new Point2D(adjustX, adjustY);
            lastPosition = getNodePosition();
            moveTo(target);
        }

        // updates the background's clip to match the new size
        background.setClip(new Rectangle(hScrollLength, vScrollLength));

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
        background.getChildren().clear();
        background.getChildren().add(node);
        node.boundsInParentProperty().addListener(collisionListener);
    }

    private Rectangle2D getNodeSize() {
        final Node associatedNode = movementPane.getAssociatedNode();

        // checks that the associated node is not null
        if (associatedNode == null) return new Rectangle2D(0, 0, 0, 0);

        final Bounds bounds = associatedNode.getBoundsInLocal();
        return new Rectangle2D(0, 0, bounds.getMaxX(), bounds.getMaxY());
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
        return new Point2D(bounds.getCenterX(), bounds.getCenterY());
    }

    private double getNodeCenterX() {
        return getNodeCenter().getX();
    }

    private double getNodeCenterY() {
        return getNodeCenter().getY();
    }

}
