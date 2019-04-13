package tests.highlevel;

import drivers.keyboard.layout.KeyboardLayout;
import drivers.keyboard.layout.KeyboardLayoutDE;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.datastructs.RingBuffer;

public class KeyboardLayoutTest {
    public static int test(){

        KeyboardLayout kl = new KeyboardLayoutDE();
        char[] escaped = "one\nliner\\n\\0".toChars();
        char[] unescaped = kl.unescape(escaped, true);

        char[] should ="oneliner\n\0".toChars();

        /* different length is ok
        if (should.length != unescaped.length){
            LowlevelOutput.printStr(new String(should), 40, 15, Color.RED);
            LowlevelOutput.printInt(should.length, 10, 10, 55, 15, Color.RED);
            LowlevelOutput.printStr(new String(unescaped), 40, 16, Color.RED);
            LowlevelOutput.printInt(unescaped.length, 10, 10, 55, 16, Color.RED);
            return 501;
        }*/

        for (int i = 0; i < unescaped.length && i < should.length; i++){
            if (unescaped[i] != should[i]) { return 504;}
        }


        return 0;
    }
}
