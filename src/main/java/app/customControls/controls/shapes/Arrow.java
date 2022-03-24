package app.customControls.controls.shapes;

import app.customControls.utilities.CssUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import java.awt.geom.Point2D;
import java.util.List;

public class Arrow extends Region {

    // =====================================
    //                FIELDS
    // =====================================

    // enum

    public enum ArrowType {
        DOUBLE,
        SINGLE,
        NULL
    }

    // constants

    private static final double DEFAULT_WIDTH = 40;
    private static final double DEFAULT_HEIGHT = 20;
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final ArrowType DEFAULT_ARROW_TYPE = ArrowType.SINGLE;
    private static final Orientation DEFAULT_ORIENTATION = Orientation.RIGHT;

    // styleable properties

    private static final StyleablePropertyFactory<Arrow> FACTORY =
            new StyleablePropertyFactory<>(Region.getClassCssMetaData());

    private static final CssMetaData<Arrow, Color> COLOR_CSS = FACTORY.createColorCssMetaData(
            "-fx-fill", arrow -> arrow.color, DEFAULT_COLOR, false
    );
    private final StyleableProperty<Color> color;

    private static final CssMetaData<Arrow, Orientation> ORIENTATION_CSS = FACTORY.createEnumCssMetaData(
            Orientation.class, "-fx-orientation", arrow -> arrow.orientation
    );
    private final StyleableProperty<Orientation> orientation;

    private static final CssMetaData<Arrow, ArrowType> ARROW_TYPE_CSS = FACTORY.createEnumCssMetaData(
            ArrowType.class, "-fx-arrow-type", arrow -> arrow.arrowType
    );
    private final StyleableProperty<ArrowType> arrowType;

    // pseudo classes

    private static final PseudoClass DOUBLE_PsC = PseudoClass.getPseudoClass("double");
    private final BooleanProperty doubleArrow;
    private static final PseudoClass SINGLE_PsC = PseudoClass.getPseudoClass("single");
    private final BooleanProperty singleArrow;
    private static final PseudoClass TOP_LEFT_PsC = PseudoClass.getPseudoClass("top-left");
    private final BooleanProperty topLeftOrient;
    private static final PseudoClass TOP_PsC = PseudoClass.getPseudoClass("top");
    private final BooleanProperty topOrient;
    private static final PseudoClass TOP_RIGHT_PsC = PseudoClass.getPseudoClass("top-right");
    private final BooleanProperty topRightOrient;
    private static final PseudoClass RIGHT_PsC = PseudoClass.getPseudoClass("right");
    private final BooleanProperty rightOrient;
    private static final PseudoClass BOTTOM_RIGHT_PsC = PseudoClass.getPseudoClass("bottom-right");
    private final BooleanProperty bottomRightOrient;
    private static final PseudoClass BOTTOM_PsC = PseudoClass.getPseudoClass("bottom");
    private final BooleanProperty bottomOrient;
    private static final PseudoClass BOTTOM_LEFT_PsC = PseudoClass.getPseudoClass("bottom-left");
    private final BooleanProperty bottomLeftOrient;
    private static final PseudoClass LEFT_PsC = PseudoClass.getPseudoClass("left");
    private final BooleanProperty leftOrient;
    private static final PseudoClass HORIZONTAL_PsC = PseudoClass.getPseudoClass("horizontal");
    private final BooleanProperty horizontalArrow;
    private static final PseudoClass VERTICAL_PsC = PseudoClass.getPseudoClass("vertical");
    private final BooleanProperty verticalArrow;
    private static final PseudoClass DIAGONAL_PsC = PseudoClass.getPseudoClass("diagonal");
    private final BooleanProperty diagonalArrow;

    // components

    final Region line;
    final Triangle arrow1;
    final Triangle arrow2;
    final Pane pane;

    // transforms

    final Rotate rotate;

    // listeners

    private final ChangeListener<Color> colorListener;
    private final ChangeListener<Orientation> orientationListener;
    private final ChangeListener<ArrowType> arrowTypeListener;

    // =====================================
    //              CONSTRUCTOR
    // =====================================

