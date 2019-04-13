package kernel;

import drivers.InputDevice;
import drivers.keyboard.Keyboard;
import io.LowlevelLogging;
import kernel.datastructs.subtypes.InputDeviceArrayList;
import kernel.datastructs.subtypes.TaskArrayList;
import tasks.Task;

public class TaskManager {
    private static TaskArrayList runningTasks;
    private static TaskArrayList tasksToStart;
    private static TaskArrayList tasksToFocus;
    private static TaskArrayList tasksToStop;

    private static InputDeviceArrayList inputs;


    private static Task focusedTask;

    TaskManager() {
        runningTasks = new TaskArrayList();
        tasksToStart = new TaskArrayList();
        tasksToFocus = new TaskArrayList();
        tasksToStop = new TaskArrayList();
        inputs = new InputDeviceArrayList();
    }

    // todo extract task stuff into own scheduler class later on
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

    // todo prettify
    public void loop() {
        while (true){
            // read input into currently focused task
            for (int i = 0; i < inputs.size(); i++) {
                inputs.get(i).readInto(focusedTask.stdin);
            }

            // run current task
            if (focusedTask != null) {
                focusedTask.onTick();
            }

            // run background tasks
            for (int i = 0; i < runningTasks.size(); i++){
                if (runningTasks.get(i) != focusedTask){
                    runningTasks.get(i).onBackgroundTick();
                }
            }

            // stop pending toStop Tasks  (backwards to allow deletion ;)
            for (int i = tasksToStop.size()-1; i >= 0 ; i--) {
                Task t = tasksToStop.get(i);
                t.onStop();

                // if task had focus return it to the shell
                if (focusedTask == t) {
                    focusedTask = runningTasks.get(0);
                    focusedTask.onFocus();
                }

                runningTasks.remove(t);
                tasksToStop.remove(t);
            }

            // start pending new tasks  (backwards to allow deletion ;)
            for (int i = tasksToStart.size()-1; i >= 0 ; i--) {
                Task t = tasksToStart.get(i);
                t.onStart();
                runningTasks.add(t);
                tasksToStart.remove(t);
            }

            // focus pending toFocus Tasks  (backwards to allow deletion ;)
            for (int i = tasksToFocus.size()-1; i >= 0 ; i--) {
                Task t = tasksToFocus.get(i);
                t.onFocus();
                focusedTask = t;
                tasksToFocus.remove(t);
            }
        }
    }

    public void addInputDevice(InputDevice inputDevice) {
        inputs.add(inputDevice);
    }
}
