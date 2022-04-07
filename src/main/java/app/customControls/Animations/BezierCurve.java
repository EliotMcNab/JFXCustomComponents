package app.customControls.Animations;

public class BezierCurve implements Interpolation {

    private final double aY;
    private final double bY;
    private final double cY;
    private final double tMax;

    public BezierCurve(
            final double aY,
            final double bY,
            final double cY,
            final double tMax
    ) {
        this.aY = aY;
        this.bY = bY;
        this.cY = cY;
        this.tMax = tMax;
    }

    @Override
    public double interpolateAt(double x) {
        final double t = x / tMax;
        return (1 - t) * (1 - t) * aY + 2 * (1 - t) * t * bY + t * t * cY;
    }
}
