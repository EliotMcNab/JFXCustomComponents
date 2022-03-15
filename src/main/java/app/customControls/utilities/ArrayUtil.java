package app.customControls.utilities;

/**
 * Collection of helper methods for arrays
 */
public class ArrayUtil {
    public static <T> T valueAt(final T[] array,
                                final int width,
                                final int height,
                                final int x,
                                final int y) {
        // checks that the specified array is not null
        validateArray(array, x, y);

        // checks that x and y coordinates are bound within the specified width and height
        validateCoordinates(array, width, height, x, y);

        // determines the position of the value in the array
        final int position              = width * y + x;

        // checks that the specified array contains a value at that position
        validatePosition(array, width, height, x, y, position);

        // if all values are valid, returns the value at the specified position
        return array[position];
    }

    public static <T>void setValueAt(final T[] array,
                                     final int width,
                                     final int height,
                                     final int x,
                                     final int y,
                                     final T value) {
        // checks that the specified array is not null
        validateArray(array, x, y);

        // checks that x and y coordinates are bound within the specified width and height
        validateCoordinates(array, width, height, x, y);

        // determines the position of the value in the array
        final int position              = width * y + x;

        // checks that the specified array contains a value at that position
        validatePosition(array, width, height, x, y, position);

        // if all values are valid, sets the value at the specified position
        array[position]                 = value;

    }

    // ===================================
    //             VALIDATION
    // ===================================

    private static <T> boolean validateArray(final T[] array,
                                             final int x,
                                             final int y) {
        final boolean invalidArray      = array == null;
        if (invalidArray) {
            final String errorMessage   = "array %s cannot be null to access value at coordinates x %s and y %s";
            throw new IllegalArgumentException(String.format(errorMessage, array, x, y));
        }
        return true;
    }

    private static <T> boolean validateCoordinates(final T[] array,
                                                   final int width,
                                                   final int height,
                                                   final int x,
                                                   final int y) {
        final boolean invalidX          = x < 0 || x > width;
        final boolean invalidY          = y < 0 || y > height;
        if (invalidX || invalidY) {
            final String errorMessage   = "x:%s or y:%s cannot be greater than width %s or height %s or inferior to 0";
            throw new IllegalArgumentException(String.format(errorMessage, x, y, width, height));
        }

        return true;
    }

    private static <T> boolean validatePosition(final T[] array,
                                                final int width,
                                                final int height,
                                                final int x,
                                                final int y,
                                                final int position) {
        final int arraySize             = array.length - 1;
        final boolean invalidPosition   = position > arraySize;
        if (invalidPosition) {
            final String errorMessage   = "x:%s and y:%s specify position %s outside of array bounds (max %s)";
            throw new IllegalArgumentException(String.format(errorMessage, x, y, position, arraySize));
        }
        return true;
    }
}
