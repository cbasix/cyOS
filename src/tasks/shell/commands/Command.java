package tasks.shell.commands;

import kernel.datastructs.RingBuffer;

public abstract class Command {
    public abstract String getCmd();
    public abstract void execute(RingBuffer shellMessageBuffer, String[] args);

}
