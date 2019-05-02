package tasks.shell.commands;

import datastructs.LinkedList;
import datastructs.RingBuffer;
import kernel.memory.MemArea;
import kernel.memory.SystemMemoryMap;
import tasks.LogEvent;

public class Smap extends Command{
    @Override
    public String getCmd() {
        return "smap";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        LinkedList memAreas = SystemMemoryMap.getAvailableGtOneMb();
        LinkedList.Iterator iter = memAreas._iter();
        shellMessageBuffer.push(new LogEvent("Available memory areas"));
        while(iter.next()){
            MemArea mem = (MemArea) iter._get();

            shellMessageBuffer.push(
                    new LogEvent(
                            String.concat(
                                    String.concat("Start: 0x",
                                        String.hexFrom(mem.start)
                                    ),
                                    String.concat(
                                        " Size (kb): ",
                                        String.from(mem.size/1024)
                                    )
                            )
                    )
            );
        }
    }
}
