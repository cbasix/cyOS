package drivers.keyboard;

import drivers.InputDevice;
import drivers.keyboard.layout.KeyboardLayout;
import datastructs.RingBuffer;
import kernel.interrupts.core.Interrupts;

public class Keyboard extends InputDevice {

    public static final int MODIFIER_EXTENSION = 1;
    public static final int MODIFIER_CAPS = 1 << 1;
    public static final int MODIFIER_ALT = 1 << 2;
    public static final int MODIFIER_NUM = 1 << 3;
    public static final int MODIFIER_STRG = 1 << 4;
    public static final int MODIFIER_ICON = 1 << 5;
    public static final int MODIFIER_ALT_GR = 1 << 6;

    //--------------------- MODIFIER KEY CONFIGURATION -----------------------
    public static final int STRG_LEFT = 0x1D;
    public static final int STRG_RIGHT = 0x11D;
    public static final int ALT = 0x38;
    public static final int ALT_GR = 0x138;
    public static final int NUM = 0x45;
    public static final int CAPS = 0x2A;
    public static final int CAPS_2ND = 0x36;
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
    public static final int PG_UP = 73;
    public static final int PG_DOWN = 81;


    KeyboardLayout layout;

    public Keyboard(KeyboardLayout layout){
        this.layout = layout;
    }

    public void readInto(RingBuffer focusTaskStdIn){
        // disable interrupts to prevent messing up the ring buffer during get
        Interrupts.disable();
        while (KeyboardInterruptReceiver.pressedBuffer.count() > 0){

            // get raw event from buffer
            KeyboardEvent k = new KeyboardEvent();
            if(KeyboardInterruptReceiver.pressedBuffer.getCopyInto(k)) {

                Interrupts.enable();
                // add char infos using the layout
                this.layout.setCharOn(k);

                focusTaskStdIn.push(k);
                Interrupts.disable();
            }
        }
        Interrupts.enable();

    }
}
