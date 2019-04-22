package tasks.shell.commands;

import datastructs.RingBuffer;

public class NullWrite extends Command{

    @Override
    public String getCmd() {
        return "nullw";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        MAGIC.wMem32(6, 55);
    }
}
