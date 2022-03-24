package app.customControls.controls.sizePanel;

import app.customControls.controls.shapes.Arrow;
import app.customControls.controls.shapes.Orientation;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;

public class SizePanelSkin extends SkinBase<SizePanel> implements Skin<SizePanel> {

    // =====================================
    //                FIELDS
    // =====================================

    // constants

    private static final String CSS_LOCATION = "/app/customControls/style/size-panel.css";

    // components

    private final Arrow topLeft;
    private final Arrow top;
    private final Arrow topRight;
    private final Arrow right;
    private final Arrow bottomRight;
    private final Arrow bottom;
    private final Arrow bottomLeft;
    private final Arrow left;

    // associated size panel

    private final SizePanel sizePanel;

    // =====================================
    //              CONSTRUCTOR
    // =====================================

    protected SizePanelSkin(SizePanel sizePanel) {
        super(sizePanel);

        // saves associated size panel

        this.sizePanel = sizePanel;

        // initialising components

        final double arrowWidth = sizePanel.getArrowWidth();
        final double arrowHeight = sizePanel.getArrowHeight();

        this.topLeft = new Arrow(arrowWidth, arrowHeight, Orientation.TOP_LEFT);
        this.top = new Arrow(arrowWidth, arrowHeight, Orientation.TOP);
        this.topRight = new Arrow(arrowWidth, arrowHeight, Orientation.TOP_RIGHT);
        this.right = new Arrow(arrowWidth, arrowHeight, Orientation.RIGHT);
        this.bottomRight = new Arrow(arrowWidth, arrowHeight, Orientation.BOTTOM_RIGHT);
        this.bottom = new Arrow(arrowWidth, arrowHeight, Orientation.BOTTOM);
        this.bottomLeft = new Arrow(arrowWidth, arrowHeight, Orientation.BOTTOM_LEFT);
        this.left = new Arrow(arrowWidth, arrowHeight, Orientation.LEFT);

        // initialisation
        style();
        populate();
        reposition();
    }

    // =====================================
    //            INITIALISATION
    // =====================================

    private void style() {
        sizePanel.getStyleClass().add("size-panel");
        try {
            String css = SizePanel.class.getResource(CSS_LOCATION).toExternalForm();
            sizePanel.getStylesheets().add(css);
        } catch (NullPointerException e) {
            System.out.printf("Impossible to add stylesheet at location %s\n", CSS_LOCATION);
        }
    }

    private void populate() {
        getChildren().addAll(
                topLeft,
                top,
                topRight,
                right,
                bottomRight,
                bottom,
                bottomLeft,
                left
        );
    }

    // =====================================
    //             REPOSITION
    // =====================================

    private void reposition() {

    }

}
