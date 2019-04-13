package tests;

import drivers.keyboard.layout.KeyboardLayout;
import io.Color;
import io.LowlevelOutput;
import kernel.Kernel;
import tests.highlevel.ArrayListTest;
import tests.highlevel.KeyboardLayoutTest;
import tests.highlevel.RingBufferTest;
import tests.highlevel.StringTest;
import tests.lowlevel.AllocationTest;
import tests.lowlevel.LowlevelOutputTest;

public class TestRunner {
    public static void run(int seconds) {
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        check(AllocationTest.test());
        check(LowlevelOutputTest.test());
        check(StringTest.test());
        check(RingBufferTest.test());
        check(KeyboardLayoutTest.test());
        check(ArrayListTest.test());

        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        LowlevelOutput.printStr("Tests OK. All systems GO;", 25, 12, Color.DEFAULT_COLOR);
        Kernel.wait(seconds);

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
