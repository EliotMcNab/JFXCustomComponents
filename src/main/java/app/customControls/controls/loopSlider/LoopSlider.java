package app.customControls.controls.loopSlider;

import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A slider which loops around when the shift key is pressed<br>
 * <br>
 * <u><i>CSS Pseudo-class</i></u> : loop-slider<br>
 * <br>
 * <u><i>CSS Styleable properties</i></u> :<br>
 * <ul>
 *     <li>can be style the same way as a normal {@link javafx.scene.control.Slider Slider}</li>
 * </ul>
 * <u><i>Substructure</i></u> :<br>
 * <ul>
 *     <li>track: {@link javafx.scene.layout.Region Region}</li>
 *     <li>thumb: {@link javafx.scene.layout.Region Region}</li>
 * </ul>
 * @implNote only support <strong>horizontal sliders</strong> for the moment
 * @see LoopSliderSkin LoopSliderSkin
 */
public class LoopSlider extends Control {

    // =========================================
    //                 FIELDS
    // =========================================

    // default values

    private static final double DEFAULT_MIN = 0;
    private static final double DEFAULT_MAX = 100;
    private static final double DEFAULT_VALUE = 0;
    private static final boolean LOOP_DEFAULT = true;

    // properties
    private final DoubleProperty minValue;
    private final DoubleProperty maxValue;
    private final DoubleProperty value;

    // looping
    private final BooleanProperty doesLoop;

    // =========================================
    //               CONSTRUCTOR
    // =========================================

    /**
     * Default {@link LoopSlider} constructor
     */
    public LoopSlider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_VALUE);
    }

    /**
     * {@link LoopSlider} constructor
     * @param minValue (double): minimal slider value possible
     * @param maxValue (double): max slider value possible
     * @param startValue (double): initial value the slider starts at
     */
    public LoopSlider(final double minValue, final double maxValue, final double startValue) {
        this(minValue, maxValue, startValue, LOOP_DEFAULT);
    }

    /**
     * {@link LoopSlider} constructor
     * @param minValue (double): minimal slider value possible
     * @param maxValue (double): max slider value possible
     * @param startValue (double): initial value the slider starts at
     * @param doesLoop (boolean): whether the slider loops back on itself or not
     */
    public LoopSlider(final double minValue, final double maxValue, final double startValue, final boolean doesLoop) {

        // initialises properties
        this.minValue = new SimpleDoubleProperty(this, "min", DEFAULT_MIN);
        this.maxValue = new SimpleDoubleProperty(this, "max", DEFAULT_MAX);
        this.value = new SimpleDoubleProperty(this, "val", DEFAULT_VALUE);
        this.doesLoop = new SimpleBooleanProperty(this, "loop", LOOP_DEFAULT);

        // sets value specified in constructor
        setMin(minValue);
        setMax(maxValue);
        setValue(startValue);
        setLooping(doesLoop);

        initialise();
    }

    // =========================================
    //              INITIALISING
    // =========================================

    /**
     * Adds the necessary slider classes
     */
    private void initialise() {
        getStyleClass().addAll("loop-slider");
    }

    // =========================================
    //                 STYLE
    // =========================================

    @Override
    protected Skin<LoopSlider> createDefaultSkin() {
        return new LoopSliderSkin(LoopSlider.this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return LoopSlider.class.getResource("/app/customControls/style/loop-slider.css").toExternalForm();
    }

    // =========================================
    //               ACCESSORS
    // =========================================

    /*               PROPERTIES               */

    /**
     * Min value {@link Property} for the {@link LoopSlider} (updates whenever the min value is changed)
     * @return (DoubleProperty): LoopSlider min value property
     */
    public DoubleProperty minProperty() {
        return minValue;
    }

    /**
     * Max value {@link Property} for the {@link LoopSlider} (updates whenever the max value is changed)
     * @return (DoubleProperty): LoopSlider max value property
     */
    public DoubleProperty maxProperty() {
        return maxValue;
    }

    /**
     * Current value {@link Property} for the {@link LoopSlider} (updates whenever the current value changes)
     * @return (DoubleProperty): LoopSlider current value property
     */
    public DoubleProperty valueProperty() {
        return value;
    }

    /**
     * Looping state {@link Property} for the {@link LoopSlider} (updates whenever the LoopSlider is set to loop)
     * @return (BooleanProperty): LoopSlider looping state property
     */
    public BooleanProperty loopingProperty() {
        return doesLoop;
    }

    /*                 VALUES                 */

    /**
     * Setter for the {@link LoopSlider}'s min valyue
     * @param min (double): the slider's new min value
     */
    public void setMin(double min) {
        minProperty().set(min);
    }

    /**
     * Getter for the {@link LoopSlider}'s min value
     * @return (double): the LoopSlider's min value
     */
    public double getMin() {
        return minProperty().get();
    }

    /**
     * Setter for the {@link LoopSlider}'s max value
     * @param max (double): the slider's new max value
     */
    public void setMax(double max) {
        maxProperty().set(max);
    }

    /**
     * Getter for the {@link LoopSlider}'s max value
     * @return (double): the LoopSlider's max value
     */
    public double getMax() {
        return maxProperty().get();
    }

    /**
     * Setter for the {@link LoopSlider}'s value
     * @param value (double); the LoopSlider's new value
     */
    public void setValue(double value) {
        // verifies that the new value is in the range of the LoopSlider's min & max values
        if (value >= minProperty().get() && value <= maxProperty().get()) valueProperty().set(value);
    }

    /**
     * Getter for the {@link LoopSlider}'s value
     * @return (double): the LoopSlider's current value
     */
    public double getValue() {
        return value.get();
    }

    /**
     * Setter for the {@link LoopSlider}'s ability to loop
     * @param doesLoop (boolean): whether the slider can loop
     */
    public void setLooping(boolean doesLoop) {
        loopingProperty().set(doesLoop);
    }

    /**
     * Getter for the {@link LoopSlider}'s ability to loop
     * @return (boolean): whether the slider can loop
     */
    public boolean doesLoop() {
        return loopingProperty().get();
    }

}
