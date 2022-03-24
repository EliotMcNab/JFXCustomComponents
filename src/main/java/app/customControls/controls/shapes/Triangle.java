package app.customControls.controls.shapes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;

public class Triangle extends Polygon {

    // =====================================
    //                FIELDS
    // =====================================

    // constants

    private static final Point2D[] DEFAULT_POINTS = {
            new Point2D(0, 0), new Point2D(17.320508076, 10), new Point2D(0, 20)
    };
    private static final double SIN60 = 0.86602540378444;

    // properties

    private final ObjectProperty<Point2D> p1;
    private final ObjectProperty<Point2D> p2;
    private final ObjectProperty<Point2D> p3;
    private final ObjectProperty<Point2D[]> points;

    // rotation

    private final Rotate rotate;
    private final DoubleProperty rotation;

    // listeners

    private final ChangeListener<Point2D[]> pointListener;

    // =====================================
    //              CONSTRUCTOR
    // =====================================

    public Triangle() {
        this(DEFAULT_POINTS);
    }

    public Triangle(final double length) {
        this(calculatePoints(length));
    }

    public Triangle(final Point2D p1, final Point2D p2, final Point2D p3) {
        this(new Point2D[]{p1, p2, p3});
    }

    public Triangle(final Point2D[] points) {
        if (points.length != 3) throw new IllegalArgumentException(
                String.format("Triangle needs 3 points to be defined, current number of points:%s\n", points.length)
        );

        // properties

        this.p1 = new SimpleObjectProperty<>(this, "p1", DEFAULT_POINTS[0]);
        this.p2 = new SimpleObjectProperty<>(this, "p2", DEFAULT_POINTS[1]);
        this.p3 = new SimpleObjectProperty<>(this, "p3", DEFAULT_POINTS[2]);
        this.points = new SimpleObjectProperty<>();

        this.points.bind(Bindings.createObjectBinding(
                () -> new Point2D[]{getP1(), getP2(), getP3()},
                p1, p2, p3
        ));

        // transformations

        final Point2D center = getCenter();
        this.rotate = new Rotate(0, center.getX(), center.getY());
        this.rotation = new SimpleDoubleProperty(this, "rotation", 0);
        getTransforms().setAll(rotate);

        // setting values

        setP1(points[0]);
        setP2(points[1]);
        setP3(points[2]);

        // listeners

        this.pointListener = this::updatePoints;

        // initialisation

        style();
        registerListeners();
        updatePolygon();
    }

    // =====================================
    //            INITIALISATION
    // =====================================

    private void style() {
        getStyleClass().add("triangle");
    }

    private void registerListeners() {
        pointsProperty().addListener(pointListener);
    }

    // =====================================
    //               UPDATING
    // =====================================

    private void updatePoints(ObservableValue<? extends Point2D[]> value, Point2D[] oldPoints, Point2D[] newPoints) {
        final Point2D oldP1 = oldPoints[0];
        final Point2D newP1 = newPoints[0];
        final Point2D oldP2 = oldPoints[1];
        final Point2D newP2 = newPoints[1];
        final Point2D oldP3 = oldPoints[2];
        final Point2D newP3 = newPoints[2];

        if (!oldP1.equals(newP1)) setP1(newP1);
        if (!oldP2.equals(newP2)) setP2(newP2);
        if (!oldP3.equals(newP3)) setP3(newP3);

        updatePolygon();
    }

    private void updatePolygon() {
        final Point2D center = getCenter();
        final Point2D p1Corrected = getP1().subtract(center);
        final Point2D p2Corrected = getP2().subtract(center);
        final Point2D p3Corrected = getP3().subtract(center);

        getPoints().clear();
        getPoints().addAll(
                p1Corrected.getX(), p1Corrected.getY(),
                p2Corrected.getX(), p2Corrected.getY(),
                p3Corrected.getX(), p3Corrected.getY()
        );

        rotate.setPivotX(center.getX());
        rotate.setPivotY(getLength() / 2 - center.getY());
        setLayoutX(center.getX());
        setLayoutY(center.getY());
    }

    // =====================================
    //               POINTS
    // =====================================

    private static Point2D[] calculatePoints(final double length) {
        final Point2D p1 = new Point2D(0, 0);
        final Point2D p2 = new Point2D(SIN60 * length, length / 2);
        final Point2D p3 = new Point2D(0, length);
        return new Point2D[]{p1, p2, p3};
    }

    // =====================================
    //              PROPERTIES
    // =====================================

    final ObjectProperty<Point2D[]> pointsProperty() {
        return points;
    }

    // =====================================
    //               SETTERS
    // =====================================

    public void setP1(final Point2D newP1) {
        p1.set(newP1);
    }

    public void setP1(final double p1x, final double p1y) {
        p1.set(new Point2D(p1x, p1y));
    }

    public void setP2(final Point2D newP2) {
        p2.set(newP2);
    }

    public void setP2(final double p2x, final double p2y) {
        p2.set(new Point2D(p2x, p2y));
    }

    public void setP3(final Point2D newP3) {
        p3.set(newP3);
    }

    public void setP3(final double p3x, final double p3y) {
        p3.set(new Point2D(p3x, p3y));
    }

    public void setLength(final double newLength) {
        final Point2D[] newPoints = calculatePoints(newLength);
        setP1(newPoints[0]);
        setP2(newPoints[1]);
        setP3(newPoints[2]);
    }

    public void setRotation(final double newRotation) {
        rotation.set(newRotation);
        rotate.setAngle(newRotation);
    }

    // =====================================
    //               GETTERS
    // =====================================

    public Point2D getP1() {
        return p1.get();
    }

    public Point2D getP2() {
        return p2.get();
    }

    public Point2D getP3() {
        return p3.get();
    }

    public Point2D getCenter() {
        return getP1().midpoint(getP2()).midpoint(getP3());
    }

    public double getLength() {
        return getP1().distance(getP2());
    }

    public double getRotation() {
        return rotation.get();
    }
}
