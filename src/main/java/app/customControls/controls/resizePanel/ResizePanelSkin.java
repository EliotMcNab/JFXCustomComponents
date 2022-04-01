package app.customControls.controls.resizePanel;

import app.customControls.controls.shapes.Arrow;
import app.customControls.controls.shapes.Orientation;
import app.customControls.utilities.KeyboardUtil;
import app.customControls.utilities.ScreenUtil;
import app.customControls.utilities.TransformUtil;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import javax.swing.*;

import static app.customControls.controls.shapes.Orientation.*;
import static app.customControls.utilities.KeyboardUtil.Letter.*;
import static app.customControls.utilities.KeyboardUtil.Modifier.CTRL;

public class ResizePanelSkin extends SkinBase<ResizePanel> implements Skin<ResizePanel> {

    // =====================================
    //                FIELDS
    // =====================================

    // constants

    private static final String CSS_LOCATION = "/app/customControls/style/size-panel.css";

    private enum ResizeMode {
        NORMAL,
        INVERTED_BOTH,
        INVERTED_X,
        INVERTED_Y,
        BI_DIRECTIONAL,
        EQUILATERAL
    }

    // components

    private final Arrow topLeft;
    private final Arrow top;
    private final Arrow topRight;
    private final Arrow right;
    private final Arrow bottomRight;
    private final Arrow bottom;
    private final Arrow bottomLeft;
    private final Arrow left;
    private final Arrow[] arrows;
    private final Pane contentPane;
    private final Pane nodeContainer;

    // associated size panel

    private final ResizePanel resizePanel;

    // movement

    private Point2D lastMousePosition;
    private Orientation resizeDirection;
    private boolean inLimbo;
    private boolean onArrow;
    private boolean wasScaling;
    private double nodeAngle;

    // transforms

    private final Translate translate;
    private final Translate adjustTranslate;
    private final Translate resizeNodeTranslate;
    private final Scale oldScale;
    private final Scale scale;

    // listeners

    private final ChangeListener<Node> associatedNodeListener;
    private final InvalidationListener arrowSpaceListener;
    private final InvalidationListener colorChangeListener;
    private final InvalidationListener sizeListener;
    private final ChangeListener<Border> borderListener;
    private final InvalidationListener selectedListener;
    private final EventHandler<MouseEvent> nodePressListener;
    private final EventHandler<MouseEvent> mousePressListener;
    private final EventHandler<MouseEvent> resizeDirectionListener;
    private final EventHandler<MouseEvent> dragListener;
    private final EventHandler<MouseEvent> mouseReleaseListener;
    private final EventHandler<KeyEvent> keyPressListener;
    private final ChangeListener<Boolean> zoomListener;

    // =====================================
    //              CONSTRUCTOR
    // =====================================

    protected ResizePanelSkin(ResizePanel sizePanel) {
        super(sizePanel);

        // saves associated size panel

        this.resizePanel = sizePanel;

        // initialising components

        final double arrowWidth = sizePanel.getArrowLength();
        final double arrowHeight = sizePanel.getArrowThickness();
        final Color arrowColor = sizePanel.getArrowColor();

        this.topLeft = new Arrow(arrowWidth, arrowHeight, TOP_LEFT, arrowColor, Arrow.ArrowType.DOUBLE);
        this.top = new Arrow(arrowWidth, arrowHeight, Orientation.TOP, arrowColor, Arrow.ArrowType.DOUBLE);
        this.topRight = new Arrow(arrowWidth, arrowHeight, Orientation.TOP_RIGHT, arrowColor, Arrow.ArrowType.DOUBLE);
        this.right = new Arrow(arrowWidth, arrowHeight, Orientation.RIGHT, arrowColor, Arrow.ArrowType.DOUBLE);
        this.bottomRight = new Arrow(arrowWidth, arrowHeight, Orientation.BOTTOM_RIGHT, arrowColor, Arrow.ArrowType.DOUBLE);
        this.bottom = new Arrow(arrowWidth, arrowHeight, Orientation.BOTTOM, arrowColor, Arrow.ArrowType.DOUBLE);
        this.bottomLeft = new Arrow(arrowWidth, arrowHeight, Orientation.BOTTOM_LEFT, arrowColor, Arrow.ArrowType.DOUBLE);
        this.left = new Arrow(arrowWidth, arrowHeight, Orientation.LEFT, arrowColor, Arrow.ArrowType.DOUBLE);
        this.arrows = new Arrow[]{topLeft, top, topRight, right, bottomRight, bottom, bottomLeft, left};
        this.contentPane = new Pane();
        this.nodeContainer = new Pane();

        contentPane.setStyle(
                "-fx-border-width: 1;" +
                "-fx-border-color: red"
        );

        // initialising variables

        this.lastMousePosition = new Point2D(0, 0);
        this.resizeDirection = Orientation.NULL;
        this.translate = new Translate();
        this.adjustTranslate = new Translate();
        this.resizeNodeTranslate = new Translate();
        this.oldScale = new Scale();
        this.scale = resizePanel.getScale();
        this.wasScaling = false;
        resizePanel.getTransforms().addAll(translate, adjustTranslate);
        resizePanel.getResizeNode().getTransforms().add(scale);

        // initialising listeners

        this.associatedNodeListener = this::updateAssociatedNode;
        this.arrowSpaceListener = observable -> updateSize();
        this.colorChangeListener = this::updateArrowColor;
        this.sizeListener = observable -> updateSize();
        this.borderListener = this::handleBorderChange;
        this.selectedListener = this::handleSelection;
        this.nodePressListener = this::handleNodePress;
        this.mousePressListener = this::updateMousePosition;
        this.resizeDirectionListener = this::updateResizeDirection;
        this.dragListener = this::handleDrag;
        this.mouseReleaseListener = this::handleMouseRelease;
        this.keyPressListener = this::handleKeyPress;
        this.zoomListener = this::updateZoom;

        // initialisation
        style();
        populate();
        registerListeners();
        setArrowsVisible(false);
        Platform.runLater(this::updateSize);
    }

