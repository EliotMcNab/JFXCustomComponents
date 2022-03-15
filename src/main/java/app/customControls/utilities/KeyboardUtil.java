package app.customControls.utilities;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;

/**
 * Collection of helper methods to make handling {@link KeyEvent Keyevents} easier and more readable
 */
public class KeyboardUtil {

    public interface Key {

    }

    public static enum Letters implements Key {
        A,
        Z,
        E,
        R,
        T,
        Y,
        U,
        I,
        O,
        P,
        Q,
        S,
        D,
        F,
        G,
        H,
        J,
        K,
        L,
        M,
        W,
        X,
        C,
        V,
        B,
        N,
        ENTER,
        ESC
    }

    private static final HashMap<Letters, KeyCode> keyCodeCorrespondence = new HashMap<>();
    static {
        keyCodeCorrespondence.put(Letters.A, KeyCode.A);
        keyCodeCorrespondence.put(Letters.B, KeyCode.B);
        keyCodeCorrespondence.put(Letters.C, KeyCode.C);
        keyCodeCorrespondence.put(Letters.D, KeyCode.D);
        keyCodeCorrespondence.put(Letters.E, KeyCode.E);
        keyCodeCorrespondence.put(Letters.F, KeyCode.F);
        keyCodeCorrespondence.put(Letters.G, KeyCode.G);
        keyCodeCorrespondence.put(Letters.H, KeyCode.H);
        keyCodeCorrespondence.put(Letters.I, KeyCode.I);
        keyCodeCorrespondence.put(Letters.J, KeyCode.J);
        keyCodeCorrespondence.put(Letters.K, KeyCode.K);
        keyCodeCorrespondence.put(Letters.L, KeyCode.L);
        keyCodeCorrespondence.put(Letters.M, KeyCode.M);
        keyCodeCorrespondence.put(Letters.N, KeyCode.N);
        keyCodeCorrespondence.put(Letters.O, KeyCode.O);
        keyCodeCorrespondence.put(Letters.P, KeyCode.P);
        keyCodeCorrespondence.put(Letters.Q, KeyCode.Q);
        keyCodeCorrespondence.put(Letters.R, KeyCode.R);
        keyCodeCorrespondence.put(Letters.S, KeyCode.S);
        keyCodeCorrespondence.put(Letters.T, KeyCode.T);
        keyCodeCorrespondence.put(Letters.U, KeyCode.U);
        keyCodeCorrespondence.put(Letters.V, KeyCode.V);
        keyCodeCorrespondence.put(Letters.W, KeyCode.W);
        keyCodeCorrespondence.put(Letters.Y, KeyCode.Y);
        keyCodeCorrespondence.put(Letters.X, KeyCode.X);
        keyCodeCorrespondence.put(Letters.Z, KeyCode.Z);
        keyCodeCorrespondence.put(Letters.ENTER, KeyCode.ENTER);
        keyCodeCorrespondence.put(Letters.ESC, KeyCode.ESCAPE);
    }

    public enum Modifier implements Key {
        SHIFT,
        CTRL,
    }

    public static boolean areKeysDown(KeyEvent keyEvent, Key... keys) {
        for (Key key : keys) {
            if (key instanceof Letters && !checkKey(keyEvent, (Letters) key)) return false;
            else if (key instanceof Modifier && !checkModifier(keyEvent, (Modifier) key)) return false;
        }
        return true;
    }

    private static boolean checkModifier(KeyEvent keyEvent, Modifier modifier) {
        return switch (modifier) {
            case CTRL -> keyEvent.isControlDown();
            case SHIFT -> keyEvent.isShiftDown();
        };
    }

    private static boolean checkKey(KeyEvent keyEvent, Letters key) {
        return keyEvent.getCode().equals(keyCodeCorrespondence.get(key));
    }
}
