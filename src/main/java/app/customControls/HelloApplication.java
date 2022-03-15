package app.customControls;

import app.customControls.controls.colorPicker.MaterialColorPicker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // sliders test
        /*BorderPane root = new BorderPane();
        root.setId("BACKGROUND");
        LoopSlider loopSlider1 = new LoopSlider(0, 100, 0, false);
        LoopSlider loopSlider2 = new LoopSlider(0, 100, 0);
        BorderPane.setMargin(loopSlider1, new Insets(20, 10, 20, 10));
        BorderPane.setMargin(loopSlider2, new Insets(10));
        loopSlider1.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println(newValue.doubleValue());
        });
        loopSlider2.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            System.out.println(newValue.doubleValue());
        });
        root.setCenter(loopSlider1);
        root.setBottom(loopSlider2);*/

        // border lines test
        /*BorderPane root = new BorderPane();
        BorderLine corner = new BorderLine(Orientation.TOP_LEFT);
        BorderPane.setMargin(corner, new Insets(10));
        root.setCenter(corner);*/

        // border frame test
        /*BorderPane root = new BorderPane();
        BorderFrame frame = new BorderFrame(50, 10);
        BorderPane.setMargin(frame, new Insets(10));
        frame.setText("press ESC to cancel");
        root.setCenter(frame);*/

        // hsv color select test
        /*BorderPane root = new BorderPane();
        HsvColorSelect hsvColorSelect = new HsvColorSelect(200, 200);
        root.setCenter(hsvColorSelect);

        final InvalidationListener displayMovement = observable -> {

            // determines the pointer's current hue and saturation
            final double pointerHue        = hsvColorSelect.getPointerHue();
            final double pointerSaturation = hsvColorSelect.getPointerSaturation();
            // determines the color selector's value
            final double value             = hsvColorSelect.getValue();

            // generates the string representing the color picker's current hsv
            final String hsv               = ColorUtil.Hsv.hsvString(pointerHue, pointerSaturation, value);

            // displays current hsv
            System.out.println(hsv);

        };

        hsvColorSelect.pointerLayoutXProperty().addListener(displayMovement);
        hsvColorSelect.pointerLayoutYProperty().addListener(displayMovement);
*/

        // color picker test
        BorderPane root = new BorderPane();
        MaterialColorPicker colorPicker = new MaterialColorPicker();
        root.setCenter(colorPicker);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app/customControls/style/app-style.css").toExternalForm());

        // HsvGradient test = new HsvGradient(200, 120, 350, 100, 100);
        // final int[] slice = test.sliceOf(350);
        // test.displayInConsole(slice);

        stage.setTitle("Color Picker");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}