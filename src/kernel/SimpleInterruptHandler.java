package kernel;


import kernel.interrupts.InterruptReceiver;

public class SimpleInterruptHandler {
    public static final int MAX_HANDLERS = 100;

    private static InterruptReceiver[] receivers = new InterruptReceiver[MAX_HANDLERS];
    private static int[] wantedInterrupts = new int[MAX_HANDLERS];
    private static int receiverCount = 0;

    @SJC.Inline
    public static void notifyReceivers(int interruptNo, int param){
        for (int i = 0; i < receiverCount; i++){
            if (wantedInterrupts[i] == interruptNo || wantedInterrupts[i] > 0xFF) {
                receivers[i].handleInterrupt(interruptNo, param);
            }
        }
    }

    public static void addReceiver(InterruptReceiver receiver, int interruptNo){
        receivers[receiverCount] = receiver;
        wantedInterrupts[receiverCount] = interruptNo;
        receiverCount++;
    }

}
