package app.customControls.utilities;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;

import java.awt.*;

/**
 * Collection of helper methods used to get info concerning the system screen
 */
public class ScreenUtil {

    public static Rectangle2D getScreenSize() {
        return Screen.getPrimary().getBounds();
    }

    public static double getScreenWidth() {
        return getScreenSize().getWidth();
    }

    public static double getScreenHeight() {
        return getScreenSize().getHeight();
    }

    public static GraphicsConfiguration defaultGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    public static int getTaskbarSize() {
        return Toolkit.getDefaultToolkit().getScreenInsets(defaultGraphicsConfiguration()).bottom;
    }

    public static Point2D getMousePosition() {
        return new Robot().getMousePosition();
    }

    public static WritableImage getScreenShot() {
        final Rectangle2D screenShotSize = new Rectangle2D(0, 0, getScreenWidth(), getScreenHeight());
        return new Robot().getScreenCapture(null, screenShotSize);
    }

}