    // =====================================
    //            INITIALISATION
    // =====================================

    private void style() {

        // style classes

        resizePanel.getStyleClass().add("size-panel");
        nodeContainer.getStyleClass().add("resized-node");

        // css stylesheet

        try {
            String css = ResizePanel.class.getResource(CSS_LOCATION).toExternalForm();
            resizePanel.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.out.printf("Impossible to add stylesheet at location %s\n", CSS_LOCATION);
        }

    }

    private void populate() {

        // adds the associated node to the node container
        nodeContainer.getChildren().add(resizePanel.getResizeNode());

        // adds children to the content pane

        contentPane.getChildren().addAll(
                nodeContainer,
                topLeft,
                top,
                topRight,
                right,
                bottomRight,
                bottom,
                bottomLeft,
                left
        );

        // adds the content pane as a child of the resize panel

        getChildren().add(contentPane);
    }

    private void registerListeners() {

        // resize panel property listeners

        resizePanel.associatedNodeProperty().addListener(associatedNodeListener);
        resizePanel.arrowSpaceProperty().addListener(arrowSpaceListener);
        resizePanel.arrowColorProperty().addListener(colorChangeListener);
        resizePanel.getResizeNode().setOnMousePressed(nodePressListener);
        resizePanel.selectedProperty().addListener(selectedListener);

        // zooming

        resizePanel.zoomUpdateProperty().addListener(zoomListener);

        // node container property listeners

        nodeContainer.boundsInParentProperty().addListener(sizeListener);
        nodeContainer.borderProperty().addListener(borderListener);

        // mouse listeners

        resizePanel.setOnMousePressed(mousePressListener);
        resizePanel.setOnMouseDragged(dragListener);
        resizePanel.setOnMouseReleased(mouseReleaseListener);

        // key listeners

        resizePanel.setOnKeyPressed(keyPressListener);

        // arrow listeners

        topLeft    .setOnMousePressed(resizeDirectionListener);
        top        .setOnMousePressed(resizeDirectionListener);
        topRight   .setOnMousePressed(resizeDirectionListener);
        right      .setOnMousePressed(resizeDirectionListener);
        bottomRight.setOnMousePressed(resizeDirectionListener);
        bottom     .setOnMousePressed(resizeDirectionListener);
        bottomLeft .setOnMousePressed(resizeDirectionListener);
        left       .setOnMousePressed(resizeDirectionListener);
    }

    // =====================================
    //              UPDATES
    // =====================================

    private void updateAssociatedNode(ObservableValue<? extends Node> value, Node oldNode, Node newNode) {
        // adds new associated node to the content pane and removes the previous node
        nodeContainer.getChildren().remove(oldNode);
        nodeContainer.getChildren().add(newNode);

        // removes scaling transformation from old node and adds it to new node
        oldNode.getTransforms().removeAll(scale);
        newNode.getTransforms().addAll(scale);

        // adds the necessary listeners to the new node
        newNode.setOnMousePressed(nodePressListener);

        // resizes the resize panel to the node container's new size
        Platform.runLater(this::updateSize);
    }

    private void updateSize() {
        // resizes and repositions the nodes
        resize();
        reposition();
    }

    private void updateArrowColor(final Observable observable) {
        for (Arrow arrow : arrows) {
            arrow.setColor(resizePanel.getArrowColor());
        }
    }

    private void updateMousePosition(final MouseEvent mouseEvent) {
        // updates the previous mouse position to the mouse event's position
        updateMousePosition(new Point2D(mouseEvent.getX(), mouseEvent.getY()));
    }

    private void updateMousePosition(final Point2D newMousePosition) {
        // updates the mouse position to the specified coordinates
        lastMousePosition = newMousePosition;
    }

