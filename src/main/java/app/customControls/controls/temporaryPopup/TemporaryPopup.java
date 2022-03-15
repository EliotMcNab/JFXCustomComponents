package app.customControls.controls.temporaryPopup;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * A {@link Label} which appears for a set amount of time before disappearing<br>
 * <br>
 * <u><i>Substructure</i></u><br>
 * <ul>
 *    <li>popup: {@link HBox}</li>
 *    <ul>
 *        <li>popup-text: {@link Label}</li>
 *    </ul>
 * </ul>
 *
 * @implNote The {@link TemporaryPopup} takes up the parent {@link javafx.scene.Scene Scene}'s stylesheet, so any styling
 * has to take place there
 */
public class TemporaryPopup extends Popup {

    private final Label text = new Label();
    private final HBox backdrop = new HBox();
    private long lifeCycle = 0;

    PauseTransition waitForCycleEnd;

    public TemporaryPopup() {
        this("", 0);
    }

    public TemporaryPopup(String text, final long lifeCycle) {
        super();

        setText(text);
        setLifeCycle(lifeCycle);

        format();
        populate();
        style();
    }

    private void format() {
        HBox.setMargin(text, new Insets(10, 20, 10, 20));
    }

    private void populate() {

        backdrop.setOpacity(0.5);
        backdrop.setAlignment(Pos.CENTER);
        backdrop.getChildren().add(text);

        getContent().add(backdrop);
    }

    private void style() {
        backdrop.getStyleClass().add("popup");
        text.getStyleClass().add("popup-text");
    }

    public void setText(String newText) {
        text.setText(newText);
    }

    public void setLifeCycle(final long lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public void setOpacity(Double opacity) {
        backdrop.setOpacity(opacity);
    }

    public void closeOnCycleEnd() {
        waitForCycleEnd = new PauseTransition(Duration.millis(lifeCycle));
        waitForCycleEnd.setOnFinished(actionEvent -> hide());
        waitForCycleEnd.play();
    }

    @Override
    public void show(Window window) {
        if (isShowing()) return;
        super.show(window);
        closeOnCycleEnd();
    }

    @Override
    public void show(Window window, final double x, final double y) {
        if (isShowing()) return;
        super.show(window, x, y);
        closeOnCycleEnd();
    }

    @Override
    public void show(Node node, final double x, final double y) {
        if (isShowing()) return;
        super.show(node, x, y);
        closeOnCycleEnd();
    }
}
