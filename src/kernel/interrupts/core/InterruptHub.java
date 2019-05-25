package kernel.interrupts.core;

import datastructs.subtypes.ObserverBindingArrayList;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelOutput;
import kernel.Kernel;

public class InterruptHub {
    public static final int ALL_EXTERNAL = 0x02FFFFFF;
    public ObserverBindingArrayList observerBindings = new ObserverBindingArrayList();

    public static class ObserverBinding {
        public InterruptReceiver observer;
        public int interruptNo;

        public ObserverBinding(InterruptReceiver observer, int interruptNo){
            this.observer = observer;
            this.interruptNo = interruptNo;
        }
    }

    //@SJC.Inline
    public void forwardInterrupt(int interruptNo, int param){
        boolean handled = false;
        for (int i = 0; i < observerBindings.size(); i++){
            ObserverBinding ob = observerBindings.get(i);
            if (ob.interruptNo == interruptNo
                    || ob.interruptNo == ALL_EXTERNAL){

                if(ob.observer.handleInterrupt(interruptNo, param)){
                    handled = true;
                }

            }
        }
        if (!handled) {
            LowlevelOutput.printStr("Interrupt not handled: ", 40, 0, Color.RED);
            LowlevelOutput.printStr(String.from(interruptNo), 63, 0, Color.RED);
            Kernel.wait(2);
        }
    }

    public void addObserver(InterruptReceiver observer, int interruptNo){
        observerBindings.add(new ObserverBinding(observer, interruptNo));
    }

    public void removeObserver(InterruptReceiver observer, int interruptNo){
        for (int i = 0; i < observerBindings.size(); i++){
            ObserverBinding ob = observerBindings.get(i);
            if (ob.interruptNo == interruptNo && ob.observer == observer) {
                observerBindings.remove(ob);
                break;
            }
        }
    }
}