    private void updateResizeDirection(final MouseEvent mouseEvent) {
        // determines the origin of the mouse event
        final Node origin = (Node) mouseEvent.getSource();

        deselectArrow(resizeDirection);

        // determines the resize direction based on the origin of the event
        if (origin.equals(topLeft))          resizeDirection = TOP_LEFT;
        else if (origin.equals(top))         resizeDirection = Orientation.TOP;
        else if (origin.equals(topRight))    resizeDirection = Orientation.TOP_RIGHT;
        else if (origin.equals(right))       resizeDirection = Orientation.RIGHT;
        else if (origin.equals(bottomRight)) resizeDirection = Orientation.BOTTOM_RIGHT;
        else if (origin.equals(bottom))      resizeDirection = Orientation.BOTTOM;
        else if (origin.equals(bottomLeft))  resizeDirection = Orientation.BOTTOM_LEFT;
        else if (origin.equals(left))        resizeDirection = Orientation.LEFT;
        else                                 return;

        selectArrow(resizeDirection);

        // updates the state of the mouse to being on resize arrow
        onArrow = true;
    }

    private void selectArrow(final Orientation resizeDirection) {
        // selects the arrow corresponding to the current resize direction
        switch (resizeDirection) {
            case TOP_LEFT     -> topLeft.setSelected(true);
            case TOP          -> top.setSelected(true);
            case TOP_RIGHT    -> topRight.setSelected(true);
            case RIGHT        -> right.setSelected(true);
            case BOTTOM_RIGHT -> bottomRight.setSelected(true);
            case BOTTOM       -> bottom.setSelected(true);
            case BOTTOM_LEFT  -> bottomLeft.setSelected(true);
            case LEFT         -> left.setSelected(true);
        }
        // updates the cursor to match resize direction
        updateCursor(resizeDirection);
    }

    private void deselectArrow(final Orientation resizeDirection) {
        // deselects the arrow corresponding to the current resize direction
        switch (resizeDirection) {
            case TOP_LEFT     -> topLeft.setSelected(false);
            case TOP          -> top.setSelected(false);
            case TOP_RIGHT    -> topRight.setSelected(false);
            case RIGHT        -> right.setSelected(false);
            case BOTTOM_RIGHT -> bottomRight.setSelected(false);
            case BOTTOM       -> bottom.setSelected(false);
            case BOTTOM_LEFT  -> bottomLeft.setSelected(false);
            case LEFT         -> left.setSelected(false);
        }
    }

    private void handleMouseRelease(final MouseEvent mouseEvent) {
        // updates the state of the mouse to not being on a resize arrow
        onArrow = false;
        // deselects the current arrow
        deselectArrow(resizeDirection);
        // resets the cursor
        resetCursor();
        // node is no longer scaling
        wasScaling = false;
    }

    // =====================================
    //               CURSOR
    // ====================================

    private void updateCursor(final Orientation resizeDirection) {
        switch (resizeDirection) {
            case TOP_LEFT     -> resizePanel.setCursor(Cursor.NW_RESIZE);
            case TOP          -> resizePanel.setCursor(Cursor.N_RESIZE);
            case TOP_RIGHT    -> resizePanel.setCursor(Cursor.NE_RESIZE);
            case RIGHT        -> resizePanel.setCursor(Cursor.E_RESIZE);
            case BOTTOM_RIGHT -> resizePanel.setCursor(Cursor.SE_RESIZE);
            case BOTTOM       -> resizePanel.setCursor(Cursor.S_RESIZE);
            case BOTTOM_LEFT  -> resizePanel.setCursor(Cursor.SW_RESIZE);
            case LEFT         -> resizePanel.setCursor(Cursor.W_RESIZE);
            case HORIZONTAL   -> resizePanel.setCursor(Cursor.H_RESIZE);
            case VERTICAL     -> resizePanel.setCursor(Cursor.V_RESIZE);
            case NULL         -> resizePanel.setCursor(Cursor.DEFAULT);
        }
    }

    private void resetCursor() {
        resizePanel.setCursor(Cursor.DEFAULT);
    }

    // =====================================
    //             ACTIVATION
    // =====================================

    private void handleNodePress(final MouseEvent mouseEvent) {
        // updates the resize panel's selection property when it is pressed
        resizePanel.setSelected(!resizePanel.getSelected());
    }

    private void handleSelection(final Observable observable) {
        // update the arrow's visibility when resize panel's selection property changes
        setArrowsVisible(resizePanel.getSelected());
    }

     private void setArrowsVisible(final boolean isVisible) {
        // sets all arrow's visibility to the specified value
        for (Arrow arrow : arrows) {
            arrow.setVisible(isVisible);
        }
    }

    private void handleKeyPress(final KeyEvent keyEvent) {
        // deselects the resize panel if user presses ESC key
        if (KeyboardUtil.areKeysDown(keyEvent, ESC)) resizePanel.setSelected(false);
    }

