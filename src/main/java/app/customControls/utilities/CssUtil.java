package app.customControls.utilities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Node;

public class CssUtil {

    public static BooleanProperty pseudoClassProperty(
            final PseudoClass pseudoClass,
            final Node bean,
            final String name
    ) {
        return new BooleanPropertyBase() {
            @Override
            protected void invalidated() {
                bean.pseudoClassStateChanged(pseudoClass, get());
            }

            @Override
            public Object getBean() {
                return bean;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

}
