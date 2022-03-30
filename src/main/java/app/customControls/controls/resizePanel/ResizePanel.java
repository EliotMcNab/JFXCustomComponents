package app.customControls.controls.resizePanel;

import app.customControls.controls.shapes.Arrow;
import app.customControls.utilities.CssUtil;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import java.util.List;

/**
 * Handles {@link Node} resizing with a custom {@link Arrow} ui which appears when the user clicks on the node
 */
public class ResizePanel extends Control {

    // =====================================
    //                FIELDS
    // =====================================

    // constants

    private static final Region DEFAULT_NODE = new Region();
    private static final double DEFAULT_ARROW_SPACE = 3;
    private static final Translate DEFAULT_TRANSLATE = new Translate();
    private static final Scale DEFAULT_SCALE = new Scale();

    static {
        DEFAULT_NODE.setPrefWidth(50);
        DEFAULT_NODE.setPrefHeight(50);
    }

    // properties

    private static final StyleablePropertyFactory<ResizePanel> FACTORY =
            new StyleablePropertyFactory<>(Control.getClassCssMetaData());

    private static final CssMetaData<ResizePanel, Number> ARROW_SPACE_CSS = FACTORY.createSizeCssMetaData(
            "fx-arrow-space", sizePanel -> sizePanel.arrowSpace, DEFAULT_ARROW_SPACE, false
    );
    private final StyleableProperty<Number> arrowSpace;

    private final ObjectProperty<Node> associatedNode;
    private final DoubleProperty arrowLength;
    private final DoubleProperty arrowThickness;
    private final ObjectProperty<Color> arrowColor;
    private final ObjectProperty<Translate> translate;
    private final ObjectProperty<Scale> scale;
    private final BooleanProperty zoomUpdateProperty;

    // pseudo classes

    private static final PseudoClass SELECTED_PsC = PseudoClass.getPseudoClass("selected");
    private final BooleanProperty selected;

    // =====================================
    //              CONSTRUCTOR
    // =====================================

    /**
     * Default constructor for the {@link ResizePanel} class
     */
    public ResizePanel() {
        this(DEFAULT_NODE);
    }

    /**
     * Constructor for the {@link ResizePanel} class
     * @param associatedNode ({@link Node}): the node associated to the resize panel
     */
    public ResizePanel(final Node associatedNode) {
        this(associatedNode, Arrow.DEFAULT_WIDTH, Arrow.DEFAULT_HEIGHT);
    }

    /**
     * Constructor for the {@link ResizePanel} class
     * @param associatedNode ({@link Node}): the node associated to the resize panel
     * @param arrowLength (double): the length of arrow in the resize panel
     * @param arrowThickness (double): the thickness of arrow in the resize panel
     */
    public ResizePanel(final Node associatedNode, final double arrowLength, final double arrowThickness) {
        this(associatedNode, arrowLength, arrowThickness, Arrow.DEFAULT_COLOR);
    }

    /**
     * Constructor for the {@link ResizePanel} class
     * @param associatedNode ({@link Node}): the node associated to the resize panel
     * @param arrowLength (double): the length of arrow in the resize panel
     * @param arrowThickness (double): the thickness of arrow in the resize panel
     * @param arrowColor ({@link Color}): the color of arrows in the resize panel
     */
    public ResizePanel(
            final Node associatedNode,
            final double arrowLength,
            final double arrowThickness,
            final Color arrowColor
    ) {
        // property initialisation

        this.associatedNode = new SimpleObjectProperty<>(ResizePanel.this, "associated-node", DEFAULT_NODE);
        this.arrowLength = new SimpleDoubleProperty(ResizePanel.this, "arrow-width", Arrow.DEFAULT_WIDTH);
        this.arrowThickness = new SimpleDoubleProperty(ResizePanel.this, "arrow-height", Arrow.DEFAULT_HEIGHT);
        this.arrowSpace = new SimpleStyleableDoubleProperty(ARROW_SPACE_CSS, ResizePanel.this, "arrow-space", DEFAULT_ARROW_SPACE);
        this.arrowColor = new SimpleObjectProperty<>(ResizePanel.this, "arrow-color", Arrow.DEFAULT_COLOR);
        this.translate = new SimpleObjectProperty<>(ResizePanel.this, "translate", DEFAULT_TRANSLATE);
        this.scale = new SimpleObjectProperty<>(ResizePanel.this, "scale", DEFAULT_SCALE);
        this.zoomUpdateProperty = new SimpleBooleanProperty(ResizePanel.this, "zoom-update", false);

        // pseudo classes initialisation

        this.selected = CssUtil.pseudoClassProperty(SELECTED_PsC, ResizePanel.this, "selected");

        // property setting

        setResizeNode(associatedNode);
        setArrowLength(arrowLength);
        setArrowThickness(arrowThickness);
        setArrowColor(arrowColor);
    }

