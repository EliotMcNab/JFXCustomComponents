package app.customControls.utilities;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Collection of helper methods to make handling {@link KeyEvent Keyevents} easier and more readable
 */
public class KeyboardUtil {

    public interface Key {

    }

    public static enum Letter implements Key {
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
        ESC,
        SPACE
    }

    private static final HashMap<Letter, KeyCode> keyCodeCorrespondence = new HashMap<>();
    static {
        keyCodeCorrespondence.put(Letter.A, KeyCode.A);
        keyCodeCorrespondence.put(Letter.B, KeyCode.B);
        keyCodeCorrespondence.put(Letter.C, KeyCode.C);
        keyCodeCorrespondence.put(Letter.D, KeyCode.D);
        keyCodeCorrespondence.put(Letter.E, KeyCode.E);
        keyCodeCorrespondence.put(Letter.F, KeyCode.F);
        keyCodeCorrespondence.put(Letter.G, KeyCode.G);
        keyCodeCorrespondence.put(Letter.H, KeyCode.H);
        keyCodeCorrespondence.put(Letter.I, KeyCode.I);
        keyCodeCorrespondence.put(Letter.J, KeyCode.J);
        keyCodeCorrespondence.put(Letter.K, KeyCode.K);
        keyCodeCorrespondence.put(Letter.L, KeyCode.L);
        keyCodeCorrespondence.put(Letter.M, KeyCode.M);
        keyCodeCorrespondence.put(Letter.N, KeyCode.N);
        keyCodeCorrespondence.put(Letter.O, KeyCode.O);
        keyCodeCorrespondence.put(Letter.P, KeyCode.P);
        keyCodeCorrespondence.put(Letter.Q, KeyCode.Q);
        keyCodeCorrespondence.put(Letter.R, KeyCode.R);
        keyCodeCorrespondence.put(Letter.S, KeyCode.S);
        keyCodeCorrespondence.put(Letter.T, KeyCode.T);
        keyCodeCorrespondence.put(Letter.U, KeyCode.U);
        keyCodeCorrespondence.put(Letter.V, KeyCode.V);
        keyCodeCorrespondence.put(Letter.W, KeyCode.W);
        keyCodeCorrespondence.put(Letter.Y, KeyCode.Y);
        keyCodeCorrespondence.put(Letter.X, KeyCode.X);
        keyCodeCorrespondence.put(Letter.Z, KeyCode.Z);
        keyCodeCorrespondence.put(Letter.ENTER, KeyCode.ENTER);
        keyCodeCorrespondence.put(Letter.ESC, KeyCode.ESCAPE);
        keyCodeCorrespondence.put(Letter.SPACE, KeyCode.SPACE);
    }

    private static final HashMap<KeyCode, Letter> lettersCorrespondence = new HashMap<>();
    static {
        lettersCorrespondence.put(KeyCode.A, Letter.A);
        lettersCorrespondence.put(KeyCode.B, Letter.B);
        lettersCorrespondence.put(KeyCode.C, Letter.C);
        lettersCorrespondence.put(KeyCode.D, Letter.D);
        lettersCorrespondence.put(KeyCode.E, Letter.E);
        lettersCorrespondence.put(KeyCode.F, Letter.F);
        lettersCorrespondence.put(KeyCode.G, Letter.G);
        lettersCorrespondence.put(KeyCode.H, Letter.H);
        lettersCorrespondence.put(KeyCode.I, Letter.I);
        lettersCorrespondence.put(KeyCode.J, Letter.J);
        lettersCorrespondence.put(KeyCode.K, Letter.K);
        lettersCorrespondence.put(KeyCode.L, Letter.L);
        lettersCorrespondence.put(KeyCode.M, Letter.M);
        lettersCorrespondence.put(KeyCode.N, Letter.N);
        lettersCorrespondence.put(KeyCode.O, Letter.O);
        lettersCorrespondence.put(KeyCode.P, Letter.P);
        lettersCorrespondence.put(KeyCode.Q, Letter.Q);
        lettersCorrespondence.put(KeyCode.R, Letter.R);
        lettersCorrespondence.put(KeyCode.S, Letter.S);
        lettersCorrespondence.put(KeyCode.T, Letter.T);
        lettersCorrespondence.put(KeyCode.U, Letter.U);
        lettersCorrespondence.put(KeyCode.V, Letter.V);
        lettersCorrespondence.put(KeyCode.W, Letter.W);
        lettersCorrespondence.put(KeyCode.X, Letter.X);
        lettersCorrespondence.put(KeyCode.Y, Letter.Y);
        lettersCorrespondence.put(KeyCode.Z, Letter.Z);
        lettersCorrespondence.put(KeyCode.ENTER, Letter.ENTER);
        lettersCorrespondence.put(KeyCode.ESCAPE, Letter.ESC);
        lettersCorrespondence.put(KeyCode.SPACE, Letter.SPACE);
    }

    public enum Modifier implements Key {
        SHIFT,
        CTRL,
    }

    public static void saveKeyPress(KeyEvent keyEvent, HashSet<Key> keySet) {
        // gets the corresponding Letter
        final Letter letter = lettersCorrespondence.get(keyEvent.getCode());

        // adds the keyCode is it is not already in the key set
        if (!keySet.contains(letter)) keySet.add(letter);
        if (keyEvent.isShiftDown() && !keySet.contains(Modifier.SHIFT)) keySet.add(Modifier.SHIFT);
        if (keyEvent.isControlDown() && !keySet.contains(Modifier.CTRL)) keySet.add(Modifier.CTRL);
    }

    public static void removeKeyPress(KeyEvent keyEvent, HashSet<Key> keySet) {
        // gets the corresponding Letter
        final Letter letter = lettersCorrespondence.get(keyEvent.getCode());

        // tries to remove the letter from the key set
        keySet.remove(letter);
        // tries to remove any modifier from the keySet
        if (keyEvent.isControlDown()) keySet.remove(Modifier.CTRL);
        if (keyEvent.isShiftDown()) keySet.remove(Modifier.SHIFT);
    }

    public static boolean areKeysDown(KeyEvent keyEvent, Key... keys) {
        for (Key key : keys) {
            if (key instanceof Letter && !checkKey(keyEvent, (Letter) key)) return false;
            else if (key instanceof Modifier && !checkModifier(keyEvent, (Modifier) key)) return false;
        }
        return true;
    }

    public static boolean areKeysDown(final HashSet<Key> keyPresses, final Key... keys) {
        for (Key key : keys) {
            if (!keyPresses.contains(key)) return false;
        }
        return true;
    }

    private static boolean checkModifier(KeyEvent keyEvent, Modifier modifier) {
        return switch (modifier) {
            case CTRL -> keyEvent.isControlDown();
            case SHIFT -> keyEvent.isShiftDown();
        };
    }

    private static boolean checkKey(KeyEvent keyEvent, Letter key) {
        return keyEvent.getCode().equals(keyCodeCorrespondence.get(key));
    }
}
