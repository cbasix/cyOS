package kernel;


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
        // todo double check
        // if interrupt from slave PIC
        if (0x20 <= interruptNo && interruptNo < 0x28){
            // ack to slave
            MAGIC.wIOs8(Interrupts.SLAVE, (byte)0x20);

        }
        // if interrupt from any PIC -> ack to master too
        if (0x20 <= interruptNo && interruptNo < 0x30){
            // ack to master
            MAGIC.wIOs8(Interrupts.MASTER, (byte)0x20);
        }
    }

    public static void addReceiver(InterruptReceiver receiver, int interruptNo){
        receivers[receiverCount] = receiver;
        wantedInterrupts[receiverCount] = interruptNo;
        receiverCount++;
    }

    public static void int0(){
        // divide error
        notifyReceivers(0x00, 0);
    }
    public static void int3(){
        // breakpoint
        notifyReceivers(0x03, 0);
    }
    public static void int20(){
        // timer
        notifyReceivers(0x20, 0);
    }

}