    // =====================================
    //                STYLE
    // =====================================

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResizePanelSkin(ResizePanel.this);
    }

    // =====================================
    //              PROPERTIES
    // =====================================

    public ObjectProperty<Node> associatedNodeProperty() {
        return associatedNode;
    }

    /**
     * Gets the {@link ResizePanel}'s associated node's width property
     * @return (DoubleProperty): associated node width property
     */
    public DoubleProperty nodeWidthProperty() {
        final Node associatedNode = getResizeNode();

        if (associatedNode instanceof Region) {
            return (DoubleProperty) ((Region) associatedNode).widthProperty();
        } else if (associatedNode instanceof Rectangle) {
            return ((Rectangle) associatedNode).widthProperty();
        } else if (associatedNode instanceof Circle) {
            return ((Circle) associatedNode).radiusProperty();
        } else {
            return null;
        }
    }

    /**
     * Gets the {@link ResizePanel}'s associated node's height property
     * @return (DoubleProperty): associated node height property
     */
    public DoubleProperty nodeHeightProperty() {
        final Node associatedNode = getResizeNode();

        if (associatedNode instanceof Region) {
            return (DoubleProperty) ((Region) associatedNode).heightProperty();
        } else if (associatedNode instanceof Rectangle) {
            return ((Rectangle) associatedNode).heightProperty();
        } else if (associatedNode instanceof Circle) {
            return ((Circle) associatedNode).radiusProperty();
        } else {
            return null;
        }
    }

    /**
     * Gets the length property of {@link Arrow Arrows} in the {@link ResizePanel}
     * @return (DoubleProperty): length property of Arrows in the resize panel
     */
    public DoubleProperty arrowLengthProperty() {
        return arrowLength;
    }

    /**
     * Gets the thickness property of {@link Arrow Arrows} in the {@link ResizePanel}
     * @return (DoubleProperty): length property of Arrows in the resize panel
     */
    public DoubleProperty arrowThicknessProperty() {
        return arrowThickness;
    }

    public DoubleProperty arrowSpaceProperty() {
        return (DoubleProperty) arrowSpace;
    }

    public ObjectProperty<Color> arrowColorProperty() {
        return arrowColor;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public ObjectProperty<Translate> translateProperty() {
        return translate;
    }

    public ObjectProperty<Scale> scaleProperty() {
        return scale;
    }

    public DoubleProperty zoomXProperty() {
        return scale.get().xProperty();
    }

    public DoubleProperty zoomYProperty() {
        return scale.get().yProperty();
    }

    public DoubleProperty zoomPivotXProperty() {
        return scale.get().pivotXProperty();
    }

    public DoubleProperty zoomPivotYProperty() {
        return scale.get().pivotYProperty();
    }

    public BooleanProperty zoomUpdateProperty() {
        return zoomUpdateProperty;
    }

    // =====================================
    //               GETTERS
    // =====================================

    public Node getResizeNode() {
        return associatedNode.get();
    }

    public double getArrowLength() {
        return arrowLength.get();
    }

    public double getArrowThickness() {
        return arrowThickness.get();
    }

    public double getArrowSpace() {
        return arrowSpace.getValue().doubleValue();
    }

    public Color getArrowColor() {
        return arrowColor.get();
    }

    public boolean getSelected() {
        return selected.get();
    }

    public Scale getScale() {
        return scale.get();
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    // =====================================
    //               SETTERS
    // =====================================

    public void setResizeNode(final Node newNode) {
        associatedNode.setValue(newNode);
    }

    public void setArrowLength(final double newWidth) {
        if (!validateArrowWidth(newWidth)) return;
        arrowLength.set(newWidth);
    }

    public void setArrowThickness(final double newHeight) {
        if (!validateArrowHeight(newHeight)) return;
        arrowThickness.set(newHeight);
    }

    public void setArrowSpace(final double newArrowSpace) {
        if (!validateArrowSpace(newArrowSpace)) return;
        arrowSpace.setValue(newArrowSpace);
    }

    public void setArrowColor(final Color newColor) {
        arrowColor.set(newColor);
    }

    public void setSelected(final boolean isSelected) {
        selected.set(isSelected);
    }

    public void replaceTranslate(final Translate newTranslate) {
        if (!validateTransform(newTranslate)) return;
        translate.set(newTranslate);
    }

    public void replaceScale(final Scale newScale) {
        if (!validateTransform(newScale)) return;
        zoomUpdateProperty.set(true);
        scale.set(newScale);
        zoomUpdateProperty.set(false);
    }

    public void setZoom(final double x, final double y, final double pivotX, final double pivotY) {
        if (!validateScale(x, y)) return;
        zoomUpdateProperty.set(true);
        scale.get().setX(x);
        scale.get().setY(y);
        scale.get().setPivotX(pivotX);
        scale.get().setPivotY(pivotY);
        zoomUpdateProperty.set(false);
    }

    // =====================================
    //              VALIDATION
    // =====================================

    private boolean validateArrowWidth(final double newWidth) {
        return newWidth > 0 && newWidth > getArrowThickness() * 2;
    }

    private boolean validateArrowHeight(final double newHeight) {
        return newHeight > 0 && newHeight * 2 < getArrowLength();
    }

    private boolean validateArrowSpace(final double newArrowSpace) {
        return newArrowSpace > 0;
    }

    private boolean validateTransform(final Transform transform) {
        return transform != null;
    }

    private boolean validateScale(final double x, final double y) {
        return x >= 0 && y >= 0;
    }
}
