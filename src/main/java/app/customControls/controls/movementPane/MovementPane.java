package app.customControls.controls.movementPane;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class MovementPane extends Control {

    // =======================================
    //                FIELDS
    // =======================================

    // default values

    private static final double DEFAULT_VAL = 0;
    private static final double DEFAULT_MIN_VAL = 0;
    private static final double DEFAULT_MAX_VAL = 1000;
    private static final double DEFAULT_VISIBLE = 500;
    private static final Node DEFAULT_NODE = null;

    // properties

    private final DoubleProperty hValue;
    private final DoubleProperty vValue;
    private final DoubleProperty minHVal;
    private final DoubleProperty maxHVal;
    private final DoubleProperty hVisible;
    private final DoubleProperty minVVal;
    private final DoubleProperty maxVVal;
    private final DoubleProperty vVisible;
    private final ObjectProperty<Node> movementNode;

    // =======================================
    //              CONSTRUCTOR
    // =======================================

    public MovementPane() {

        this.hValue = new SimpleDoubleProperty(this, "hValue", DEFAULT_VAL);
        this.vValue = new SimpleDoubleProperty(this, "vValue", DEFAULT_VAL);
        this.minHVal = new SimpleDoubleProperty(this, "minHVal", DEFAULT_MIN_VAL);
        this.minVVal = new SimpleDoubleProperty(this, "minVVal", DEFAULT_MIN_VAL);
        this.maxHVal = new SimpleDoubleProperty(this, "maxHVal", DEFAULT_MAX_VAL);
        this.maxVVal = new SimpleDoubleProperty(this, "minVVal", DEFAULT_MAX_VAL);
        this.hVisible = new SimpleDoubleProperty(this, "visibleH", DEFAULT_VISIBLE);
        this.vVisible = new SimpleDoubleProperty(this, "visibleV", DEFAULT_VISIBLE);
        this.movementNode = new SimpleObjectProperty<>(this, "movementNode", DEFAULT_NODE);

    }

    // =======================================
    //                 STYLE
    // =======================================

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MovementPaneSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return MovementPane.class.getResource("/app/customControls/style/movement-pane.css").toExternalForm();
    }

    // =======================================
    //                MOVEMENT
    // =======================================

    public ObjectProperty<Node> associatedNodeProperty() {
        return movementNode;
    }

    public Node getAssociatedNode() {
        return movementNode.get();
    }

    public void setMovementNode(final Node node) {
        movementNode.set(node);
    }

    // =======================================
    //               PROPERTIES
    // =======================================

    public DoubleProperty horizontalValueProperty() {
        return hValue;
    }

    public DoubleProperty verticalValueProperty() {
        return vValue;
    }

    public DoubleProperty horizontalMinProperty() {
        return minHVal;
    }

    public DoubleProperty verticalMinProperty() {
        return minVVal;
    }

    public DoubleProperty horizontalMaxProperty() {
        return maxHVal;
    }

    public DoubleProperty verticalMaxProperty() {
        return maxVVal;
    }

    public DoubleProperty horizontalVisibleProperty() {
        return hVisible;
    }

    public DoubleProperty verticalVisibleProperty() {
        return vVisible;
    }

    // =======================================
    //                GETTERS
    // =======================================

    public double getHValue() {
        return hValue.get();
    }

    public double getVValue() {
        return vValue.get();
    }

    public double getHorizontalMin() {
        return minHVal.get();
    }

    public double getVerticalMin() {
        return minVVal.get();
    }

    public double getHorizontalMax() {
        return maxHVal.get();
    }

    public double getVerticalMax() {
        return maxVVal.get();
    }

    public double getHorizontalVisibleAmount() {
        return hVisible.get();
    }

    public double getVerticalVisibleAmount() {
        return vVisible.get();
    }

    // =======================================
    //                SETTERS
    // =======================================

    public void setHValue(final double newVal) {
        if (!validateHVal(newVal)) return;
        hValue.set(newVal);
    }

    public void setVValue(final double newVal) {
        if (!validateVVal(newVal)) return;
        vValue.set(newVal);
    }

    public void setHorizontalMin(final double newMin) {
        if (!validateHMin(newMin)) return;
        minHVal.set(newMin);
    }

    public void setVerticalMin(final double newMin) {
        if (!validateVMin(newMin)) return;
        minVVal.set(newMin);
    }

    public void setHorizontalMax(final double newMax) {
        if (!validateHMax(newMax)) return;
        maxHVal.set(newMax);
    }

    public void setVerticalMax(final double newMax) {
        if (!validateVMax(newMax)) return;
    }

    public void setHorizontalVisibleAmount(final double newVisibleAmount) {
        if (!validateHVisible(newVisibleAmount)) return;
        hVisible.set(newVisibleAmount);
    }

    public void setVerticalVisibleAmount(final double newVisibleAmount) {
        if (!validateVVisible(newVisibleAmount)) return;
        vVisible.set(newVisibleAmount);
    }

    // =======================================
    //              VALIDATION
    // =======================================

    private boolean validateHVal(final double newVal) {
        return newVal >= getHorizontalMin() && newVal <= getHorizontalMax();
    }

    private boolean validateVVal(final double newVal) {
        return newVal >= getVerticalMin() && newVal <= getVerticalMax();
    }

    private boolean validateHMin(final double newMin) {
        return newMin < getHorizontalMax();
    }

    private boolean validateVMin(final double newMin) {
        return newMin < getVerticalMax();
    }

    private boolean validateHMax(final double newMax) {
        return newMax > getHorizontalMin();
    }

    private boolean validateVMax(final double newMax) {
        return newMax > getVerticalMin();
    }

    private boolean validateHVisible(final double newVisibleAmount) {
        return newVisibleAmount > getHorizontalMin() && newVisibleAmount < getHorizontalMax();
    }

    private boolean validateVVisible(final double newVisibleAmount) {
        return newVisibleAmount > getVerticalMin() && newVisibleAmount < getHorizontalMax();
    }

}
