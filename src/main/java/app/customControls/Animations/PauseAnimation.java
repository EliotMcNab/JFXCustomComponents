package app.customControls.Animations;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

/**
 * {@link javafx.animation.Animation Animation} with support for pausing and restarting from where animation left off
 */
public abstract class PauseAnimation extends AnimationTimer {

    private long animationStart;
    private long pauseStart;
    private long pauseDuration;
    private final Duration animationDuration;
    private boolean pauseScheduled;
    private boolean startScheduled;
    private boolean paused;
    private boolean stopped;
    private boolean targetReached;
    private boolean isRunning;
    private EventHandler<ActionEvent> onEnd;

    public PauseAnimation(final Duration animationDuration) {
        super();
        this.onEnd = actionEvent -> {};
        this.animationDuration = animationDuration;
    }

    @Override
    public void start() {
        super.start();
        reset();
        startScheduled = true;
        isRunning = true;
    }

    /**
     * Schedules pausing the animation on the next handle call
     */
    public void pause() {
        if (paused) return;

        pauseScheduled = true;
        paused = true;
        isRunning = false;
    }

    /**
     * Schedules resetting the animation on the next handle call
     */
    public void reset() {
        paused = false;
        stopped = false;
        targetReached = false;
        pauseDuration = 0;
    }

    @Override
    public void handle(long now) {
        // exits the animation if it has reached the end
        if (stopped) return;

        // updates the time since the animation was paused
        if (paused) {
            pauseDuration = now - pauseStart;
        }
        // pauses the animation if scheduled
        if (pauseScheduled) {
            pauseStart = now;
            pauseScheduled = false;
        }
        // starts the animation if scheduled
        else if (startScheduled) {
            animationStart = now - pauseDuration;
            startScheduled = false;
        }

        // determines the current progress of the animation in millis, taking pauses into consideration
        final long animationTime = (now - animationStart) / (long) 1e6;

        // if the animation has finished but has not reached the target timestamp, artificially calls it at the mas time
        if (animationTime > animationDuration.toMillis() && !targetReached) {
            tick((long) animationDuration.toMillis());
            handleEnd();
            return;
        }

        // runs the animation
        tick(animationTime);

        // marks the target animation duration as reached when that happens
        if (animationTime == animationDuration.toMillis()) {
            targetReached = true;
            handleEnd();
        }
    }

    private void handleEnd() {
        stopped = true;
        isRunning = false;
        onEnd.handle(new ActionEvent());
    }

    /**
     * Run your animation here
     * @param now (long): animation time in millis
     */
    public abstract void tick(final long now);

    public void setOnEnd(EventHandler<ActionEvent> onEnd) {
        this.onEnd = onEnd;
    }

    /**
     * Returns true if the animation is being played, is not paused and has not finished
     * @return (boolean): whether the animation is still running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
