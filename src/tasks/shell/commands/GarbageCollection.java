package tasks.shell.commands;

import datastructs.RingBuffer;
import kernel.Kernel;
import tasks.LogEvent;

public class GarbageCollection extends Command{
    @Override
    public String getCmd() {
        return "gc";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        Kernel.doGC = true;
    }
}
