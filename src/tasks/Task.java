package tasks;

import datastructs.RingBuffer;

public abstract class Task {
    public RingBuffer stdin = new RingBuffer(200);

    public abstract void onStart();

    public abstract void onStop();

    public abstract void onFocus();

    public abstract void onTick();

    public abstract void onBackgroundTick();
}
