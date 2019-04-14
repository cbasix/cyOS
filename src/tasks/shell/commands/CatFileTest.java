package tasks.shell.commands;

import datastructs.RingBuffer;
import tasks.LogEvent;

public class CatFileTest extends Command{
    @Override
    public String getCmd() {
        return "cat";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        String t = MAGIC.getNamedString("../../blobs/example.yml");

        shellMessageBuffer.push(new LogEvent(t));
    }
}
