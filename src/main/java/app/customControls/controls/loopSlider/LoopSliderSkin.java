package app.customControls.controls.loopSlider;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Handles display logic for the {@link LoopSlider} class
 */
public class LoopSliderSkin extends SkinBase<LoopSlider> implements Skin<LoopSlider> {

    // =======================================
    //                 FIELDS
    // =======================================

    // size constants
    //preferred

    private static final double PREF_TRACK_WIDTH = 200;
    private static final double PREF_TRACK_HEIGHT = 10;
    private static final double PREF_THUMB_HEIGHT = 20;

    // bounds

    private static final double MIN_TRACK_WIDTH = 100;
    private static final double MIN_TRACK_HEIGHT = 10;

    // components
    private Region track;
    private Region thumb;
    private Pane pane;

    // listeners

    private final InvalidationListener sizeListener;
    private final InvalidationListener valueUpdateListener;
    private final EventHandler<MouseEvent> focusListener;
    private final EventHandler<MouseEvent> dragListener;
    private final EventHandler<MouseEvent> valueClickListener;

    // paired control
    private LoopSlider loopSlider;

    // =======================================
    //              CONSTRUCTOR
    // =======================================

    /**
     * Default constructor for the LoopSliderSkin class
     * @param loopSlider (LoopSlider): LoopSlider which is associated to the class
     */
    protected LoopSliderSkin(LoopSlider loopSlider) {
        super(loopSlider);

        // saves the associated LoopSlider
        this.loopSlider = loopSlider;
        // initialises listeners
        this.sizeListener = observable -> repositionComponents();
        this.valueUpdateListener = observable -> repositionThumb();
        this.focusListener = this::focusOnClick;
        this.dragListener = this::calculateThumbPosition;
        this.valueClickListener = this::updateThumbOnTrackClick;

        initialiseGraphics();
        registerListeners();
    }

    // =======================================
    //            INITIALISATION
    // =======================================

    /**
     * Handles the creation of the LoopSlider's components
     */
    private void initialiseGraphics() {

        // track
        track = new Region();
        track.getStyleClass().setAll("track");
        track.setPrefSize(PREF_TRACK_WIDTH, PREF_TRACK_HEIGHT);
        track.setMinSize(MIN_TRACK_WIDTH, MIN_TRACK_HEIGHT);

        // thumb
        thumb = new Region();
        thumb.getStyleClass().setAll("thumb");
        thumb.setPrefSize(PREF_THUMB_HEIGHT, PREF_THUMB_HEIGHT);

        pane = new Pane(track, thumb);

        // populating
        getChildren().add(pane);

        // positioning
        Platform.runLater(this::repositionComponents);

    }

    /**
     * Handles the registration of listeners for the LoopSlider's components
     */
    private void registerListeners() {

        // resizing
        loopSlider.widthProperty().addListener(sizeListener);
        loopSlider.heightProperty().addListener(sizeListener);

        // mouse presses
        track.setOnMousePressed(valueClickListener);
        thumb.setOnMousePressed(focusListener);

        // thumb positioning
        thumb.setOnMouseDragged(dragListener);

        // value changes
        loopSlider.valueProperty().addListener(valueUpdateListener);

    }

    /**
     * Gives the LoopSlider focus when it is clicked
     * @param mouseEvent (MouseEvent): MouseEvent resulting from the click
     */
    private void focusOnClick(MouseEvent mouseEvent) {
        loopSlider.requestFocus();
    }

    /**
     * Cleans up the LoopSliderSkin when it is no longer needed
     */
    @Override
    public void dispose() {
        loopSlider.widthProperty().removeListener(sizeListener);
        loopSlider.heightProperty().removeListener(sizeListener);
        loopSlider = null;
    }

    // =======================================
    //           TRACK POSITIONING
    // =======================================

    /**
     * Determines where to position the LoopSlider's thumb when it is dragged
     * @param mouseEvent (MouseEvent): drag event resulting from dragging the thumb
     */
    private void calculateThumbPosition(MouseEvent mouseEvent) {

        // mouse position
        double x = thumb.localToParent(mouseEvent.getX(), 0).getX();

        // adjustments
        x -= thumb.getWidth() / 2;
        if (loopSlider.doesLoop() && mouseEvent.isShiftDown())  x = loop(x);
        else                                                    x = bound(x);

        // update slider value
        final double value = sliderProgress(x);
        loopSlider.setValue(value);
    }

    private void updateThumbOnTrackClick(MouseEvent mouseEvent) {

        focusOnClick(mouseEvent);

        double x = mouseEvent.getX();
        double progression = x / track.getWidth();
        double value = progression * loopSlider.getMax();

        loopSlider.setValue(value);
    }

    /**
     * Loops the thumb around
     * @param x (double): x-coordinates of the thumb
     * @return (double): coordinates of the thumb % track width
     */
    private double loop(double x) {
        if (x < 0) x += adjustedWidth() * (int) ((-x / adjustedWidth()) + 1);
        return x % adjustedWidth();
    }

    /**
     * Bounds the thumb between then edges of the track (thumb does not loop)
     * @param x (double): x-coordinates of the thumb
     * @return (double): bound coordinates of the thumb
     */
    private double bound(double x) {
        if (x < 0) return 0;
        return Math.min(x, adjustedWidth());
    }

    /**
     * Converts the thumbs coordinates to progression along the LoopSlider
     * @param x (double): x-coordinates of the thumb
     * @return (double): value associated to the thumb along the LoopSlider
     */
    private double sliderProgress(double x) {
        return x / adjustedWidth() * loopSlider.getMax();
    }

    /**
     * Calculates the available width of the track when taking into account the width of the thumb
     * @return (double): available width on the track
     */
    private double adjustedWidth() {
        return track.getWidth() - thumb.getWidth();
    }

    // =======================================
    //                RESIZING
    // =======================================

    /**
     * Handles track & thumb resizing and repositioning
     */
    private void repositionComponents() {
        repositionTrack();

        // thumb resizing occurs after track has been resized to take into account new size
        Platform.runLater(this::repositionThumb);
    }

    /**
     * Handles resizing and repositioning the track
     */
    private void repositionTrack() {
        // calculates the available width of the LoopSlider
        final Insets insets = loopSlider.getInsets();
        final double width  = loopSlider.getWidth() - insets.getLeft() - insets.getRight();

        // avoids situations where width is negative
        if (width < 0) return;

        // centers the track vertically
        final double trackY = (loopSlider.getHeight() - track.getHeight()) / 2;
        track.setLayoutY(trackY);
        // grows the track to fit the loopSlider
        track.setMaxWidth(width);
        track.setPrefWidth(width);
    }

    /**
     * Handles resizing and repositioning the thumb
     */
    private void repositionThumb() {
        // centers the thumb vertically
        final double thumbX = loopSlider.getValue() / loopSlider.getMax() * adjustedWidth();
        // System.out.println(adjustedWidth());
        thumb.setLayoutX(thumbX);

        // sets the thumb's progression along the LoopSlider
        final double thumbY = (loopSlider.getHeight() - thumb.getHeight()) / 2;
        thumb.setLayoutY(thumbY);
    }

}
