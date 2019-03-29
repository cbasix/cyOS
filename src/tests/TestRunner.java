package tests;

import io.Color;
import io.GreenScreenConst;
import io.LowlevelOutput;
import kernel.Kernel;
import tests.lowlevel.AllocationTest;
import tests.lowlevel.LowlevelOutputTest;

public class TestRunner {
    public static void run(int seconds) {
        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
        check(AllocationTest.test());
        check(LowlevelOutputTest.test());

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
        LowlevelOutput.printStr("Tests OK. All systems GO;", 25, 12, GreenScreenConst.DEFAULT_COLOR);
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
