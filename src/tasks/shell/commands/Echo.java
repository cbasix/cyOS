package tasks.shell.commands;

import datastructs.RingBuffer;
import tasks.LogEvent;

public class Echo extends Command{
    @Override
    public String getCmd() {
        return "echo";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        //LowlevelLogging.debug(String.join(args, " "), LowlevelLogging.ERROR);
        shellMessageBuffer.push(new LogEvent(String.join(args, " ")));
    }
}
