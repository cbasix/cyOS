package kernel;


import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardInterruptReceiver;
import drivers.keyboard.layout.KeyboardLayoutDE;
import io.*;
import kernel.interrupts.core.InterruptHub;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;
import kernel.interrupts.receivers.AliveIndicator;
import kernel.interrupts.receivers.Bluescreen;
import kernel.interrupts.receivers.ScreenOutput;
import rte.DynamicRuntime;
import tasks.shell.Shell;
import tests.TestRunner;

public class Kernel {

    public static final int OUTPUT_APP = 0;
    public static final int ALLOCATION_APP = 1;
    public static final int INTERRUPT_APP = 2;

    public static int mode = OUTPUT_APP;

    public static void init() {
        DynamicRuntime.initializeMemoryPointers();
        MAGIC.doStaticInit();

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    }

    // temporary: use nmis to switch between tasks
    // misuse of the nmi interrupt command from qemu monitor ;)
    private static class ModeSwitcher extends InterruptReceiver {
        int cnt = 0;
        @Override
        public void handleInterrupt(int interruptNo, int param) {
            Kernel.mode = (Kernel.mode + 1) % 2;

            // on 3th nmi start the interrupt app which shows a bluescreen
            cnt++;
            if (cnt >= 3){
                Kernel.mode = INTERRUPT_APP;
            }

        }
    }

    public static void main() {
        init();

        Keyboard keyboard = new Keyboard(new KeyboardLayoutDE());

        Interrupts.init();

        InterruptHub.addObserver(new ScreenOutput(), InterruptHub.ALL_INTERRUPTS);
        InterruptHub.addObserver(new AliveIndicator(), Interrupts.TIMER);
        InterruptHub.addObserver(new ModeSwitcher(), Interrupts.NMI);
        InterruptHub.addObserver(new Bluescreen(), InterruptHub.ALL_EXCEPTIONS);
        InterruptHub.addObserver(new KeyboardInterruptReceiver(), Interrupts.KEYBOARD);

        Interrupts.enable();

        // Show Welcome screen
        //Welcome.run();

        // Run Tests
        TestRunner.run(0); // run test suite and show result, then wait for 2 secs

        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);

        //Editor e = new Editor();

        Shell s = new Shell();

        // Start tasks
        s.onStart();

        while (true){
            keyboard.readInto(s.stdin);
            s.onTick();


            /*if (mode == ALLOCATION_APP){
                AllocationApp.run();  // run allocation app
            } else if (mode == OUTPUT_APP){
                OutputApp.run(); // run output app
            } else {
                Interrupt.run();
            }*/
            //wait(1);
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
