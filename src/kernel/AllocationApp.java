package kernel;

import io.*;
import kernel.interrupts.DescriptorTable;
import kernel.interrupts.Interrupts;
import rte.DynamicRuntime;
import tests.TestObject;


/**
 *  This app tries to do some allocations and shows data regarding to it in form of parameters and a hexdump of
 *  the memory containing the new objects
 */

public class AllocationApp {
    private static int dumpStartAddr = 1024;

    public static void run() {
        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);

        // set hexdump start to the address where our new objects will be created
        dumpStartAddr = DynamicRuntime.getNextFreeAddr();

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
        LowlevelOutput.printStr("Allocation App", 0, 0, GreenScreenConst.DEFAULT_COLOR);
        LowlevelOutput.printStr("Welcome to cyOS", 10, 4, GreenScreenConst.DEFAULT_COLOR);
        LowlevelOutput.printStr("Destroying world in progress ", 3, 6, GreenScreenConst.DEFAULT_COLOR);
        LowlevelOutput.printChar('%', 34, 6, GreenScreenConst.DEFAULT_COLOR);


        int tick = 0;

        TestObject t2 = null;

        while (true) {

            // update percent number
            LowlevelOutput.printInt(tick, 10, 3, 31, 6, GreenScreenConst.DEFAULT_COLOR);
            //LowlevelOutputTest.printInt(tick-50, 16, 3, 19, 7, GreenScreenConst.DEFAULT_COLOR);

            //update seconds
            LowlevelOutput.printStr("Sec", 27, 0, GreenScreenConst.DEFAULT_COLOR);
            LowlevelOutput.printInt(RTC.read(RTC.SECOND), 2, 8, 31, 0, GreenScreenConst.DEFAULT_COLOR);

            // update next free addr
            //LowlevelOutputTest.printStr("Next Free",  0, 1, GreenScreenConst.DEFAULT_COLOR);
            //LowlevelOutputTest.printInt(DynamicRuntime.getNextFreeAddr(), 10, 9, 10, 1, GreenScreenConst.DEFAULT_COLOR);

            LowlevelLogging.debug("Kernel A", LowlevelLogging.FINE);


            TestObject t1 = new TestObject();


            // after one tick start doing something usefull
            if (tick == 1) {

                LowlevelLogging.debug("Kernel X", LowlevelLogging.FINE);
                Kernel.wait(1);

                //t1 = ;
                t2 = new TestObject();


                // print status
                LowlevelLogging.debug("Kernel Y", LowlevelLogging.FINE);
            }

            if (tick >= 2) {
                LowlevelLogging.debug("Ticking T2", LowlevelLogging.INFO);
                t2.setData(tick);
                LowlevelLogging.debug("Testing T2", LowlevelLogging.INFO);
                if (tick == t2.getData()) {
                    LowlevelLogging.debug("Tick SUCCESS", LowlevelLogging.INFO);
                } else {
                    LowlevelLogging.debug("Something is seriously wrong with this object instance ", LowlevelLogging.ERROR);
                }

                LowlevelOutput.printStr("T1 addr", 0, 13, Color.PINK);
                LowlevelOutput.printInt(MAGIC.addr(t1), 10, 10, 15, 13, Color.PINK);
                LowlevelOutput.printInt(MAGIC.rMem32(MAGIC.addr(t1)), 10, 10, 15, 13, Color.PINK);

                LowlevelOutput.printStr("T2 addr", 0, 14, Color.PINK);
                LowlevelOutput.printInt(MAGIC.addr(t2), 10, 10, 15, 14, Color.PINK);
                LowlevelOutput.printInt(MAGIC.rMem32(MAGIC.addr(t2)), 10, 10, 15, 14, Color.PINK);
            }

            if (tick == 5){
                MAGIC.inline(0xcc);
                //int i = 0;
                //int f = 5 / i;
            }

            LowlevelLogging.debug("Kernel E", LowlevelLogging.FINE);
            LowlevelLogging.printHexdump(rte.DynamicRuntime.interruptDescriptorTableAddr + DescriptorTable.entryCount*MAGIC.ptrSize*2 - 16);

            if (tick == 4) {
                // stop here to inspect hexdump
                //debug("Stopped for inspection", ERROR);
                //while (true){}
            }

            //while (true){};
            LowlevelLogging.debug("Kernel F", LowlevelLogging.FINE);
            Kernel.wait(1);

            tick++;
        }

    }
}
