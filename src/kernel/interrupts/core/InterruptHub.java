package kernel.interrupts.core;

import io.Color;
import io.GreenScreenOutput;
import io.LowlevelOutput;
import kernel.Kernel;

public class InterruptHub {
    public static final int ALL_EXTERNAL = 0x02FFFFFF;

    // todo use new arraylist class here instead of duplicated code
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
        boolean handled = false;
        for (int i = 0; i < observerBindings.length; i++){
            if (observerBindings[i].interruptNo == interruptNo
                    || observerBindings[i].interruptNo == ALL_EXTERNAL){

                if(observerBindings[i].observer.handleInterrupt(interruptNo, param)){
                    handled = true;
                }

            }
        }
        if (!handled) {
            LowlevelOutput.printStr("Interrupt not handled: ", 40, 0, Color.RED);
            LowlevelOutput.printInt(interruptNo,10,2, 63, 0, Color.RED);
            Kernel.wait(2);
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
