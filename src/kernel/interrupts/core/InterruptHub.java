package kernel.interrupts.core;

public class InterruptHub {
    public static final int ALL_INTERRUPTS = 0x00FFFFFF;
    public static final int ALL_EXCEPTIONS = 0x01FFFFFF;
    public static final int ALL_EXTERNAL = 0x02FFFFFF;

    public static ObserverBinding[] observerBindings = {};

    private static class ObserverBinding {
        public InterruptReceiver observer;
        public int interruptNo;

        public ObserverBinding(InterruptReceiver observer, int interruptNo){
            this.observer = observer;
            this.interruptNo = interruptNo;
        }
    }

    @SJC.Inline
    public static void forwardInterrupt(int interruptNo, int param){
        for (int i = 0; i < observerBindings.length; i++){
            if (observerBindings[i].interruptNo == interruptNo
                    || observerBindings[i].interruptNo == ALL_INTERRUPTS
                    || (interruptNo <= Interrupts.PAGE_FAULT && observerBindings[i].interruptNo == ALL_EXCEPTIONS)){
                observerBindings[i].observer.handleInterrupt(interruptNo, param);
            }
        }
    }

    public static void addObserver(InterruptReceiver observer, int interruptNo){
        ObserverBinding[] newObserverBindings = new ObserverBinding[observerBindings.length + 1];
        for (int i = 0; i < observerBindings.length; i++){
            newObserverBindings[i] = observerBindings[i];
        }
        // use observer bindings length here since it already is less one
        newObserverBindings[observerBindings.length] = new ObserverBinding(observer, interruptNo);
        observerBindings = newObserverBindings;
    }

    public static void removeObserver(InterruptReceiver observer, int interruptNo){
        ObserverBinding[] newObserverBindings = new ObserverBinding[observerBindings.length - 1];
        int j = 0;
        for (int i = 0; i < observerBindings.length; i++){
            if (observerBindings[i].interruptNo == interruptNo && observerBindings[i].observer == observer) {
                newObserverBindings[j] = observerBindings[i];
                j++;
            }
        }
        observerBindings = newObserverBindings;
    }
}
