package tests.highlevel;

import drivers.keyboard.layout.KeyboardLayout;
import drivers.keyboard.layout.KeyboardLayoutDE;
import io.LowlevelLogging;
import kernel.datastructs.RingBuffer;

public class KeyboardLayoutTest {
    public static int test(){

        KeyboardLayout kl = new KeyboardLayoutDE();
        char[] escaped = "one\nliner\\n\\0".toChars();
        char[] unescaped = kl.unescape(escaped, true);

        char[] should ="oneliner\n\0".toChars();

        for (int i = 0; i < unescaped.length; i++){
            if (unescaped[i] != should[i]) { return 501;}
        }


        return 0;
    }
}