    // =====================================
    //              RESIZING
    // =====================================

    private void handleDrag(final MouseEvent mouseEvent) {
        // if the mouse is not on an arrow aborts drag
        if (!onArrow) return;

        // gets the mouse position and determines displacement since last drag
        final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        final Point2D deltaMovement = mousePosition.subtract(lastMousePosition);
        // adapts displacement to current node scale
        final Point2D scaledMovement = new Point2D(deltaMovement.getX() / scale.getX(), deltaMovement.getY() / scale.getY());

        // determines if resize must be mirrored
        final boolean isMirrored = mouseEvent.isShiftDown();
        final boolean isScaled = mouseEvent.isControlDown();

        if (isScaled && !wasScaling) {
            nodeAngle = getNodeAngle();
            wasScaling = true;
        }

        if (!isScaled && wasScaling) {
            wasScaling = false;
        }

        // resize node
        resizeOnDrag(resizeDirection, scaledMovement, isMirrored, isScaled);
    }

    private void resize() {
        // TODO: update comments

        final Insets containerInsets = getBorderInsets();
        final double insetsWidth = containerInsets.getLeft() + containerInsets.getRight();
        final double insetsHeight = containerInsets.getTop() + containerInsets.getBottom();
        final double containerWidth = getNodeWidth() + insetsWidth;
        final double containerHeight = getNodeHeight() + insetsHeight;

        nodeContainer.setPrefWidth(containerWidth);
        nodeContainer.setPrefHeight(containerHeight);


        final double resizeWidth = containerWidth + left.getWidth() + right.getWidth() + resizePanel.getArrowSpace() * 2;
        final double resizeHeight = containerHeight + top.getWidth() + bottom.getWidth() + resizePanel.getArrowSpace() * 2;

        resizePanel.setPrefWidth(resizeWidth);
        resizePanel.setPrefHeight(resizeHeight);

        contentPane.setPrefWidth(resizeWidth);
        contentPane.setPrefHeight(resizeHeight);
    }

    private void resizeOnDrag(
            final Orientation direction,
            final Point2D amount,
            final boolean mirrored,
            final boolean scaled
    ) {
        // corrects the resize amount to take scale into consideration
        final Point2D correctedAmount = new Point2D(amount.getX(), amount.getY());

        // determines the difference in width and height and the resize mode based on resize direction
        final double deltaWidth = determineDeltaWidth(direction, correctedAmount);
        final double deltaHeight = determineDeltaHeight(direction, correctedAmount);
        final ResizeMode resizeMode = determineResizeMode(direction);

        // makes sure user cannot resize when outside an arrow
        final double rightArrowCenter = getContainerX() + resizePanel.getArrowSpace() + right.getLength() / 2;
        final double leftArrowCenter = getContainerX() - resizePanel.getArrowSpace() - left.getLength() / 2;
        final double topArrowCenter = getContainerY() - resizePanel.getArrowSpace() - top.getLength() / 2;
        final double bottomArrowCenter = getContainerY() + resizePanel.getArrowSpace() + bottom.getLength() / 2;
        // gets the mouse's position at this time in the event
        final Point2D mousePosition = resizePanel.screenToLocal(ScreenUtil.getMousePosition());
        final double mouseX = mousePosition.getX();
        final double mouseY = mousePosition.getY();
        // updates the mouse's position accordingly
        updateMousePosition(new Point2D(mouseX, mouseY));

        // checks that the user is in the range of the arrow used for resizing
        // range depends on the orientation and does not necessarily mean the user is ON the arrow.
        // for example, if resize orientation is LEFT, user only has to be within the correct x - coordinates
        if (direction.equals(Orientation.LEFT)  && inLimbo && deltaWidth > 0 && mouseX > leftArrowCenter ) return;
        if (direction.equals(Orientation.RIGHT) && inLimbo && deltaWidth > 0 && mouseX < rightArrowCenter) return;
        if (direction.equals(Orientation.TOP)   && inLimbo && deltaHeight > 0 && mouseY > topArrowCenter) return;

        // determines if selection direction must flip
        final boolean xFlip = getNodeWidth() + deltaWidth < 0;
        final boolean yFlip = getNodeHeight() + deltaHeight < 0;

        // changes the direction of the resize if a flip is necessary
        if (xFlip || yFlip) {

            // marks the mouse as being at the center of the size panel, between any arrows
            inLimbo = true;

            // determines if the mouse has reached the correct coordinates to switch the resize orientation
            final boolean canResizeRight = mouseX >= rightArrowCenter;
            final boolean canResizeLeft = mouseX <= leftArrowCenter;
            final boolean canResizeTop = mouseY <= topArrowCenter;
            final boolean canResizeBottom = mouseY >= bottomArrowCenter;

            // stops flip if mouse has not reached the correct coordinates
            if      (xFlip && correctedAmount.getX() < 0 && !canResizeLeft  ) return;
            else if (xFlip && correctedAmount.getX() > 0 && !canResizeRight ) return;
            if      (yFlip && correctedAmount.getY() < 0 && !canResizeTop   ) return;
            else if (yFlip && correctedAmount.getY() > 0 && !canResizeBottom) return;

            // deselects the current arrow
            deselectArrow(resizeDirection);

            // updates the resize direction
            resizeDirection = invertDirection(direction, deltaWidth, deltaHeight);

            // updates the selected arrow
            selectArrow(resizeDirection);

            // resizes the node with the new direction
            resizeOnDrag(resizeDirection, correctedAmount, mirrored, scaled);
            // marks the node as having exited limbo
            inLimbo = false;
            // exits method to abort resizing under current direction
            return;
        }

        final Bounds bounds = resizePanel.getBoundsInParent();
        // System.out.printf("resize direction       :%s\n", resizeDirection);
        // System.out.printf("relative mouse position:%s\n", getRelativeMouseCoordinates(resizeDirection, resizeMode));
        // System.out.printf("relative mouse angle:%s\n", getRelativeMouseAngle(resizeDirection, resizeMode));
        // System.out.println("==============");

        if (scaled) {
            // System.out.printf("relative mouse position:%s\n", getRelativeMouseCoordinates(resizeDirection, resizeMode));
            // System.out.printf("relative mouse angle:%s\n", getRelativeMouseAngle(resizeDirection, resizeMode));
            scaleNode(resizeDirection, resizeMode);
        } else {
            // resizes the associated node
            resizeNode(deltaWidth, deltaHeight, resizeMode, mirrored);
        }
    }

