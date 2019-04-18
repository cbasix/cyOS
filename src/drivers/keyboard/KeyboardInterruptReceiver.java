package drivers.keyboard;

import datastructs.RingBuffer;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;


// todo prettify
public class KeyboardInterruptReceiver extends InterruptReceiver {
    public static final int ON_HOLD = 0;
    public static final int TOOGLE = 1;

    public static final int KEYBOARD_PORT = 0x60 & 0xFF;
    public static final int NORMAL = 0xDF;
    public static final int EXPAND_ONE = 0xE0;
    public static final int EXPAND_TWO = 0xE1;

    public static final int[] MODIFIER_KEYS = {
            Keyboard.STRG_LEFT,
            Keyboard.STRG_RIGHT,
            Keyboard.ALT,
            Keyboard.NUM,
            Keyboard.CAPS,
            Keyboard.CAPS_LCK,
            Keyboard.ICON,
            Keyboard.ICON_RIGHT,
            Keyboard.ALT_GR,
            Keyboard.CAPS_2ND
    };
    public static final int[] MODIFIER_MODES = {
            ON_HOLD,
            ON_HOLD,
            ON_HOLD,
            TOOGLE,
            ON_HOLD,
            TOOGLE,
            ON_HOLD,
            ON_HOLD,
            ON_HOLD,
            ON_HOLD,
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
    public boolean handleInterrupt(int interruptNo, int param) {
        boolean done = false;
        KeyboardEvent e = null;
        int key_byte = MAGIC.rIOs8(KEYBOARD_PORT) & 0x000000FF;

        if (byteNo == 0 && key_byte > EXPAND_TWO){
            // ignore "diverses" see phase 4a
            return true;
        }

        keyPartBuffer[byteNo] = key_byte;
        byteNo++;

        if (keyPartBuffer[0] <= NORMAL){
            done = true;
            e = new KeyboardEvent(keyPartBuffer[0] & 0x7F, (keyPartBuffer[0] & 0x80) == 0);

        } else if (keyPartBuffer[0] == EXPAND_ONE && byteNo == 2){
            done = true;
            e = new KeyboardEvent((keyPartBuffer[1] & 0x7F), (keyPartBuffer[1] & 0x80) == 0, Keyboard.MODIFIER_EXTENSION);

        } else if (keyPartBuffer[0] == EXPAND_TWO && byteNo == 3){
            // all 3 byte codes (pause) are used as system interrupt
            MAGIC.inline(0xCC);
            byteNo = 0;
            return true;
        }

        if (done){
            // apply modifiers
            applyModifiers(e);

            // toogle modifiers (after apply because they shall not modify them selfes)
            toggleModifiers(e);
            /*for (int i = 0; i < modifierStates.length; i++) {
                LowlevelOutput.printBool(modifierStates[i], 70, 10+i, Color.PINK);
            }*/
            LowlevelOutput.printInt(e.key, 10, 4, 50, 0, Color.DEFAULT_COLOR);
            LowlevelOutput.printHex(e.modifiers, 8, 58, 0, Color.DEFAULT_COLOR);

            pressedBuffer.push(e);
            byteNo = 0;
        }

        return true;
    }

    @SJC.Inline
    private void toggleModifiers(KeyboardEvent e) {
        for (int i = 0; i < modifierStates.length; i++){
            if ((MODIFIER_KEYS[i] == e.key && (e.modifiers & 0x1) != Keyboard.MODIFIER_EXTENSION)
                    || (MODIFIER_KEYS[i]  == (e.key | 0x100) && (e.modifiers & 0x1) == Keyboard.MODIFIER_EXTENSION)){

                if (MODIFIER_MODES[i] == ON_HOLD) {
                    // modifier only active while key hold down
                    modifierStates[i] = e.pressed;

                } else if (MODIFIER_MODES[i] == TOOGLE) {
                    // toggle mode on key press only if last one wasnt the same key
                    if (e.pressed){
                        modifierStates[i] = ! modifierStates[i];
                    }
                }
            }
        }
    }

    @SJC.Inline
    private void applyModifiers(KeyboardEvent e) {
        for (int i = 0; i < modifierStates.length; i++){
            if (modifierStates[i]) {
                int modifierKey = MODIFIER_KEYS[i];
                if (modifierKey == Keyboard.CAPS || modifierKey == Keyboard.CAPS_LCK || modifierKey == Keyboard.CAPS_2ND) {
                    e.modifiers |= Keyboard.MODIFIER_CAPS;
                }
                // alt gr works as strg & alt
                if (modifierKey == Keyboard.STRG_LEFT || modifierKey == Keyboard.STRG_RIGHT) {
                    e.modifiers |= Keyboard.MODIFIER_STRG;
                }
                if (modifierKey == Keyboard.ALT) {
                    e.modifiers |= Keyboard.MODIFIER_ALT;
                }
                if (modifierKey == Keyboard.ALT_GR) {
                    e.modifiers |= Keyboard.MODIFIER_ALT_GR;
                }
                if (modifierKey == Keyboard.NUM) {
                    e.modifiers |= Keyboard.MODIFIER_NUM;
                }
                if (modifierKey == Keyboard.ICON) {
                    e.modifiers |= Keyboard.MODIFIER_ICON;
                }
            }
        }
    }
}
