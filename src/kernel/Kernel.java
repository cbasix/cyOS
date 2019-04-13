package kernel;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardInterruptReceiver;
import drivers.keyboard.layout.KeyboardLayoutDE;
import io.*;
import kernel.datastructs.subtypes.TaskArrayList;
import kernel.interrupts.core.InterruptHub;
import kernel.interrupts.core.Interrupts;
import kernel.interrupts.receivers.AliveIndicator;
import kernel.interrupts.receivers.Bluescreen;
import kernel.interrupts.receivers.ScreenOutput;
import rte.DynamicRuntime;
import tasks.Task;
import tasks.shell.Shell;
import tests.TestRunner;


public class Kernel {

    public static final int OUTPUT_APP = 0;
    public static final int ALLOCATION_APP = 1;
    public static final int INTERRUPT_APP = 2;

    public static int mode = OUTPUT_APP;

    public static TaskManager taskManager;


    public static void init() {
        DynamicRuntime.initializeMemoryPointers();
        MAGIC.doStaticInit();

        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
        taskManager = new TaskManager();
    }


    public static void main() {
        init();

        Keyboard keyboard = new Keyboard(new KeyboardLayoutDE());

        Interrupts.init();

        InterruptHub.addObserver(new ScreenOutput(), InterruptHub.ALL_INTERRUPTS);
        InterruptHub.addObserver(new AliveIndicator(), Interrupts.TIMER);
        InterruptHub.addObserver(new Bluescreen(), InterruptHub.ALL_EXCEPTIONS);
        InterruptHub.addObserver(new KeyboardInterruptReceiver(), Interrupts.KEYBOARD);

        Interrupts.enable();

        // Run Tests
        TestRunner.run(2); // run test suite and show result, then wait for 2 secs
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);

        taskManager.addInputDevice(keyboard);
        taskManager.requestStart(new Shell());

        taskManager.loop();

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
