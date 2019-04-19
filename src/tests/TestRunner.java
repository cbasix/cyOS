package tests;

import io.Color;
import io.LowlevelOutput;
import io.Screen;
import kernel.Kernel;
import tests.highlevel.ArrayListTest;
import tests.highlevel.KeyboardLayoutTest;
import tests.highlevel.RingBufferTest;
import tests.highlevel.StringTest;
import tests.lowlevel.BasicAllocationTest;
import tests.lowlevel.LowlevelOutputTest;

public class TestRunner {
    public static void run(int seconds) {
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        check(LowlevelOutputTest.test());
        check(StringTest.test());
        check(RingBufferTest.test());
        check(KeyboardLayoutTest.test());
        check(ArrayListTest.test());

        //LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        //LowlevelOutput.printStr("Tests OK. All systems GO;", 25, 12, Color.DEFAULT_COLOR);
        Screen s = new Screen();
        s.switchToGraphics();
        s.showGreyscaleSquirrelPicture();
        Kernel.wait(seconds);
        s.switchToTextMode();

    }

    public static void runBasicAllocationTest() {
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        check(BasicAllocationTest.test());
    }

    public static void check(int returnCode){
        if (returnCode != 0){
            //LowlevelOutput.clearScreen(Color.RED);
            LowlevelOutput.printStr("Test Failed", 30, 12, Color.RED);
            LowlevelOutput.printStr("Error Code: ", 30, 13, Color.RED);
            LowlevelOutput.printInt(returnCode, 10, 10, 30, 14, Color.RED);
            while (true){}
        }
    }
}
