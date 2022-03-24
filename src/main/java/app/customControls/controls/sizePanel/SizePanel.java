package app.customControls.controls.sizePanel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;

public class SizePanel extends Control {

    // =====================================
    //                FIELDS
    // =====================================

    // constants

    private static final Node DEFAULT_NODE = new Region();
    private static final Rectangle2D DEFAULT_ARROW_SIZE = new Rectangle2D(0, 0, 25, 10);

    static {
        DEFAULT_NODE.resize(50, 50);
    }

    // properties

    private final ObjectProperty<Node> associatedNode;
    private final DoubleProperty arrowWidth;
    private final DoubleProperty arrowHeight;

    // =====================================
    //              CONSTRUCTOR
    // =====================================

    public SizePanel() {
        this(DEFAULT_NODE);
    }

    public SizePanel(final Node associatedNode) {
        this(associatedNode, DEFAULT_ARROW_SIZE.getWidth(), DEFAULT_ARROW_SIZE.getHeight());
    }

    public SizePanel(final Node associatedNode, final double arrowWidth, final double arrowHeight) {

        // property initialisation

        this.associatedNode = new SimpleObjectProperty<>(SizePanel.this, "associated-node", DEFAULT_NODE);
        this.arrowWidth = new SimpleDoubleProperty(SizePanel.this, "arrow-width", DEFAULT_ARROW_SIZE.getWidth());
        this.arrowHeight = new SimpleDoubleProperty(SizePanel.this, "arrow-height", DEFAULT_ARROW_SIZE.getHeight());

        // property setting

        setAssociatedNode(associatedNode);
        setArrowWidth(arrowWidth);
        setArrowHeight(arrowHeight);
    }

    // =====================================
    //                STYLE
    // =====================================

    @Override
    protected Skin<?> createDefaultSkin() {
        return super.createDefaultSkin();
    }


    // =====================================
    //              PROPERTIES
    // =====================================

    public ObjectProperty<Node> associatedNodeProperty() {
        return associatedNode;
    }

    public DoubleProperty arrowWidthProperty() {
        return arrowWidth;
    }

    public DoubleProperty arrowHeightProperty() {
        return arrowHeight;
    }

    // =====================================
    //               GETTERS
    // =====================================

    public Node getAssociatedNode() {
        return associatedNode.get();
    }

    public double getArrowWidth() {
        return arrowWidth.get();
    }

    public double getArrowHeight() {
        return arrowHeight.get();
    }

    // =====================================
    //               SETTERS
    // =====================================

    public void setAssociatedNode(final Node newNode) {
        associatedNode.set(newNode);
    }

    public void setArrowWidth(final double newWidth) {
        if (!validateArrowWidth(newWidth)) return;
        arrowWidth.set(newWidth);
    }

    public void setArrowHeight(final double newHeight) {
        if (!validateArrowHeight(newHeight)) return;
        arrowHeight.set(newHeight);
    }

    // =====================================
    //              VALIDATION
    // =====================================

    private boolean validateArrowWidth(final double newWidth) {
        return newWidth > 0 && newWidth > getArrowHeight() * 2;
    }

    private boolean validateArrowHeight(final double newHeight) {
        return newHeight > 0 && newHeight * 2 < getArrowWidth();
    }

}
