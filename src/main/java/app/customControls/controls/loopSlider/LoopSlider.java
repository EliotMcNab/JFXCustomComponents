package app.customControls.controls.loopSlider;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A slider which loops around when the shift key is pressed<br>
 * @implNote Same styleable properties as a normal slider but <strong>can only be horizontal for the moment</strong>
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

    // properties
    private DoubleProperty minValue;
    private DoubleProperty maxValue;
    private DoubleProperty value;

    // looping
    private BooleanProperty doesLoop;

    // =========================================
    //               CONSTRUCTOR
    // =========================================

    public LoopSlider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_VALUE);
    }

    public LoopSlider(final double minValue, final double maxValue, final double startValue) {
        this(minValue, maxValue, startValue, true);
    }

    public LoopSlider(final double minValue, final double maxValue, final double startValue, final boolean doesLoop) {

        setMin(minValue);
        setMax(maxValue);
        setValue(startValue);
        setLooping(doesLoop);

        initialise();
    }

    // =========================================
    //              INITIALISING
    // =========================================

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
    public DoubleProperty minProperty() {
        if (minValue == null) {
            minValue = new DoublePropertyBase(DEFAULT_MIN) {
                @Override
                public Object getBean() {
                    return LoopSlider.this;
                }

                @Override
                public String getName() {
                    return "min";
                }
            };
        }

        return minValue;
    }

    public DoubleProperty maxProperty() {
        if (maxValue == null) {
            maxValue = new DoublePropertyBase(DEFAULT_MAX) {
                @Override
                public Object getBean() {
                    return LoopSlider.this;
                }

                @Override
                public String getName() {
                    return "max";
                }
            };
        }

        return maxValue;
    }

    public DoubleProperty valueProperty() {
        if (value == null) {
            value = new DoublePropertyBase(DEFAULT_VALUE) {
                @Override
                public Object getBean() {
                    return LoopSlider.this;
                }

                @Override
                public String getName() {
                    return "value";
                }
            };
        }

        return value;
    }

    public BooleanProperty loopingProperty() {
        if (doesLoop == null) {
            doesLoop = new BooleanPropertyBase() {
                @Override
                public Object getBean() {
                    return LoopSlider.this;
                }

                @Override
                public String getName() {
                    return "loop";
                }
            };
        }
        return doesLoop;
    }

    /*                 VALUES                 */
    public void setMin(double min) {
        minProperty().set(min);
    }

    public double getMin() {
        return minProperty().get();
    }

    public void setMax(double max) {
        maxProperty().set(max);
    }

    public double getMax() {
        return maxProperty().get();
    }

    public void setValue(double value) {
        if (value >= minProperty().get() && value <= maxProperty().get()) valueProperty().set(value);
    }

    public double getValue() {
        return value.get();
    }

    public void setLooping(boolean doesLoop) {
        loopingProperty().set(doesLoop);
    }

    public boolean doesLoop() {
        return loopingProperty().get();
    }

}
