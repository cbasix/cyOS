package kernel.interrupts.core;

public abstract class InterruptReceiver {
    public abstract void handleInterrupt(int interruptNo, int param);
}