    private double determineDeltaWidth(final Orientation direction, final Point2D amount) {
        // determines the change in width based on the resize direction
        return switch (direction) {
            case TOP_LEFT, LEFT, BOTTOM_LEFT    -> -amount.getX();
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> amount.getX();
            default                             -> 0;
        };
    }

    private double determineDeltaHeight(final Orientation direction, final Point2D amount) {
        // determines the change in height based on the resize direction
        return switch (direction) {
            case TOP_LEFT, TOP, TOP_RIGHT          -> -amount.getY();
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> amount.getY();
            default                                -> 0;
        };
    }

    private ResizeMode determineResizeMode(final Orientation direction) {
        // determines the resize mode based on the resize direction
        return switch (direction) {
            case TOP_LEFT          -> ResizeMode.INVERTED_BOTH;
            case TOP, TOP_RIGHT    -> ResizeMode.INVERTED_Y;
            case LEFT, BOTTOM_LEFT -> ResizeMode.INVERTED_X;
            default                -> ResizeMode.NORMAL;
        };
    }

    private Orientation invertDirection(final Orientation direction, final double deltaWidth, final double deltaHeight) {
        // inverts the resize direction based on the change in width and height
        return switch (direction) {
            case TOP_LEFT     -> {
                if (getNodeWidth() + deltaWidth < 0 && getNodeHeight() + deltaHeight < 0) yield Orientation.BOTTOM_RIGHT;
                if (getNodeWidth() + deltaWidth < 0)                                      yield Orientation.TOP_RIGHT;
                else                                                                      yield Orientation.BOTTOM_LEFT;
            }
            case TOP          -> Orientation.BOTTOM;
            case TOP_RIGHT    -> {
                if (getNodeWidth() + deltaWidth < 0 && getNodeHeight() + deltaHeight < 0) yield Orientation.BOTTOM_LEFT;
                if (getNodeWidth() + deltaWidth < 0)                                      yield TOP_LEFT;
                else                                                                      yield Orientation.BOTTOM_RIGHT;
            }
            case RIGHT        -> Orientation.LEFT;
            case BOTTOM_RIGHT -> {
                if (getNodeWidth() + deltaWidth < 0 && getNodeHeight() + deltaHeight < 0) yield Orientation.TOP_RIGHT;
                if (getNodeWidth() + deltaWidth < 0)                                      yield Orientation.BOTTOM_LEFT;
                else                                                                      yield Orientation.TOP_RIGHT;
            }
            case BOTTOM       -> Orientation.TOP;
            case BOTTOM_LEFT  -> {
                if (getNodeWidth() + deltaWidth < 0 && getNodeHeight() + deltaHeight < 0) yield Orientation.TOP_RIGHT;
                if (getNodeWidth() + deltaWidth < 0)                                      yield Orientation.BOTTOM_RIGHT;
                else                                                                      yield TOP_LEFT;
            }
            case LEFT         -> Orientation.RIGHT;
            default           -> throw new IllegalStateException("Unexpected value: " + direction);
        };
    }

