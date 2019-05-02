package tasks.shell.commands;

import datastructs.RingBuffer;
import kernel.Kernel;
import kernel.memory.LinkedListMemoryManager;
import kernel.memory.MarkAndSweepGarbageCollector;
import tasks.LogEvent;

public class GarbageCollectionInfo extends Command{
    @Override
    public String getCmd() {
        return "gcinfo";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        // this assumes much... it will break. but it only is for debugging so ...
        MarkAndSweepGarbageCollector gc = (MarkAndSweepGarbageCollector) ((LinkedListMemoryManager)Kernel.memoryManager).gc;
        shellMessageBuffer.push(
                new LogEvent(String.concat("Root obj: ", String.from(gc.rootObjects)))
        );
        shellMessageBuffer.push(
                new LogEvent(String.concat("Heap obj: ", String.from(gc.heapObjects)))
        );
        shellMessageBuffer.push(
                new LogEvent(String.concat("Visited root obj: ", String.from(gc.visitedRootObjects)))
        );
        shellMessageBuffer.push(
                new LogEvent(String.concat("Visited heap obj: ", String.from(gc.visitedHeapObjects)))
        );
        shellMessageBuffer.push(
                new LogEvent(String.concat("Deleted: ", String.from(gc.deleted)))
        );
        shellMessageBuffer.push(
                new LogEvent(String.concat("Invalid: ", String.from(gc.invalid)))
        );
    }
}
