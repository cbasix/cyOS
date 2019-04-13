package kernel.interrupts.core;

public abstract class InterruptReceiver {
    public abstract boolean handleInterrupt(int interruptNo, int param);
}
