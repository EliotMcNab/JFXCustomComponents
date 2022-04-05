package app.customControls;

import app.customControls.controls.movementPane.MovementPane;
import app.customControls.controls.resizePanel.ResizePanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
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
        /*BorderPane root = new BorderPane();
        MaterialColorPicker colorPicker = new MaterialColorPicker();
        root.setCenter(colorPicker);*/

        // movement pane test
        /*final BorderPane root = new BorderPane();
        final MovementPane movementPane = new MovementPane();
        Platform.runLater(() -> movementPane.setAssociatedNode(new Rectangle(200, 200, Color.GRAY)));
        root.setCenter(movementPane);*/

        // triangle test
        /*final BorderPane root = new BorderPane();
        final Triangle triangle = new Triangle();
        root.setCenter(triangle);*/

        // arrow test
        /*final AnchorPane root = new AnchorPane();
        final Arrow arrow1 = new Arrow(25, 10);
        final Arrow arrow2 = new Arrow(25, 10);
        arrow1.setColor(Color.RED);
        arrow2.setOrientation(Orientation.TOP_LEFT);
        arrow2.setColor(Color.BLUE);
        Platform.runLater(() -> {
            arrow2.moveTo(arrow2.getDisplayWidth(), arrow2.getDisplayHeight());
            arrow1.moveTo(arrow2.getDisplayWidth(), arrow2.getDisplayHeight());
        });
        root.getChildren().addAll(
                arrow1,
                arrow2
        );*/

        // resize panel test
        /*final AnchorPane root = new AnchorPane();
        final ResizePanel sizePanel = new ResizePanel();
        final Rectangle rectangle = new Rectangle(40, 40, Color.BLUE);
        sizePanel.setLayoutX(100);
        sizePanel.setLayoutY(100);
        Platform.runLater(() -> sizePanel.setResizeNode(rectangle));
        root.getChildren().add(sizePanel);

        root.setOnKeyPressed(keyEvent -> {
            final double centerX = rectangle.getWidth() / 2;
            final double centerY = rectangle.getHeight() / 2;
            if (keyEvent.getCode().equals(KeyCode.SPACE)) sizePanel.setZoom(2, 2, centerX, centerY);
            else                                          sizePanel.setZoom(1, 1, centerX, centerY);
        });*/

        final BorderPane root = new BorderPane();
        final MovementPane movementPane = new MovementPane();
        final Rectangle rectangle = new Rectangle(20, 20, Color.BLUE);
        Platform.runLater(() -> movementPane.setMovementNode(new Rectangle(200, 100, Color.BLUE)));
        root.setCenter(movementPane);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app/customControls/style/app-style.css").toExternalForm());

        // HsvGradient test = new HsvGradient(200, 120, 350, 100, 100);
        // final int[] slice = test.sliceOf(350);
        // test.displayInConsole(slice);

        stage.setTitle("Custom Controls");
        stage.setMinWidth(500);
        stage.setMinHeight(300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}