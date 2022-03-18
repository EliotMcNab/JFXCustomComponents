package app.customControls.controls.time;

import javafx.animation.AnimationTimer;

public abstract class TimedAnimation extends AnimationTimer {

    private final DelayHandler delay;

    public TimedAnimation(final long delay) {
        this.delay = new DelayHandler(delay);
    }

    @Override
    public void handle(long l) {
        if (!delay.hasElapsed()) return;
        run();
    }

    public abstract void run();
}
