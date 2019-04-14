package tasks.shell.commands;

import kernel.Kernel;
import datastructs.RingBuffer;
import tasks.Editor;
import tasks.LogEvent;

public class ExecuteTask extends Command{
    @Override
    public String getCmd() {
        return "exec";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        //LowlevelLogging.debug(String.join(args, " "), LowlevelLogging.ERROR);
        if (args.length <= 1){
            shellMessageBuffer.push(new LogEvent("Usage: exec <taskname>"));
        } else {
            if (args[1].equals("editor")){
                shellMessageBuffer.push(new LogEvent("Editor started"));
                Kernel.taskManager.requestStart(new Editor());
            } else {
                shellMessageBuffer.push(new LogEvent("Task not found. (Currently there is only 'editor')"));
            }
        }
    }
}
