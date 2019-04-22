package tasks.shell.commands;

import datastructs.RingBuffer;

public class Nullpointer extends Command{

    @Override
    public String getCmd() {
        return "nullptr";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        Echo o = null; //(Echo) MAGIC.cast2Obj(15);
        String t = o.getCmd();
    }
}