    private void resizeNode(double deltaWidth, double deltaHeight, ResizeMode resizeMode, final boolean mirrored) {
        // aborts resizing if change in width and height would lead to negative size
        if (!validateSize(deltaWidth, deltaHeight)) return;

        // determines if resizing is inverted along the x-axis or y-axis (if node is being resized to top or left)
        final boolean invertedW = resizeMode.equals(ResizeMode.INVERTED_X) || resizeMode.equals(ResizeMode.INVERTED_BOTH);
        final boolean invertedH = resizeMode.equals(ResizeMode.INVERTED_Y) || resizeMode.equals(ResizeMode.INVERTED_BOTH);

        // determines the node's new size
        final Node associatedNode = resizePanel.getResizeNode();
        final double newWidth = getLocalNodeWidth() + deltaWidth * (mirrored ? 2 : 1);
        final double newHeight = getLocalNodeHeight() + deltaHeight * (mirrored ? 2 : 1);

        // translates the node to change resize direction, taking scale into consideration
        translate.setX(translate.getX() + (invertedW ? -deltaWidth * scale.getX(): -deltaWidth * (mirrored ? 1 : 0)));
        translate.setY(translate.getY() + (invertedH ? -deltaHeight * scale.getY(): -deltaHeight * (mirrored ? 1 : 0)));

        // updates the mouse's position to account for translation
        lastMousePosition = resizePanel.screenToLocal(ScreenUtil.getMousePosition());

        // resizes the node based on its class
        setNodeSize(newWidth, newHeight);
    }

    private void scaleNode(final Orientation resizeDirection, final ResizeMode resizeMode) {

        final Rectangle2D scaledNodeSize = getScaledNodeSize(resizeDirection, resizeMode);
        final double scaledWidth = scaledNodeSize.getWidth();
        final double scaledHeight = scaledNodeSize.getHeight();

        System.out.printf("scaled node width :%s\n", scaledWidth);
        System.out.printf("scaled node height:%s\n", scaledHeight);
        System.out.println("================");

        setNodeSize(scaledWidth, scaledHeight);
    }

    final void setNodeSize(final double width, final double height) {
        final Node resizeNode = resizePanel.getResizeNode();

        if (resizeNode instanceof Region) {
            ((Region) resizeNode).setPrefWidth(Math.abs(width));
            ((Region) resizeNode).setPrefHeight(Math.abs(height));
        } else if (resizeNode instanceof Rectangle) {
            ((Rectangle) resizeNode).setWidth(Math.abs(width));
            ((Rectangle) resizeNode).setHeight(Math.abs(height));
        } else if (resizeNode instanceof Circle) {
            ((Circle) resizeNode).setRadius(Math.abs(width / 2));
        }
    }

    final Rectangle2D getScaledNodeSize(final Orientation resizeDirection, final ResizeMode resizeMode) {

        final Point2D relativeMouseCoordinates = getRelativeMouseCoordinates(resizeDirection, resizeMode);
        final double mouseAngle = getRelativeMouseAngle(relativeMouseCoordinates);

        System.out.printf("node angle:%s\n", Math.toDegrees(nodeAngle));
        System.out.printf("mouse angle:%s\n", Math.toDegrees(mouseAngle));

        final double scaledWidth;
        final double scaledHeight;

        if (mouseAngle > Math.PI / 2 - nodeAngle) {
            scaledHeight = relativeMouseCoordinates.getY();
            scaledWidth = scaledHeight / Math.tan(nodeAngle);
        } else if (mouseAngle < Math.PI / 2 - nodeAngle) {
            scaledWidth = relativeMouseCoordinates.getX();
            scaledHeight = scaledWidth * Math.tan(nodeAngle);
        } else {
            scaledWidth = relativeMouseCoordinates.getX();
            scaledHeight = relativeMouseCoordinates.getY();
        }

        return new Rectangle2D(0, 0, scaledWidth, scaledHeight);
    }

