package drivers.keyboard;

import io.Color;
import io.LowlevelOutput;
import kernel.datastructs.RingBuffer;
import kernel.interrupts.core.InterruptReceiver;

public class KeyboardInterruptReceiver extends InterruptReceiver {
    public static final int ON_HOLD = 0;
    public static final int TOOGLE = 1;

    public static final int KEYBOARD_PORT = 0x60 & 0xFF;
    public static final int NORMAL = 0xDF;
    public static final int EXPAND_ONE = 0xE0;
    public static final int EXPAND_TWO = 0xE1;

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
    public static final int NUM = 0x45;
    public static final int CAPS = 0x2A;
    public static final int CAPS_LCK = 0X3A;
    public static final int ICON = 0x5B;
    public static final int ICON_RIGHT = 0x5C;
    // todo doesnt work now
    public static final int ALT_GR = ( ALT | MODIFIER_EXTENSION );

    public static final int[] MODIFIER_KEYS = {
            STRG,    ALT,     NUM,    CAPS,    CAPS_LCK, ICON,    ICON_RIGHT, ALT_GR
    };
    public static final int[] MODIFIER_MODES = {
            ON_HOLD, ON_HOLD, TOOGLE, ON_HOLD, TOOGLE,   ON_HOLD, ON_HOLD,    ON_HOLD
    };
    //--------------------- END MODIFIER KEY CONFIGURATION -----------------------

    private boolean[] modifierStates = new boolean[MODIFIER_KEYS.length];
    public static RingBuffer pressedBuffer;
    private int keyPartBuffer[] = new int[3];
    private int byteNo = 0;


    public KeyboardInterruptReceiver(){
        pressedBuffer = new RingBuffer(20);
    }


    @Override
    public void handleInterrupt(int interruptNo, int param) {
        boolean done = false;
        KeyboardEvent e = null;
        int key_byte = MAGIC.rIOs8(KEYBOARD_PORT) & 0x000000FF;

        if (byteNo == 0 && key_byte > EXPAND_TWO){
            // ignore "diverses" see phase 4a
            return;
        }

        keyPartBuffer[byteNo] = key_byte;
        byteNo++;

        if (keyPartBuffer[0] <= NORMAL){
            done = true;
            e = new KeyboardEvent(keyPartBuffer[0] & 0x7F, (keyPartBuffer[0] & 0x80) == 0);

        } else if (keyPartBuffer[0] == EXPAND_ONE && byteNo == 2){
            done = true;
            e = new KeyboardEvent((keyPartBuffer[1] & 0x7F), (keyPartBuffer[1] & 0x80) == 0, MODIFIER_EXTENSION);

        } else if (keyPartBuffer[0] == EXPAND_TWO && byteNo == 3){
            // all 3 byte codes (pause) are used as system interrupt
            MAGIC.inline(0xCC);
            return;
        }

        if (done){
            // apply modifiers
            applyModifiers(e);

            // toogle modifiers (after apply because they shall not modify them selfes)
            toggleModifiers(e);
            /*for (int i = 0; i < modifierStates.length; i++) {
                LowlevelOutput.printBool(modifierStates[i], 70, 10+i, Color.PINK);
            }*/

            pressedBuffer.push(e);
            byteNo = 0;
        }
    }

    @SJC.Inline
    private void toggleModifiers(KeyboardEvent e) {
        for (int i = 0; i < modifierStates.length; i++){
            if (MODIFIER_KEYS[i] == e.key){
                if (MODIFIER_MODES[i] == ON_HOLD) {
                    // modifier only active while key hold down
                    modifierStates[i] = e.pressed;

                } else if (MODIFIER_MODES[i] == TOOGLE) {
                    // toggle mode on key press only if last one wasnt the same key
                    if (e.pressed){
                        modifierStates[i] = ! modifierStates[i];
                    }
                    // todo maybe if key has led: set it too (no leds available on my keyboard)
                }
            }
        }
    }

    @SJC.Inline
    private void applyModifiers(KeyboardEvent e) {
        for (int i = 0; i < modifierStates.length; i++){
            if (modifierStates[i]) {
                int modifierKey = MODIFIER_KEYS[i];
                if (modifierKey == CAPS || modifierKey == CAPS_LCK) {
                    e.modifiers |= MODIFIER_CAPS;
                }
                // alt gr works as strg & alt
                if (modifierKey == STRG || modifierKey == ALT_GR) {
                    e.modifiers |= MODIFIER_STRG;
                }
                if (modifierKey == ALT || modifierKey == ALT_GR) {
                    e.modifiers |= MODIFIER_ALT;
                }
                if (modifierKey == NUM) {
                    e.modifiers |= MODIFIER_NUM;
                }
                if (modifierKey == ICON) {
                    e.modifiers |= MODIFIER_ICON;
                }
            }
        }
    }
}
