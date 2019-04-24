package kernel;

import drivers.InputDevice;
import datastructs.subtypes.InputDeviceArrayList;
import datastructs.subtypes.TaskArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.interrupts.core.Interrupts;
import tasks.Task;

public class TaskManager {
    static int savedEbp;
    static int savedEsp;
    private static int currentEbp;

    private TaskArrayList runningTasks;
    private TaskArrayList tasksToStart;
    private TaskArrayList tasksToFocus;
    private TaskArrayList tasksToStop;

    private InputDeviceArrayList inputs;

    private Task focusedTask;
    private Task currentlyRunningTask;

    TaskManager() {
        runningTasks = new TaskArrayList();
        tasksToStart = new TaskArrayList();
        tasksToFocus = new TaskArrayList();
        tasksToStop = new TaskArrayList();
        inputs = new InputDeviceArrayList();
    }

    private void startTask(Task task, boolean withFocus){
        task.onStart();
        if (withFocus){
            focusedTask = task;
            focusedTask.onFocus();
        }
        runningTasks.add(task);
    }

    private void stopTask(Task task){
        task.onStop();
        runningTasks.remove(task);
    }

    public void requestFocus(Task task){
        tasksToFocus.add(task);
    }

    public void requestStart(Task task){
        tasksToStart.add(task);
    }

    public void requestStop(Task task){
        tasksToStop.add(task);
    }

    public void loop() {
        while (true) {
            if (Kernel.doGC) {
                // doGC is set by the shell command GarbageCollection gc
                Kernel.memoryManager.gc();
                Kernel.doGC = false;
            }


            // check if kernel can put processor to sleep
            boolean nothingTodo = true;
            for (int i = 0; i < runningTasks.size(); i++) {
                if (runningTasks.get(i).stdin.count() > 0) {
                    nothingTodo = false;
                }
            }
            if (nothingTodo) {
                Kernel.hlt();
            }

            // read input into currently focused task
            if (focusedTask != null) {
                for (int i = 0; i < inputs.size(); i++) {
                    inputs.get(i).readInto(focusedTask.stdin);
                }

                // run current task
                currentlyRunningTask = focusedTask;
                focusedTask.onTick();
            }

            // run background tasks
            for (int i = 0; i < runningTasks.size(); i++) {
                if (runningTasks.get(i) != focusedTask) {
                    currentlyRunningTask = runningTasks.get(i);
                    runningTasks.get(i).onBackgroundTick();
                }
            }

            // stop pending toStop Tasks  (backwards to allow deletion ;)
            for (int i = tasksToStop.size() - 1; i >= 0; i--) {
                Task t = tasksToStop.get(i);
                currentlyRunningTask = t;
                t.onStop();

                // if task had focus return it to the shell
                if (focusedTask == t) {
                    focusedTask = runningTasks.get(0);
                    currentlyRunningTask = focusedTask;
                    focusedTask.onFocus();
                }

                runningTasks.remove(t);
                tasksToStop.remove(t);
            }

            // start pending new tasks  (backwards to allow deletion ;)
            for (int i = tasksToStart.size() - 1; i >= 0; i--) {
                Task t = tasksToStart.get(i);
                currentlyRunningTask = t;
                t.onStart();
                runningTasks.add(t);
                tasksToStart.remove(t);
            }

            // focus pending toFocus Tasks  (backwards to allow deletion ;)
            for (int i = tasksToFocus.size() - 1; i >= 0; i--) {
                Task t = tasksToFocus.get(i);
                currentlyRunningTask = t;
                t.onFocus();
                focusedTask = t;
                tasksToFocus.remove(t);
            }
        }
    }

    public void addInputDevice(InputDevice inputDevice) {
        inputs.add(inputDevice);
    }

    //@SJC.Inline
    public static void saveStackCheckpoint(){
        //Ablage der äußeren (aufrufenden) Stack-Registerwerte in Variablen
        //int currentEbp = 0;
        MAGIC.inline(0x89, 0x2D); MAGIC.inlineOffset(4, currentEbp); //mov [addr(v1)],ebp

        savedEsp = currentEbp;
        savedEbp = MAGIC.rMem32(currentEbp+2*MAGIC.ptrSize); // see ab 5a (method has no params, so only eip and epb are there

        /*LowlevelLogging.printHexdump(savedEsp);
        Interrupts.disable();
        Kernel.hlt();*/
    }

    /*
    ATTENTION THIS DOES BLACK STACK MAGIC !
     */
    public static void killCurrentTask(int intNo){
        LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);

        // remove the killed task from all task lists
        Kernel.taskManager.runningTasks.remove(Kernel.taskManager.currentlyRunningTask);
        Kernel.taskManager.tasksToFocus.remove(Kernel.taskManager.currentlyRunningTask);
        Kernel.taskManager.tasksToStop.remove(Kernel.taskManager.currentlyRunningTask);
        Kernel.taskManager.tasksToStart.remove(Kernel.taskManager.currentlyRunningTask);

        // if the to be killed task was focused -> return focus to pid 0 (normally shell)
        if (Kernel.taskManager.focusedTask == Kernel.taskManager.currentlyRunningTask){
            Kernel.taskManager.focusedTask = null;

           if (Kernel.taskManager.runningTasks.get(0) != null) {
                Kernel.taskManager.requestFocus(Kernel.taskManager.runningTasks.get(0));
            }
        }

        // if there is no pid 0 because we are currently killing the last task, we can not give it focus... show message
        if (Kernel.taskManager.runningTasks.size() == 0
                && Kernel.taskManager.tasksToStart.size() == 0) {

            LowlevelOutput.clearScreen(Color.DEFAULT_COLOR);
            LowlevelOutput.printStr("Contratulations you killed all tasks. Happy rebooting", 10, 13, Color.DEFAULT_COLOR);
            // maybe start a new shell here...
        }
        Interrupts.ack(intNo);

        //Beschreiben der Register aus gespeicherten Variablenwerten
        MAGIC.inline(0x8B, 0x2D); MAGIC.inlineOffset(4, savedEbp); //mov ebp,[addr(v1)]
        MAGIC.inline(0x8B, 0x25); MAGIC.inlineOffset(4, savedEsp); //mov esp,[addr(v1)]

        // reenter main loop
        Interrupts.forceEnable();
        Kernel.taskManager.loop();
    }
}