    /**
     * Calculates the mouse's coordinates relative to the corner opposite to the resize direction
     * (ex: if resize direction is bottom-right, gets mouse coordinates relative to top-right corner)
     * @param resizeDirection ({@link Orientation}): the direction in which the resize is occurring
     * @param resizeMode ({@link ResizeMode}): if resize is inverted along x-axis or y-axis
     * @return (Point2D): the mouse's position relative to the corner opposite to the resize direction
     */
    private Point2D getRelativeMouseCoordinates(final Orientation resizeDirection, final ResizeMode resizeMode) {

        // gets the mouse position relative to the screen
        final Point2D mouseScreenPosition = ScreenUtil.getMousePosition();

        // gets the resize node's bounds
        final Bounds nodeBounds = resizePanel.getResizeNode().getBoundsInParent();
        // determines the coordinates of the corner opposite the resize direction
        final Point2D nodeScreenPosition = switch (resizeDirection) {
            case TOP_LEFT     -> nodeContainer.localToScreen(nodeBounds.getMaxX(), nodeBounds.getMaxY());
            case TOP_RIGHT    -> nodeContainer.localToScreen(nodeBounds.getMinX(), nodeBounds.getMaxY());
            case BOTTOM_RIGHT -> nodeContainer.localToScreen(nodeBounds.getMinX(), nodeBounds.getMinY());
            case BOTTOM_LEFT  -> nodeContainer.localToScreen(nodeBounds.getMaxX(), nodeBounds.getMinY());
            default           -> new Point2D(0, 0);
        };

        // determines the adjustments to the mouse's position
        final double adjustX = resizePanel.getArrowSpace();
        final double adjustY = resizePanel.getArrowSpace();
        // determines if the resizing is inverted horizontally or vertically
        final boolean invertedH = resizeMode.equals(ResizeMode.INVERTED_X) || resizeMode.equals(ResizeMode.INVERTED_BOTH);
        final boolean invertedV = resizeMode.equals(ResizeMode.INVERTED_Y) || resizeMode.equals(ResizeMode.INVERTED_BOTH);
        // calculates the mouse's position relative to the corner opposite to the resize direction
        final double relativeX = mouseScreenPosition.getX() - nodeScreenPosition.getX() + (invertedH ? adjustX : -adjustX);
        final double relativeY = mouseScreenPosition.getY() - nodeScreenPosition.getY() + (invertedV ? adjustY : -adjustY);

        // returns the mouse's relative position
        return new Point2D(
                Math.max(0, invertedH ? -relativeX : relativeX),
                Math.max(0, invertedV ? -relativeY : relativeY)
        );
    }

    private double getRelativeMouseAngle(final Point2D mousePosition) {

        final double width = mousePosition.getX();
        final double height = mousePosition.getY();
        final double mouseAngle = Math.atan(width / height);

        return Double.isNaN(mouseAngle) ? 0 : mouseAngle;
    }

    private double getNodeAngle() {

        final double nodeWidth = getNodeWidth();
        final double nodeHeight = getNodeHeight();
        final double nodeAngle = Math.atan(nodeHeight / nodeWidth);

        return Double.isNaN(nodeAngle) ? 0 : nodeAngle;
    }

    // =====================================
    //              SCALING
    // =====================================

    private void updateZoom(ObservableValue<? extends Boolean> value, Boolean oldBoolean, Boolean newBoolean) {
        // zoom value updating has started
        if (newBoolean) {
            // updates the old scale to save current zoom state
            oldScale.setX(scale.getX());
            oldScale.setY(scale.getY());
            oldScale.setPivotX(scale.getPivotX());
            oldScale.setPivotY(scale.getPivotY());

            // removes scale from node transforms so that any changes to it will node affect the node
            resizePanel.getResizeNode().getTransforms().remove(scale);
        }
        // zoom value updating has finished (can apply zoom)
        else {
            // calculates the zoom
            zoom(scale);
            // applies the zoom
            resizePanel.getResizeNode().getTransforms().add(scale);
        }
    }

    private void zoom(final Scale newScale) {

        System.out.printf("old scale:%s\n", oldScale);
        System.out.printf("new scale:%s\n", newScale);

        // resets the resize node's layoutX and layoutY values, so they do not interfere with Transforms
        final Node resizeNode = resizePanel.getResizeNode();
        resizeNode.setLayoutX(0);
        resizeNode.setLayoutY(0);

        // determines node displacement
        final Point2D lastAdjustment = new Point2D(adjustTranslate.getX(), adjustTranslate.getY());
        // determines difference between current node position with displacement and node position after scaling
        final Point2D deltaPosition = newScale.transform(getNodePosition()).subtract(lastAdjustment);

        // translates the resize panel so that node container matches resize node position (moves resize node as well)
        adjustTranslate.setY(adjustTranslate.getY() + deltaPosition.getY());
        adjustTranslate.setX(adjustTranslate.getX() + deltaPosition.getX());
        // translates resize node back to its original position
        resizeNodeTranslate.setX(resizeNodeTranslate.getX() - deltaPosition.getX());
        resizeNodeTranslate.setY(resizeNodeTranslate.getY() - deltaPosition.getY());

        // concatenates new scale with translation (because JavaFx createConcatenation does not woooork hahAHHahaaHhaha)
        final Scale concatenatedScale = TransformUtil.scaleAndTranslate(newScale, resizeNodeTranslate);
        // updates node scale to match concatenation
        scale.setX(concatenatedScale.getX());
        scale.setY(concatenatedScale.getY());
        scale.setPivotX(concatenatedScale.getPivotX());
        scale.setPivotY(concatenatedScale.getPivotY());

        // updates resize node layoutX and layoutY to account for border
        final Insets containerInsets = getBorderInsets();
        resizeNode.setLayoutX(resizeNode.getLayoutX() -getResizeNodeX() + containerInsets.getLeft());
        resizeNode.setLayoutY(resizeNode.getLayoutY() -getResizeNodeY() + containerInsets.getTop());
    }

    // =====================================
    //             REPOSITION
    // =====================================

