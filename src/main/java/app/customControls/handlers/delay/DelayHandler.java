package app.customControls.handlers.delay;

/**
 * Acts as a looping counter to check if a specific delay has elapsed
 */
public class DelayHandler {

    // ===================================
    //               FIELDS
    // ===================================

    private long delayDuration;
    private long lastCycle = 0;

    // ===================================
    //            CONSTRUCTOR
    // ===================================

    public DelayHandler(final long delayDuration) {
        this.delayDuration = delayDuration;
    }

    // ===================================
    //              METHODS
    // ===================================

    public boolean hasElapsed() {
        if (System.currentTimeMillis() - lastCycle >= delayDuration) {
            lastCycle = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void reset() {
        lastCycle = 0;
    }

    // ===================================
    //             ACCESSORS
    // ===================================

    public long getDelayDuration() {
        return delayDuration;
    }

    public void setDelayDuration(final long newDuration) {
        if (newDuration < 0) return;
        delayDuration = newDuration;
    }

}
