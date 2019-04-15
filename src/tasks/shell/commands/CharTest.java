package tasks.shell.commands;

import datastructs.RingBuffer;
import tasks.LogEvent;

public class CharTest extends Command{
    @Override
    public String getCmd() {
        return "ctest";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        char[] out = new char[255];
        int j = 0;
        for(int i = 0; i < 255 ; i++){
            out[j++] = (char) i;
        }
        //LowlevelLogging.debug(String.join(args, " "), LowlevelLogging.ERROR);
        shellMessageBuffer.push(new LogEvent(new String(out)));
    }
}
