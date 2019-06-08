package tests;

import io.Color;
import io.GreenScreenOutput;
import io.LowlevelOutput;
import io.Screen;
import kernel.Kernel;
import tests.highlevel.*;
import tests.lowlevel.BasicAllocationTest;
import tests.lowlevel.LowlevelOutputTest;
import tests.lowlevel.PagingTest;

public class TestRunner {
    public static void run(int seconds) {
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        check(LowlevelOutputTest.test());
        check(GreenScreenOutputTest.test());
        check(PagingTest.test());
        check(StringTest.test());
        check(RingBufferTest.test());
        check(LinkedListIterTest.test());
        check(KeyboardLayoutTest.test());
        //check(DnsTest.test());
        check(ArrayListTest.test());
        check(LinkedListMemoryManagerTest.test());
        check(PooledKeyEventBufferTest.test());
        check(DivesesAndPlaygroundTest.test());
        check(EndianessTest.test());
        check(IpTest.test());
        check(GarbageCollectorTest.test());


        //LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        //LowlevelOutput.printStr("Tests OK. All systems GO;", 25, 12, Color.DEFAULT_COLOR);
        Screen s = new Screen();
        s.switchToGraphics();
        s.showGreyscaleSquirrelPicture();
        Kernel.wait(seconds);
        s.switchToTextMode();
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);

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
