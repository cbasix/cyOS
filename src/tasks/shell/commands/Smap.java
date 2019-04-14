package tasks.shell.commands;

import datastructs.RingBuffer;
import datastructs.subtypes.MemAreaArrayList;
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
        MemAreaArrayList memAreas = SystemMemoryMap.getAvailableGtOneMb();
        for(int i = 0; i < memAreas.size(); i++) {
            MemArea mem = memAreas.get(i);
            shellMessageBuffer.push("Available memory areas:");
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
