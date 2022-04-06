package app.customControls.controls.resizePanel;

import app.customControls.controls.shapes.Arrow;
import app.customControls.controls.shapes.Orientation;
import app.customControls.handlers.delay.DelayHandler;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import static app.customControls.controls.shapes.Orientation.*;

public class ResizePanelSkin extends SkinBase<ResizePanel> implements Skin<ResizePanel> {

    // =====================================
    //                FIELDS
    // =====================================

    // constants

    private static final String CSS_LOCATION = "/app/customControls/style/size-panel.css";
    private static final int MOVEMENT_DELAY = 10;

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

    private Orientation resizeDirection;
    private boolean onArrow;
    private boolean wasScaling;
    private double nodeAngle;
    private final DelayHandler movementDelay;

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
    private final EventHandler<MouseEvent> resizeDirectionListener;
    private final EventHandler<MouseEvent> dragListener;
    private final EventHandler<MouseEvent> mouseReleaseListener;
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

        this.movementDelay = new DelayHandler(MOVEMENT_DELAY);
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
        this.resizeDirectionListener = this::updateResizeDirection;
        this.dragListener = this::handleDrag;
        this.mouseReleaseListener = this::handleMouseRelease;
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

        contentPane.setOnMouseDragged(dragListener);
        contentPane.setOnMouseReleased(mouseReleaseListener);

        // key listeners

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
    //             SELECTION
    // =====================================

    private void handleNodePress(final MouseEvent mouseEvent) {
        // updates the resize panel's selection property when it is pressed
        resizePanel.setSelected(true);
    }

    private void handleSelection(final Observable observable) {
        // update the arrow's visibility when resize panel's selection property changes
        setArrowsVisible(resizePanel.isSelected());
    }

     private void setArrowsVisible(final boolean isVisible) {
        // sets all arrow's visibility to the specified value
        for (Arrow arrow : arrows) {
            arrow.setVisible(isVisible);
        }
    }

    // =====================================
    //              RESIZING
    // =====================================

    private void handleDrag(final MouseEvent mouseEvent) {
        // if the mouse is not on an arrow aborts drag
        if (!onArrow) return;

        // small delay to make sure that transformations have the time to be applied before the next mouseEvent is fired
        // (results in incorrect resize arrow position otherwise, fuck JFX)
        if (!movementDelay.hasElapsed()) return;

        // gets the mouse position
        // final Point2D mousePosition = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        final Point2D mousePosition = ScreenUtil.getMousePosition();
        // determines the path from the resize arrow to the mouse
        final Point2D deltaMovement = mousePosition.subtract(getResizeArrowPosition(resizeDirection));
        // adapts displacement to current node scale
        final Point2D scaledMovement = new Point2D(deltaMovement.getX() / scale.getX(), deltaMovement.getY() / scale.getY());

        // determines if resize must be mirrored
        final boolean isMirrored = mouseEvent.isShiftDown();
        final boolean isScaled = mouseEvent.isControlDown();

        // gets the node angle whenever the node starts scaling
        if (isScaled && !wasScaling) {
            nodeAngle = getNodeAngle();
            wasScaling = true;
        }

        // stops scaling the node when CTRl key is no longer pressed
        if (!isScaled && wasScaling) {
            wasScaling = false;
        }

        // resize node
        resizeOnDrag(resizeDirection, scaledMovement, isMirrored, isScaled);
    }

    private void setResizeDirection(final Orientation newResizeDirection) {
        // deselects the current arrow
        deselectArrow(resizeDirection);
        // updates the resize direction
        resizeDirection = newResizeDirection;
        // selects the new arrow corresponding to the resize direction
        selectArrow(newResizeDirection);
    }

