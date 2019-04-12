package drivers;

import kernel.datastructs.RingBuffer;

public abstract class InputDevice {
    public abstract void readInto(RingBuffer focusTaskStdIn);
}
