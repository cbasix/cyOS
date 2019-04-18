package tasks.shell.commands;

import datastructs.RingBuffer;

public class Interrupt extends Command{

    @Override
    public String getCmd() {
        return "INT";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        int i = 0;



        int z = i / i;

        // todo nullpointers no exception here /only with -n
        Command c = null;
        String t = c.getCmd();

    }
}
