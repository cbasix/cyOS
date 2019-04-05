package kernel;


import apps.AllocationApp;
import apps.InterruptApp;
import apps.OutputApp;
import io.*;
import kernel.interrupts.*;
import rte.DynamicRuntime;
import tests.TestRunner;

public class Kernel {

    public static final int OUTPUT_APP = 0;
    public static final int ALLOCATION_APP = 1;
    public static int mode = OUTPUT_APP;

    public static void init() {
        DynamicRuntime.initializeMemoryPointers();
        MAGIC.doStaticInit();

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    }

    private static class ModeSwitcher extends InterruptReceiver{
        @Override
        public void handleInterrupt(int interruptNo, int param) {
            Kernel.mode = (Kernel.mode + 1) % 2;
        }
    }

    public static void main() {
        init();

        Interrupts.init();

        InterruptHub.addObserver(new ScreenOutput(), InterruptHub.ALL_INTERRUPTS);
        InterruptHub.addObserver(new AliveIndicator(), 0x20);
        // misuse nmi interrupt command from qemu monitor to switch mode ;)
        InterruptHub.addObserver(new ModeSwitcher(), 0x02);
        InterruptHub.addObserver(new Bluescreen(), 0x00);
        Interrupts.enable();

        /*InterruptHub.forwardInterrupt(8, 0);

        InterruptReceiver t = intScreenOutput;

        wait(1);
        intScreenOutput.handleInterrupt(6, 5);
        wait(1);
        t.handleInterrupt(7, 6);*/

        //String[] args = null;
        //LongHexAlgorithmTest.main(args);

        TestRunner.run(2); // run test suite and show result, then wait for 2 secs
        InterruptApp.run();

        while (true){
            if (mode == ALLOCATION_APP){
                AllocationApp.run();  // run allocation app
            } else {
                OutputApp.run(); // run output app
            }
        }


        // remind myself that i forgot to uncomment one of the run methods above...
        /*while (true) {
            LowlevelLogging.debug("Please uncomment one of the run methods within the main method (or forgot loop?)", LowlevelLogging.ERROR);
        }*/
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
