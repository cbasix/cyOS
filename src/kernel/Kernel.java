package kernel;


import io.*;
import rte.DynamicRuntime;
import tests.TestRunner;

public class Kernel {

    private static int vidMemCursor = 0xB8000;


    public static void main() {
        init();

        //String[] args = null;
        //LongHexAlgorithmTest.main(args);

        TestRunner.run(2); // run test suite and show result, then wait for 2 secs
        OutputApp.run(10); // run output app for 10 seconds
        AllocationApp.run();  // run allocation app (which runs forever)

        // remind myself that i forgot to uncomment one of the run methods above...
        while (true) {
            LowlevelLogging.debug("Please uncomment one of the run methods within the main method (or forgot loop?)", LowlevelLogging.ERROR);
        }
    }

    public static void init() {
        DynamicRuntime.initializeMemoryPointers();
        MAGIC.doStaticInit();

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    }


    // TODO this is not exact! up to 1000ms not exact.
    public static void wait(int delayInSeconds) {
        //LowlevelOutputTest.printInt(88, 10, 3, 0, 19, Color.GREEN);
        int diffCount = 0;
        int lastSeen = RTC.read(RTC.SECOND);
        while (diffCount < delayInSeconds) {
            int currentSec = RTC.read(RTC.SECOND);
            if (lastSeen != currentSec) {
                lastSeen = currentSec;
                diffCount++;
            }
            //LowlevelOutputTest.printInt(diffCount, 10, 3, 0, 20, Color.GREEN);
        }
        ;
    }


}
