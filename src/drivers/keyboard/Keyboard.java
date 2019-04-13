package drivers.keyboard;

import drivers.InputDevice;
import drivers.keyboard.layout.KeyboardLayout;
import kernel.datastructs.RingBuffer;
import kernel.interrupts.core.Interrupts;

public class Keyboard extends InputDevice {

    public static final int MODIFIER_EXTENSION = 0x01;
    public static final int MODIFIER_CAPS = 0x02;
    public static final int MODIFIER_ALT = 0x04;
    public static final int MODIFIER_NUM = 0x08;
    public static final int MODIFIER_STRG = 0x10;
    public static final int MODIFIER_ICON = 0x20;
    // todo fill correct
    //--------------------- MODIFIER KEY CONFIGURATION -----------------------
    // maybe define roll modifier even if nobody needs it
    public static final int STRG = 0x1D;
    public static final int ALT = 0x38;
    // todo altgr not implemented
    public static final int ALT_GR = ( ALT | MODIFIER_EXTENSION );
    public static final int NUM = 0x45;
    public static final int CAPS = 0x2A; // todo 2nd caps
    public static final int CAPS_2ND = 0x36; // todo 2nd caps
    public static final int CAPS_LCK = 0X3A;
    public static final int ICON = 0x5B;
    public static final int ICON_RIGHT = 0x5C;

    // command keys
    public static final int ENTER = 28;
    public static final int BACKSP = 14;
    public static final int UP = 72;
    public static final int RIGHT = 77;
    public static final int DOWN = 80;
    public static final int LEFT = 75;
    public static final int ESC = 1;


    KeyboardLayout layout;

    public Keyboard(KeyboardLayout layout){
        this.layout = layout;
    }

    public void readInto(RingBuffer focusTaskStdIn){
        // disable interrupts to prevent messing up the ring buffer during read
        Interrupts.disable();
        if(KeyboardInterruptReceiver.pressedBuffer.count() > 0){

            // _get raw event from buffer
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
