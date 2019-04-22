package tasks.shell.commands;

import kernel.Kernel;
import datastructs.RingBuffer;
import tasks.Blocking;
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
            shellMessageBuffer.push(new LogEvent("Usage: exec <taskname> (Currently there are only 'editor' and 'blocking')"));
        } else {
            if (args[1].equals("editor")){
                Kernel.taskManager.requestStart(new Editor());

            } else if (args[1].equals("blocking")){
                Kernel.taskManager.requestStart(new Blocking());

            } else {
                shellMessageBuffer.push(new LogEvent("Task not found. (Currently there are only 'editor' and 'blocking')"));
            }
        }
    }
}