    public Arrow() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Arrow(final Orientation orientation) {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, orientation);
    }

    public Arrow(final double width, final double height) {
        this(width, height, DEFAULT_ORIENTATION);
    }

    public Arrow(final double width, final double height, final Orientation orientation) {
        this(width, height, orientation, DEFAULT_COLOR, DEFAULT_ARROW_TYPE);
    }

    public Arrow(
            final double width,
            final double height,
            final Orientation orientation,
            final Color color,
            final ArrowType arrowType
    ) {

        // initialising properties

        this.color = new SimpleStyleableObjectProperty<>(COLOR_CSS, this, "fill");
        this.orientation = new SimpleStyleableObjectProperty<>(ORIENTATION_CSS, this, "orientation");
        this.arrowType = new SimpleStyleableObjectProperty<>(ARROW_TYPE_CSS, this, "arrow-type");
        this.doubleArrow = CssUtil.pseudoClassProperty(DOUBLE_PsC, Arrow.this, "double");
        this.singleArrow = CssUtil.pseudoClassProperty(SINGLE_PsC, Arrow.this, "single");
        this.topLeftOrient = CssUtil.pseudoClassProperty(TOP_LEFT_PsC, Arrow.this, "top-left");
        this.topOrient = CssUtil.pseudoClassProperty(TOP_PsC, Arrow.this, "top");
        this.topRightOrient = CssUtil.pseudoClassProperty(TOP_RIGHT_PsC, Arrow.this, "top-right");
        this.rightOrient = CssUtil.pseudoClassProperty(RIGHT_PsC, Arrow.this, "right");
        this.bottomRightOrient = CssUtil.pseudoClassProperty(BOTTOM_RIGHT_PsC, Arrow.this, "bottom-right");
        this.bottomOrient = CssUtil.pseudoClassProperty(BOTTOM_PsC, Arrow.this, "bottom");
        this.bottomLeftOrient = CssUtil.pseudoClassProperty(BOTTOM_LEFT_PsC, Arrow.this, "bottom-left");
        this.leftOrient = CssUtil.pseudoClassProperty(LEFT_PsC, Arrow.this, "left");
        this.horizontalArrow = CssUtil.pseudoClassProperty(HORIZONTAL_PsC, Arrow.this, "horizontal");
        this.verticalArrow = CssUtil.pseudoClassProperty(VERTICAL_PsC, Arrow.this, "vertical");
        this.diagonalArrow = CssUtil.pseudoClassProperty(DIAGONAL_PsC, Arrow.this, "diagonal");

        // initialising listeners

        this.colorListener = this::updateColor;
        this.orientationListener = this::updateOrientation;
        this.arrowTypeListener = this::updateArrowType;

        // initialising components

        this.line = new Region();
        this.arrow1 = new Triangle();
        this.arrow2 = new Triangle();
        this.pane = new Pane();
        this.line.setStyle("-fx-background-color: #" + color.toString().replace("0x", ""));

        this.rotate = new Rotate();
        getTransforms().add(rotate);
        arrow1.setRotation(180);

        // settings properties

        setPrefWidth(width);
        setPrefHeight(height);
        setOrientation(orientation);
        setColor(color);
        setArrowType(arrowType);

        // initialisation
        style();
        populate();
        registerListeners();

        // positioning

        Platform.runLater(() -> {
            resize();
            reposition();
        });
    }

    // =====================================
    //            INITIALISATION
    // =====================================

    private void style() {
        arrow1.getStyleClass().add("inner-arrow");
        arrow2.getStyleClass().add("outer-arrow");
        line.getStyleClass().add("line");
        getStyleClass().add("arrow");
    }

    private void populate() {
        pane.getChildren().addAll(
                line,
                arrow1,
                arrow2
        );
        getChildren().add(pane);
    }

    private void registerListeners() {
        colorProperty().addListener(colorListener);
        orientationProperty().addListener(orientationListener);
        arrowTypeProperty().addListener(arrowTypeListener);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }

    // =====================================
    //               UPDATING
    // =====================================

    private void updateColor(ObservableValue<? extends Color> value, Color oldColor, Color newColor) {
        color.setValue(newColor);
        line.setStyle("-fx-background-color: #" + newColor.toString().replace("0x", ""));
        arrow1.setFill(newColor);
        arrow2.setFill(newColor);
    }

    private void updateOrientation(ObservableValue<? extends Orientation> value, Orientation oldOrientation, Orientation newOrientation) {
        orientation.setValue(newOrientation);
        reposition();
    }

    private void updateArrowType(ObservableValue<? extends ArrowType> value, ArrowType oldType, ArrowType newType) {
        arrowType.setValue(newType);
        setArrowType(getArrowType());
    }


    // =====================================
    //             POSITIONING
    // =====================================

    private void resize() {;
        final double width = getWidth();
        final double height = getHeight();

        arrow1.setLength(height);
        arrow2.setLength(height);

        final double arrowWidth = arrow2.getBoundsInParent().getWidth();
        final int arrowCount = getArrowType().equals(ArrowType.DOUBLE) ? 2 : 1;

        line.setPrefWidth(width - arrowCount * (arrowWidth - 0.5));
        line.setPrefHeight(height / 2);

        setLayoutX(0);
        setLayoutY(-height / 2);
    }

    private void reposition() {
        final double width = getWidth();
        final double height = getHeight();
        final double arrowWidth = arrow2.getBoundsInParent().getWidth();

        line.setLayoutX(getArrowType().equals(ArrowType.DOUBLE) ? arrowWidth - 0.5 : 0);
        line.setLayoutY((height - height / 2) / 2);
        arrow2.setLayoutX(width - arrow2.getBoundsInParent().getWidth() + arrowWidth / 4);

        switch (getOrientation()) {
            case TOP_LEFT       -> setRotation(-135);
            case TOP            -> setRotation(-90);
            case TOP_RIGHT      -> setRotation(-45);
            case RIGHT          -> setRotation(0);
            case BOTTOM_RIGHT   -> setRotation(45);
            case BOTTOM         -> setRotation(90);
            case BOTTOM_LEFT    -> setRotation(135);
            case LEFT           -> setRotation(180);
        }

        rotate.setPivotX(0);
        rotate.setPivotY(height / 2);
    }

    public void moveTo(final Point2D target) {
        moveTo(target.getX(), target.getY());
    }

    public void moveTo(final double x, final double y) {
        setLayoutX(x);
        setLayoutY(y - getHeight() / 2);
    }

    // =====================================
    //              PROPERTIES
    // =====================================

    public ObjectProperty<Color> colorProperty() {
        return (ObjectProperty<Color>) color;
    }

    public ObjectProperty<Orientation> orientationProperty() {
        return (ObjectProperty<Orientation>) orientation;
    }

    public ObjectProperty<ArrowType> arrowTypeProperty() {
        return (ObjectProperty<ArrowType>) arrowType;
    }

    // =====================================
    //               GETTERS
    // =====================================

    public Color getColor() {
        return color.getValue();
    }

    public Orientation getOrientation() {
        return orientation.getValue();
    }

    public ArrowType getArrowType() {
        return arrowType.getValue();
    }

    public double getRotation() {
        return rotate.getAngle();
    }

    public double getDisplayRotation() {
        final double rotation = -getRotation();
        final double adjustRotation = (rotation < 0 ? (rotation + 360 * (int) Math.abs(rotation) / 360 + 1) : rotation) % 360;
        return Math.abs(adjustRotation);
    }

    public double getLocalRotation() {
        final double displayRotation = getDisplayRotation();
        if (displayRotation > 90 && displayRotation <= 180) return displayRotation - 90;
        else if (displayRotation > 180 && displayRotation <= 270) return displayRotation - 180;
        else if (displayRotation > 270 && displayRotation < 360) return displayRotation - 270;
        else return displayRotation;
    }

    public double getDisplayWidth() {
        return Math.cos(Math.toRadians(getLocalRotation())) * getWidth();
    }

    public double getDisplayHeight() {
        return Math.sin(Math.toRadians(getLocalRotation())) * getWidth();
    }

    // =====================================
    //               SETTERS
    // =====================================

    public void setColor(final Color newColor) {
        color.setValue(newColor);
    }

    public void setOrientation(final Orientation newOrientation) {
        orientation.setValue(newOrientation);

        topLeftOrient.set(newOrientation.equals(Orientation.TOP_LEFT));
        topOrient.set(newOrientation.equals(Orientation.TOP));
        topRightOrient.set(newOrientation.equals(Orientation.TOP_RIGHT));
        rightOrient.set(newOrientation.equals(Orientation.RIGHT));
        bottomRightOrient.set(newOrientation.equals(Orientation.BOTTOM_RIGHT));
        bottomOrient.set(newOrientation.equals(Orientation.BOTTOM));
        bottomLeftOrient.set(newOrientation.equals(Orientation.BOTTOM_LEFT));
        leftOrient.set(newOrientation.equals(Orientation.LEFT));
        horizontalArrow.set(newOrientation.equals(Orientation.LEFT) || newOrientation.equals(Orientation.RIGHT));
        verticalArrow.set(newOrientation.equals(Orientation.TOP) || newOrientation.equals(Orientation.BOTTOM));
        diagonalArrow.set(!horizontalArrow.get() && !verticalArrow.get());
    }

    public void setArrowType(final ArrowType newArrowType) {
        arrowType.setValue(newArrowType);
        doubleArrow.set(newArrowType.equals(ArrowType.DOUBLE));
        singleArrow.set(newArrowType.equals(ArrowType.SINGLE));
        arrow1.setVisible(newArrowType.equals(ArrowType.DOUBLE));

        resize();
        reposition();
    }

    public void setRotation(final double rotation) {
        if (Math.abs(rotation) % 45 != 0) setOrientation(Orientation.NULL);
        rotate.setAngle(rotation);
    }
}
