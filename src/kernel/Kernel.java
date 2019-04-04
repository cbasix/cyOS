package kernel;


import io.*;
import rte.DynamicRuntime;
import tests.TestRunner;

public class Kernel {

    private static int vidMemCursor = 0xB8000;

    public static void init() {
        DynamicRuntime.initializeMemoryPointers();
        MAGIC.doStaticInit();

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    }

    public static void main() {
        init();

        Interrupts.init();

        //String[] args = null;
        //LongHexAlgorithmTest.main(args);

        TestRunner.run(2); // run test suite and show result, then wait for 2 secs

        OutputApp.run(2); // run output app for 10 seconds
        AllocationApp.run();  // run allocation app (which runs forever)

        // remind myself that i forgot to uncomment one of the run methods above...
        while (true) {
            LowlevelLogging.debug("Please uncomment one of the run methods within the main method (or forgot loop?)", LowlevelLogging.ERROR);
        }
    }

    /**
     * Wait for up to <delayInSeconds> seconds
     *
     * TODO Rembember this is not exact! depending on the timing up to 1000ms are getting lost.
     */
    public static void wait(int delayInSeconds) {
        int diffCount = 0;
        int lastSeen = RTC.read(RTC.SECOND);

        while (diffCount < delayInSeconds) {

            int currentSec = RTC.read(RTC.SECOND);
            if (lastSeen != currentSec) {
                lastSeen = currentSec;
                diffCount++;
            }
        }
    }


}
