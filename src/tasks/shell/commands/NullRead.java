package tasks.shell.commands;

import datastructs.RingBuffer;

public class NullRead extends Command{

    @Override
    public String getCmd() {
        return "nullr";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        int i = MAGIC.rMem32(6);
    }
}
