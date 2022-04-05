package app.customControls;

import javafx.scene.ImageCursor;
import javafx.scene.image.Image;

public class Images {

    public static final String IMAGE_PATH = "/app/customControls/pictures/";

    public static class Icons {

        public static final Image MOVE_ICON = new Image(Images.class.getResource(IMAGE_PATH + "move.png").toExternalForm());

    }

    public static class Cursor {

        public static final ImageCursor MOVE_CURSOR = new ImageCursor(Icons.MOVE_ICON);

    }

}
