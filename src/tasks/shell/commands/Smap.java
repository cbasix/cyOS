package tasks.shell.commands;

import datastructs.RingBuffer;
import datastructs.subtypes.MemAreaLinkedList;
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
        MemAreaLinkedList memAreas = SystemMemoryMap.getAvailableGtOneMb();
        MemAreaLinkedList.MemAreaIterator iter = memAreas.iter();
        shellMessageBuffer.push(new LogEvent("Available memory areas"));
        while(iter.next()){
            MemArea mem = iter.get();

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
