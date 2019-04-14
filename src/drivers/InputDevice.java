package drivers;

import datastructs.RingBuffer;

public abstract class InputDevice {
    public abstract void readInto(RingBuffer focusTaskStdIn);
}
