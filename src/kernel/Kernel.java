package kernel;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardInterruptReceiver;
import drivers.keyboard.layout.KeyboardLayoutDE;
import io.*;
import kernel.interrupts.core.InterruptHub;
import kernel.interrupts.core.Interrupts;
import kernel.interrupts.receivers.AliveIndicator;
import kernel.interrupts.receivers.ScreenOutput;
import kernel.memory.BasicMemoryManager;
import kernel.memory.ArrayListMemoryManager;
import kernel.memory.MemoryManager;
import tasks.shell.Shell;
import tests.TestRunner;


public class Kernel {
    public static TaskManager taskManager;
    public static MemoryManager memoryManager;


    public static void main() {

        // ------------- setup memory ~> new -------------
        // basic manager allows other mangers to use new before taking over allocation
        memoryManager = BasicMemoryManager.initialize();
        // test basic allocation
        TestRunner.runBasicAllocationTest();
        // instatiate advanced memory managers
        memoryManager = new ArrayListMemoryManager();


        MAGIC.doStaticInit();
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);


        // -------------- setup interrupts
        InterruptHub interruptHub = Interrupts.init();

        interruptHub.addObserver(new ScreenOutput(), InterruptHub.ALL_EXTERNAL);
        interruptHub.addObserver(new AliveIndicator(), Interrupts.TIMER);
        interruptHub.addObserver(new KeyboardInterruptReceiver(), Interrupts.KEYBOARD);

        Interrupts.enable();

        // -------------- Run Tests
        TestRunner.run(1); // run test suite and show result
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);


        // -------------- setup and run task manager
        taskManager = new TaskManager();
        taskManager.addInputDevice(new Keyboard(new KeyboardLayoutDE()));
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
