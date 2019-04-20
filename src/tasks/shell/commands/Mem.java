package tasks.shell.commands;

import datastructs.RingBuffer;
import datastructs.subtypes.MemAreaLinkedList;
import kernel.Kernel;
import kernel.memory.LinkedListMemoryManager;
import kernel.memory.MemArea;
import tasks.LogEvent;

public class Mem extends Command{
    @Override
    public String getCmd() {
        return "mem";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        shellMessageBuffer.push(new LogEvent("Free memory areas"));
        MemAreaLinkedList memAreas = ((LinkedListMemoryManager)Kernel.memoryManager).getAreas();
        MemAreaLinkedList.MemAreaIterator iter = memAreas.iter();
        int cnt = 0;
        int free = 0;
        while(iter.next()){
            MemArea mem = iter.get();

            shellMessageBuffer.push(
                    new LogEvent(
                            String.concat(
                                    String.concat("Start: ",
                                        String.from(mem.start)
                                    ),
                                    String.concat(
                                        " Size: ",
                                        String.from(mem.size)
                                    )
                            )
                    )
            );
            cnt++;
            free += mem.size;
        }
        shellMessageBuffer.push(new LogEvent(String.concat(
                String.concat("Total Mem areas: ", String.from(cnt)),
                String.concat(" Free mem (bytes): ", String.from(free))
        )));

    }
}
