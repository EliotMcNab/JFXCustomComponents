package app.customControls.Animations;

import java.util.function.Function;

/**
 * {@link Interpolation} class with a gradient start and a gradient end
 * @implNote see <a href="https://www.desmos.com/calculator/gq88zdiqx8">desmos</a> for a graphical visualisation
 * of the function used and how changes in parameters effects the final result
 */
public class BezierGradient implements Interpolation {

    // interpolation value as of which f1 is swapped for f2
    private final double cutoff;

    // function for the 1st part of the gradient
    // a Bézier curve with a decreasing slope
    private final Function<Double, Double> f1;
    // function for the 2nd part of the gradient
    // a Bézier curve with an increasing slope
    private final Function<Double, Double> f2;

    public BezierGradient(
            final double min,
            final double pivot,
            final double max,
            final double cutoff,
            final double tMax
    ) {
        // saves the cutoff
        this.cutoff = cutoff;

        // initialises function
        // see https://www.desmos.com/calculator/ypmes48pio for a visual representation of each function
        // and how they vary under different parameters
        this.f1 = x -> max + (max-pivot) * ((x / cutoff) * (x / cutoff) - 2 * (x / cutoff));
        this.f2 = x -> pivot - ((x - cutoff) / (tMax - cutoff)) * ((x - cutoff) / (tMax - cutoff)) * (pivot - min);
    }

    @Override
    public double interpolateAt(double x) {
        // interpolates using function 1 till cutoff value, then uses function 2
        return x <= cutoff ? f1.apply(x) : f2.apply(x);
    }
}
