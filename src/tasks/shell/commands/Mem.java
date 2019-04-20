package tasks.shell.commands;

import datastructs.RingBuffer;
import datastructs.subtypes.MemAreaArrayList;
import kernel.Kernel;
import kernel.memory.ArrayListMemoryManager;
import kernel.memory.MemArea;
import kernel.memory.SystemMemoryMap;
import tasks.LogEvent;

public class Mem extends Command{
    @Override
    public String getCmd() {
        return "mem";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        MemAreaArrayList memAreas = ((ArrayListMemoryManager)Kernel.memoryManager).getAreas();
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
                                        " Size: ",
                                        String.from(mem.size)
                                    )
                            )
                    )
            );
        }
    }
}
