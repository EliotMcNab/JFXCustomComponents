package app.customControls.Animations;

/**
 * Functional interface for handling interpolation<br>
 * @implNote see <a href="https://en.wikipedia.org/wiki/Interpolation">wikipedia</a> for more info about interpolation
 */
@FunctionalInterface
public interface Interpolation {
    double interpolateAt(final double x);
}
