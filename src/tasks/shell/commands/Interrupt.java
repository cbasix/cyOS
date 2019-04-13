package tasks.shell.commands;

import kernel.datastructs.RingBuffer;

public class Interrupt extends Command{

    @Override
    public String getCmd() {
        return "INT";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        int i = 0;

        // no exception here
        Command c = null;
        String t = c.getCmd();

        int z = i / i;

    }
}