    private void reposition() {
        // determines the size of top & left arrow
        final double deltaX = left.getDisplayWidth();
        final double deltaY = top.getDisplayHeight();
        // determines the size of the node
        final double nodeWidth = getContainerWidth();
        final double nodeHeight = getContainerHeight();
        // gets the amount of blank space between the node and arrows
        final double arrowSpace = resizePanel.getArrowSpace();

        // repositions the node
        nodeContainer.setLayoutX(deltaX + arrowSpace);
        nodeContainer.setLayoutY(deltaY + arrowSpace);

        // repositions the arrows
        topLeft.moveTo(deltaX                                 , deltaY);
        top.moveTo(deltaX + nodeWidth / 2 + arrowSpace        , deltaY);
        topRight.moveTo(deltaX + nodeWidth + arrowSpace * 2   , deltaY);
        right.moveTo(deltaX + nodeWidth + arrowSpace * 2      , deltaY + nodeHeight / 2 + arrowSpace);
        bottomRight.moveTo(deltaX + nodeWidth + arrowSpace * 2, deltaY + nodeHeight + arrowSpace * 2);
        bottom.moveTo(deltaX + nodeWidth / 2 +  arrowSpace    , deltaY + nodeHeight + arrowSpace * 2);
        bottomLeft.moveTo(deltaX                              , deltaY + nodeHeight + arrowSpace * 2);
        left.moveTo(deltaX                                    , deltaY + nodeHeight / 2 + arrowSpace);
    }

    // =====================================
    //               BORDER
    // =====================================

    private void handleBorderChange(ObservableValue<? extends Border> value, Border oldBorder, Border newBorder) {

        // gets the previous & current border size
        final Insets previousInsets = oldBorder == null ? new Insets(0) : oldBorder.getInsets();
        final Insets newInsets = newBorder == null ? new Insets(0) : newBorder.getInsets();
        // determines the previous width & height of the border
        final double oldWidth = previousInsets.getLeft() + previousInsets.getRight();
        final double oldHeight = previousInsets.getTop() + previousInsets.getBottom();
        // determines the new width & height of the border
        final double newWidth = newInsets.getLeft() + previousInsets.getRight();
        final double newHeight = newInsets.getTop() + newInsets.getBottom();
        // determines the difference in width & height between the old and new borders
        final double deltaWidth = oldWidth - newWidth;
        final double deltaHeight = oldHeight - newHeight;

        // updates the translation to center the resize panel once it has been resized
        translate.setX(translate.getX() + deltaWidth / 2);
        translate.setY(translate.getY() + deltaHeight / 2);

        // TODO: add comments here
        final Node resizeNode = resizePanel.getResizeNode();
        resizeNode.setLayoutX(resizeNode.getLayoutX() + newInsets.getLeft() - previousInsets.getLeft());
        resizeNode.setLayoutY(resizeNode.getLayoutY() + newInsets.getTop() - previousInsets.getBottom());

        updateSize();
    }

    private Border getBorder() {
        return nodeContainer.getBorder();
    }

    private Insets getBorderInsets() {
        final Border containerBorder = getBorder();
        return containerBorder == null ? new Insets(0) : containerBorder.getInsets();
    }

    // =====================================
    //               0GETTERS
    // =====================================

    private double getNodeWidth() {
        return resizePanel.getResizeNode().getBoundsInParent().getWidth();
    }

    private double getNodeHeight() {
        return resizePanel.getResizeNode().getBoundsInParent().getHeight();
    }

    private Point2D getNodePosition() {
        final Bounds bounds = resizePanel.getResizeNode().getBoundsInParent();
        return new Point2D(bounds.getMinX(), bounds.getMinY());
    }

    private double getResizeNodeX() {
        return getNodePosition().getX();
    }

    private double getResizeNodeY() {
        return getNodePosition().getY();
    }

    private double getLocalNodeWidth() {
        return resizePanel.getResizeNode().getBoundsInLocal().getWidth();
    }

    private double getLocalNodeHeight() {
        return resizePanel.getResizeNode().getBoundsInLocal().getHeight();
    }

    private double getContainerWidth() {
        return nodeContainer.getBoundsInParent().getWidth();
    }

    private double getContainerHeight() {
        return nodeContainer.getBoundsInParent().getHeight();
    }

    private Rectangle2D getContainerSize() {
        return new Rectangle2D(0, 0, getContainerWidth(), getContainerHeight());
    }

    private Point2D getContainerPosition() {
        final Bounds bounds = nodeContainer.getBoundsInParent();
        return new Point2D(bounds.getMinX(), bounds.getMinY());
    }

    private double getContainerX() {
        return getContainerPosition().getX();
    }

    private double getContainerY() {
        return getContainerPosition().getY();
    }

    // =====================================
    //             VALIDATION
    // =====================================

    private boolean validateSize(final double deltaWidth, final double deltaHeight) {
        return getContainerWidth() + deltaWidth >= 0 && getContainerHeight() + deltaHeight >= 0;
    }

}