    private void resize() {
        // gets the size of the border in the node container
        final Insets containerInsets = getBorderInsets();
        final double insetsWidth = containerInsets.getLeft() + containerInsets.getRight();
        final double insetsHeight = containerInsets.getTop() + containerInsets.getBottom();
        // gets the size of the node container
        final double containerWidth = getNodeWidth() + insetsWidth;
        final double containerHeight = getNodeHeight() + insetsHeight;

        // sets the node container's size
        nodeContainer.setPrefWidth(containerWidth);
        nodeContainer.setPrefHeight(containerHeight);

        // determines the resize panel's size
        final double resizeWidth = containerWidth + left.getWidth() + right.getWidth() + resizePanel.getArrowSpace() * 2;
        final double resizeHeight = containerHeight + top.getWidth() + bottom.getWidth() + resizePanel.getArrowSpace() * 2;

        // sets the resize panel's size
        resizePanel.setPrefWidth(resizeWidth);
        resizePanel.setPrefHeight(resizeHeight);

        // sets the node container's size to match the resize panel
        contentPane.setPrefWidth(resizeWidth);
        contentPane.setPrefHeight(resizeHeight);
    }

    private void resizeOnDrag(
            final Orientation direction,
            final Point2D amount,
            final boolean mirrored,
            final boolean scaled
    ) {
        final boolean isDiagonalDrag = direction.equals(TOP_LEFT)
                || direction.equals(TOP_RIGHT)
                || direction.equals(BOTTOM_RIGHT)
                || direction.equals(BOTTOM_LEFT);

        // aborts resizing if resize direction is invalid for scaling
        // (LEFT, RIGHT, TOP or BOTTOM don't work with scaling)
        if (scaled && !isDiagonalDrag) return;

        // determines the difference in width and height and the resize mode based on resize direction
        final double deltaWidth = determineDeltaWidth(direction, amount);
        final double deltaHeight = determineDeltaHeight(direction, amount);
        final ResizeMode resizeMode = determineResizeMode(direction);

        if (scaled) {
            // scales the associated node
            scaleNode(resizeDirection, resizeMode, mirrored);
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

    private Orientation invertScaleDirection(
            final Orientation resizeDirection,
            final double nodeAngle,
            final double mouseAngle
    ) {
        return switch (resizeDirection) {
            case TOP_LEFT -> {
                if (mouseAngle > nodeAngle) yield BOTTOM_LEFT;
                if (mouseAngle < nodeAngle) yield TOP_RIGHT;
                else yield TOP_LEFT;
            }
            case TOP_RIGHT -> {
                if (mouseAngle > nodeAngle) yield BOTTOM_RIGHT;
                if (mouseAngle < nodeAngle) yield TOP_LEFT;
                else yield TOP_RIGHT;
            }
            case BOTTOM_RIGHT -> {
                if (mouseAngle > nodeAngle) yield TOP_RIGHT;
                if (mouseAngle < nodeAngle) yield BOTTOM_LEFT;
                else yield BOTTOM_RIGHT;
            }
            case BOTTOM_LEFT -> {
                if (mouseAngle > nodeAngle) yield TOP_LEFT;
                if (mouseAngle < nodeAngle) yield BOTTOM_RIGHT;
                else yield BOTTOM_LEFT;
            }
            default -> throw new IllegalArgumentException(
                    resizeDirection + "is not a valid scale direction, only diagonal directions are accepted");
        };
    }

    private void resizeNode(
            final double deltaWidth,
            final double deltaHeight,
            final ResizeMode resizeMode,
            final boolean mirrored
    ) {
        // checks that a resize is possible
        if (!canResize(deltaWidth, deltaHeight)) return;

        // adjusts delta width & delta height to take into account resize mirroring
        final double adjustWidth = deltaWidth * (mirrored ? 2 : 1);
        final double adjustHeight = deltaHeight * (mirrored ? 2 : 1);

        if (checkForResizeAxisFlip(adjustWidth, adjustHeight, resizeMode, mirrored)) return;

        // determines the node's new size
        final double newWidth = getUnscaledNodeWidth() + adjustWidth;
        final double newHeight = getUnscaledNodeHeight() + adjustHeight;

        // translates the node to change resize direction, taking scale into consideration
        counterMovement(deltaWidth, deltaHeight, resizeMode, mirrored);

        // resizes the node based on its class
        setNodeSize(newWidth, newHeight);
    }

    private boolean canResize(final double deltaWidth, final double deltaHeight) {
        // determines the center of each horizontal and vertical arrow
        final double rightArrowCenter = getContainerX() + resizePanel.getArrowSpace() + right.getLength() / 2 + getContainerWidth();
        final double leftArrowCenter = getContainerX() - resizePanel.getArrowSpace() - left.getLength() / 2;
        final double topArrowCenter = getContainerY() - resizePanel.getArrowSpace() - top.getLength() / 2;
        final double bottomArrowCenter = getContainerY() + resizePanel.getArrowSpace() + bottom.getLength() / 2 + getContainerHeight();

        // gets the mouse's position relative to the resize panel
        final Point2D mousePosition = resizePanel.screenToLocal(ScreenUtil.getMousePosition());
        final double mouseX = mousePosition.getX();
        final double mouseY = mousePosition.getY();

        // determines if selection direction must flip
        final boolean xFlip = getNodeWidth() + deltaWidth < 0;
        final boolean yFlip = getNodeHeight() + deltaHeight < 0;

        // determines if the mouse has reached the correct coordinates to switch the resize orientation
        final boolean canResizeRight = mouseX >= rightArrowCenter;
        final boolean canResizeLeft = mouseX <= leftArrowCenter;
        final boolean canResizeTop = mouseY <= topArrowCenter;
        final boolean canResizeBottom = mouseY >= bottomArrowCenter;

        // stops flip if mouse has not reached the correct coordinates
        if      (xFlip && resizeDirection.equals(RIGHT)   && !canResizeLeft  ) return false;
        else if (xFlip && resizeDirection.equals(LEFT)    && !canResizeRight ) return false;
        if      (yFlip && resizeDirection.equals(BOTTOM)  && !canResizeTop   ) return false;
        else if (yFlip && resizeDirection.equals(TOP)     && !canResizeBottom) return false;

        return true;
    }

    private boolean isResizeFlippingHorizontal(final double deltaWidth) {
        // gets the node's current width
        final double nodeWidth = getUnscaledNodeWidth();
        // determines the node's width after the resize
        final double widthResult = nodeWidth + deltaWidth;

        // returns whether a flip is necessary
        return widthResult < 0;
    }

    private boolean checkForResizeAxisFlip(
            final double deltaWidth,
            final double deltaHeight,
            final ResizeMode resizeMode,
            final boolean mirrored
    ) {
        // determines if a horizontal or vertical flip should take place
        final boolean horizontalFlip = isResizeFlippingHorizontal(deltaWidth);
        final boolean verticalFlip = isResizeFlippingVertical(deltaHeight);

        // if resize leads to any axis flip...
        if (horizontalFlip || verticalFlip) {

            // ...flips the corresponding axis
            handleResizeAxisFlip(deltaWidth, deltaHeight, horizontalFlip, verticalFlip, resizeMode, mirrored);

            // axis flip had occurred
            return true;
        }

        // axis flip has not occurred
        return false;
    }

    private void handleResizeAxisFlip(
            final double deltaWidth,
            final double deltaHeight,
            final boolean horizontalFlip,
            final boolean verticalFlip,
            final ResizeMode resizeMode,
            final boolean mirrored
    ) {
        // gets the size of the resize node
        final double width = getUnscaledNodeWidth();
        final double height = getUnscaledNodeHeight();

        // determines the amount of translation needed to counter axis flip movement
        final double counterWidth = (horizontalFlip ? -width: 0) * (mirrored ? .5 : 1);
        final double counterHeight = (verticalFlip ? -height : 0) * (mirrored ? .5 : 1);

        // sets the node's with and / or height to 0 depending on which axis is being flipped
        counterMovement(counterWidth, counterHeight, resizeMode, mirrored);
        setNodeSize(horizontalFlip ? 0 : width, verticalFlip ? 0 : height);

        // inverts the resize direction to account for the axis flip
        setResizeDirection(invertDirection(resizeDirection, deltaWidth, deltaHeight));
    }

    private boolean isResizeFlippingVertical(final double deltaHeight) {
        // gets the node's current height
        final double nodeHeight = getUnscaledNodeHeight();
        // determines the height of the node after resize
        final double heightResult = nodeHeight + deltaHeight;

        // returns whether a flip is necessary
        return heightResult < 0;
    }

    private void scaleNode(final Orientation resizeDirection, final ResizeMode resizeMode, final boolean mirrored) {
        // gets the mouse's position relative to the corner opposite to the resize direction
        final Point2D relativeMouseCoordinates = getRelativeMouseCoordinates(resizeDirection, resizeMode);
        // determines the mouse's angle relative to the corner opposite to the resize direction
        final double mouseAngle = getRelativeMouseAngle(relativeMouseCoordinates);

        // checks if it is possible to scale node
        if (!canScale(relativeMouseCoordinates)) return;

        // determines the size of the node after scaling
        final Rectangle2D scaledNodeSize = getScaledNodeSize(relativeMouseCoordinates, mouseAngle);

        // checks for an axis flip
        if (isScaleFlipping(relativeMouseCoordinates)) {

            // flips the node's axes
            handleScaleAxisFlip(mouseAngle, resizeMode, mirrored);

            // exits the method, next drag event will call scale again which will set the node to the correct size
            return;
        }

        // gets the current node size
        final Rectangle2D nodeSize = getUnscaledNodeSize();

        // determines the difference in size between the scaled node and the current node
        final double deltaWidth = (scaledNodeSize.getWidth() - nodeSize.getWidth());
        final double deltaHeight = (scaledNodeSize.getHeight() - nodeSize.getHeight());

        // determines the node's final size in the case where the scaling is mirrored
        final double finalWidth = nodeSize.getWidth() + deltaWidth * (mirrored ? 2 : 1);
        final double finalHeight = nodeSize.getHeight() + deltaHeight * (mirrored ? 2 : 1);

        // if the node's new size would be negative...
        if (finalWidth < 0 || finalHeight < 0) {
            // ...flips the node's axes
            handleScaleAxisFlip(mouseAngle, resizeMode, mirrored);
            // ...and aborts scaling
            return;
        }

        // counters the node's movement
        counterMovement(deltaWidth, deltaHeight, resizeMode, mirrored);

        // updates the node's size
        setNodeSize(finalWidth, finalHeight);
    }

    private boolean canScale(final Point2D relativeMouseCoordinates) {
        // determines the center of each horizontal and vertical arrow
        final double leftArrowCenter = left.getLength() / 2;
        final double rightArrowCenter = (getContainerX() + getContainerWidth() + resizePanel.getArrowSpace() + right.getLength() / 2) / scale.getX();
        final double topArrowCenter = top.getLength() / 2;
        final double bottomArrowCenter = (getContainerY() + getContainerHeight() + resizePanel.getArrowSpace() + bottom.getLength() / 2) / scale.getY();

        // gets the mouse's position relative to the resize panel
        final Point2D mousePosition = resizePanel.screenToLocal(ScreenUtil.getMousePosition());
        final double mouseX = mousePosition.getX();
        final double mouseY = mousePosition.getY();

        // whether the mouse is in limbo along the x-axis or y-axis
        // (limbo is the term given to the space between arrow centers when the resize node's size is 0 along that axis)
        final boolean inHorizontalLimbo;
        final boolean inVerticalLimbo;

        // determines whether the mouse is in limbo along the x-axis
        if (resizeDirection.equals(BOTTOM_LEFT) || resizeDirection.equals(TOP_LEFT))
            inHorizontalLimbo = relativeMouseCoordinates.getX() < 0 && mouseX < rightArrowCenter;
        else if (resizeDirection.equals(BOTTOM_RIGHT) || resizeDirection.equals(TOP_RIGHT))
            inHorizontalLimbo = relativeMouseCoordinates.getX() < 0 && mouseX > leftArrowCenter;
        else
            inHorizontalLimbo = false;
        // determines whether the mouse is in limbo along the y-axis
        if (resizeDirection.equals(TOP_RIGHT) || resizeDirection.equals(TOP_LEFT))
            inVerticalLimbo = relativeMouseCoordinates.getY() < 0 && mouseY < bottomArrowCenter;
        else if (resizeDirection.equals(BOTTOM_RIGHT) || resizeDirection.equals(BOTTOM_LEFT))
            inVerticalLimbo = relativeMouseCoordinates.getY() < 0 && mouseY > topArrowCenter;
        else
            inVerticalLimbo = false;

        // returns whether it is possible to scale the node
        // (mouse is not in limbo)
        return !inHorizontalLimbo && !inVerticalLimbo;
    }

    private boolean isScaleFlipping(final Point2D relativeMouseCoordinates) {
        // gets the mouse's coordinates
        final double mouseX = relativeMouseCoordinates.getX();
        final double mouseY = relativeMouseCoordinates.getY();
        // gets the space between the node container and arrows
        final double arrowSpace = resizePanel.getArrowSpace();

        final boolean xMinusFlip = mouseX + arrowSpace + left.getLength() / 2 < 0;
        final boolean xPlusFlip = mouseX - arrowSpace - left.getLength() / 2 < 0;
        final boolean yMinusFlip = mouseY + arrowSpace + top.getLength() / 2 < 0;
        final boolean yPlusFlip = mouseY - arrowSpace - top.getLength() / 2 < 0;

        // determines whether a flip should occur along the x-axis or y-axis
        final boolean xFlip = xMinusFlip || xPlusFlip;
        final boolean yFlip = yMinusFlip || yPlusFlip;

        // returns whether a flip should occur
        return xFlip || yFlip;
    }

    private void handleScaleAxisFlip(
            final double mouseAngle,
            final ResizeMode resizeMode,
            final boolean mirrored
    ) {
        // determines the amount of movement to counter
        final double counterWidth = -getUnscaledNodeWidth() * (mirrored ? .5 : 1);
        final double counterHeight = -getUnscaledNodeHeight() * (mirrored ? .5 : 1);

        // sets the node's size to 0 and counters the associated movement
        counterMovement(counterWidth, counterHeight, resizeMode, mirrored);
        setNodeSize(0, 0);
        setResizeDirection(invertScaleDirection(resizeDirection, Math.PI / 2 - nodeAngle, mouseAngle));
    }

    private void counterMovement(
            final double deltaWidth,
            final double deltaHeight,
            final ResizeMode resizeMode,
            final boolean mirrored
    ) {
        // if there is no need to counter the node's movement, exits the method
        if (resizeMode.equals(ResizeMode.NORMAL) && !mirrored) return;

        // determines whether to invert node movement along x-axis or y-axis
        final boolean invertX = resizeMode.equals(ResizeMode.INVERTED_X) || resizeMode.equals(ResizeMode.INVERTED_BOTH);
        final boolean invertY = resizeMode.equals(ResizeMode.INVERTED_Y) || resizeMode.equals(ResizeMode.INVERTED_BOTH);

        // determines the amount of translation necessary to counteract node movement
        final double counterX = mirrored || invertX ? -deltaWidth * scale.getX() : 0;
        final double counterY = mirrored || invertY ? -deltaHeight * scale.getY() : 0;

        // counteracts node movement along the required axes
        translate.setX(translate.getX() + counterX);
        translate.setY(translate.getY() + counterY);
    }

    final void setNodeSize(final double width, final double height) {
        // gets the associated node
        final Node resizeNode = resizePanel.getResizeNode();

        // resizes the node depending on its type
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

    final Rectangle2D getScaledNodeSize(
            final  Point2D relativeMouseCoordinates,
            final double mouseAngle
    ) {
        // gets the mouse's coordinates
        final double mouseX = relativeMouseCoordinates.getX();
        final double mouseY = relativeMouseCoordinates.getY();

        // the node's size after scaling
        final double scaledWidth;
        final double scaledHeight;

        if (mouseAngle < 0 || mouseAngle > Math.PI / 2) return new Rectangle2D(0, 0, 0, 0);

        // we know the node's height
        if (mouseAngle > Math.PI / 2 - nodeAngle) {
            scaledHeight = mouseY;
            scaledWidth = scaledHeight / Math.tan(nodeAngle);
        }
        // we know the node's width
        else if (mouseAngle < Math.PI / 2 - nodeAngle) {
            scaledWidth = mouseX;
            scaledHeight = scaledWidth * Math.tan(nodeAngle);
        }
        // mouse position is in the diagonal of the node's bounding box, so mouse position respects node scale,
        // and we know both scaled node width and height
        else {
            scaledWidth = mouseX;
            scaledHeight = mouseY;
        }

        // returns the scaled node's size
        return new Rectangle2D(0, 0, scaledWidth / scale.getX(), scaledHeight / scale.getY());
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
            case TOP_LEFT     -> nodeContainer.localToScreen(nodeBounds.getMaxX() /*/ scale.getX()*/, nodeBounds.getMaxY() /*/ scale.getY()*/);
            case TOP_RIGHT    -> nodeContainer.localToScreen(nodeBounds.getMinX(), nodeBounds.getMaxY() /*/ scale.getY()*/);
            case BOTTOM_RIGHT -> nodeContainer.localToScreen(nodeBounds.getMinX(), nodeBounds.getMinY());
            case BOTTOM_LEFT  -> nodeContainer.localToScreen(nodeBounds.getMaxX() /*/ scale.getX()*/, nodeBounds.getMinY());
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
                invertedH ? -relativeX : relativeX,
                invertedV ? -relativeY : relativeY
        );
    }

    private double getRelativeMouseAngle(final Point2D relativeMouseCoordinates) {
        // gets the mouse's coordinates
        final double mouseX = relativeMouseCoordinates.getX();
        final double mouseY = relativeMouseCoordinates.getY();
        // determines if the mouse is above the node (using relative mouse coordinates here)
        final boolean mouseAboveNode = mouseY < 0;

        // calculates the mouse's angle
        final double mouseAngle = Math.atan(mouseX / mouseY);

        // calculates the node's angle, adjusting it when necessary
        return Double.isNaN(mouseAngle) ? 0 : mouseAboveNode ? Math.PI + mouseAngle : mouseAngle;
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

            // removes scale from node transforms so that any changes to it will not affect the node
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
        resizeNode.setLayoutX(resizeNode.getLayoutX() - getNodeX() + containerInsets.getLeft());
        resizeNode.setLayoutY(resizeNode.getLayoutY() - getNodeY() + containerInsets.getTop());
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

        // moves the associated node to take into account the border's thickness
        final Node resizeNode = resizePanel.getResizeNode();
        resizeNode.setLayoutX(resizeNode.getLayoutX() + newInsets.getLeft() - previousInsets.getLeft());
        resizeNode.setLayoutY(resizeNode.getLayoutY() + newInsets.getTop() - previousInsets.getBottom());

        // updates the size of the resize panel
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

    private Point2D getResizeArrowPosition(final Orientation resizeDirection) {
        // gets the resize arrow's center based on the current resize direction
        return switch (resizeDirection) {
            case TOP_LEFT     -> getArrowCenter(topLeft);
            case TOP          -> getArrowCenter(top);
            case TOP_RIGHT    -> getArrowCenter(topRight);
            case RIGHT        -> getArrowCenter(right);
            case BOTTOM_RIGHT -> getArrowCenter(bottomRight);
            case BOTTOM       -> getArrowCenter(bottom);
            case BOTTOM_LEFT  -> getArrowCenter(bottomLeft);
            case LEFT         -> getArrowCenter(left);
            default           -> throw new IllegalStateException("Invalid arrow direction: " + resizeDirection);
        };
    }

    private Point2D getArrowCenter(final Arrow arrow) {
        // gets the arrow's size
        final Bounds arrowBounds = arrow.getBoundsInParent();
        final double width = arrowBounds.getWidth();
        final double height = arrowBounds.getHeight();
        // gets the arrow's position
        final double arrowX = arrowBounds.getMinX();
        final double arrowY = arrowBounds.getMinY();
        // returns the coordinates to the arrow's center
        return contentPane.localToScreen(new Point2D(arrowX + width / 2, arrowY + height / 2));
    }

    private Rectangle2D getNodeSize() {
        final Bounds bounds = resizePanel.getResizeNode().getBoundsInParent();
        return new Rectangle2D(0, 0, bounds.getWidth(), bounds.getHeight());
    }

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

    private double getNodeX() {
        return getNodePosition().getX();
    }

    private double getNodeY() {
        return getNodePosition().getY();
    }

    private Rectangle2D getUnscaledNodeSize() {
        final Rectangle2D nodeSize = getNodeSize();

        final double scaledWidth = nodeSize.getWidth() / scale.getX();
        final double scaledHeight = nodeSize.getHeight() / scale.getY();

        return new Rectangle2D(0, 0, scaledWidth, scaledHeight);
    }

    private double getUnscaledNodeWidth() {
        return getNodeWidth() / scale.getX();
    }

    private double getUnscaledNodeHeight() {
        return getNodeHeight() / scale.getY();
    }

    private double getContainerWidth() {
        return nodeContainer.getBoundsInParent().getWidth();
    }

    private double getContainerHeight() {
        return nodeContainer.getBoundsInParent().getHeight();
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
}
