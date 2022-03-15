package app.customControls.utilities;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Collection of helper methods for the system clipboard
 */
public class ClipBoardUtil {

    public static void copy(final String string) {

        Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();

        content.putString(string);

        clipboard.setContent(content);
    }

    public static String getClipboardContent() {
        return Clipboard.getSystemClipboard().getString();
    }
}
