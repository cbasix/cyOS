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
import kernel.memory.LinkedListMemoryManager;
import kernel.memory.MemoryManager;
import kernel.memory.Paging;
import tasks.shell.Shell;
import tests.TestRunner;


public class Kernel {
    public static TaskManager taskManager;
    public static MemoryManager memoryManager;
    public static boolean doGC = false;


    public static void main() {

        // ------------- setup memory ~> new -------------
        // basic manager allows other mangers to use new before taking over allocation
        memoryManager = BasicMemoryManager.initialize();
        // test basic allocation
        //TestRunner.runBasicAllocationTest();
        // instatiate advanced memory managers



        MAGIC.doStaticInit();
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);


        // -------------- setup interrupts
        InterruptHub interruptHub = Interrupts.init();

        interruptHub.addObserver(new ScreenOutput(), InterruptHub.ALL_EXTERNAL);
        interruptHub.addObserver(new AliveIndicator(), Interrupts.TIMER);
        interruptHub.addObserver(new KeyboardInterruptReceiver(), Interrupts.KEYBOARD);

        Interrupts.enable();

        // switch to advanced memory manager
        memoryManager = new LinkedListMemoryManager();

        // -------------- Run Tests
        TestRunner.run(2); // run test suite and show result

        // enable paging
        Paging.enable();

        // -------------- setup and run task manager
        taskManager = new TaskManager();
        taskManager.addInputDevice(new Keyboard(new KeyboardLayoutDE()));
        taskManager.requestStart(new Shell());

        while (true) {
            taskManager.tick();

            if (doGC) {
                // doGC is set by the shell command GarbageCollection gc
                memoryManager.gc();
                doGC = false;
            }
        }

    }


    /**
     * Wait for up to <delayInSeconds> seconds
     *
     *  Rembember this is not exact! depending on the timing up to 1000ms are getting lost.
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

    @SJC.Inline
    public static void hlt(){
        MAGIC.inline(0xF4);
    }


}
