package drivers.keyboard;

import drivers.InputDevice;
import drivers.keyboard.layout.KeyboardLayout;
import kernel.datastructs.RingBuffer;
import kernel.interrupts.core.Interrupts;

public class Keyboard extends InputDevice {

    KeyboardLayout layout;

    public Keyboard(KeyboardLayout layout){
        this.layout = layout;
    }

    public void readInto(RingBuffer focusTaskStdIn){
        // disable interrupts to prevent messing up the ring buffer during read
        Interrupts.disable();
        if(KeyboardInterruptReceiver.pressedBuffer.count() > 0){

            // get raw event from buffer
            KeyboardEvent k = (KeyboardEvent) KeyboardInterruptReceiver.pressedBuffer.get();
            Interrupts.enable();

            // translate to real keybord Event
            this.layout.setCharOn(k);


            focusTaskStdIn.push(k);

        } else{
            Interrupts.enable();
        }
    }
}
