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
    public static InterruptHub interruptHub;
    public static boolean doGC = false;
    public  static int gcRun;


    public static void main() {

        // ------------- setup memory ~> new -------------
        // basic manager allows other mangers to use new before taking over allocation
        memoryManager = BasicMemoryManager.initialize();
        // test basic allocation
        // TestRunner.runBasicAllocationTest(); todo actualize basic allocation test to new object format
        // instatiate advanced memory managers -> after interrupts are enabled

        MAGIC.doStaticInit();
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);

        // -------------- setup interrupts
        interruptHub = Interrupts.init();

        //interruptHub.addObserver(new ScreenOutput(), InterruptHub.ALL_EXTERNAL);
        interruptHub.addObserver(new AliveIndicator(), Interrupts.TIMER);
        interruptHub.addObserver(new KeyboardInterruptReceiver(), Interrupts.KEYBOARD);

        Interrupts.enable();

        // switch to advanced memory manager
        memoryManager = new LinkedListMemoryManager();

        // -------------- Run Tests
        TestRunner.run(1); // run test suite and show result

        // enable paging
        Paging.enable();

        // -------------- setup and run task manager
        taskManager = new TaskManager();
        taskManager.addInputDevice(new Keyboard(new KeyboardLayoutDE()));
        taskManager.requestStart(new Shell());

        TaskManager.saveStackCheckpoint();

        while (true) {
            if (Kernel.doGC) {
                // doGC is set by the shell command GarbageCollection gc
                Kernel.memoryManager.gc();
                Kernel.doGC = false;
                gcRun++;
            }
            /*if (gcRun == 2){
                LowlevelLogging.debug("into loop");
            }*/
            taskManager.loop();
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

    /*
        Puts the processor to sleep until the next interrupt. (Most likely system timer)
     */
    @SJC.Inline
    public static void sleep(){
        MAGIC.inline(0xF4);
    }

    /* just stops forever

    may be used from textual call*/
    @SJC.GenCode
    public static void stop(){
        Interrupts.disable();
        sleep();
    }

}
