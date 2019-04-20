package tests.highlevel;

import datastructs.subtypes.MemAreaArrayList;
import io.Color;
import io.LowlevelOutput;
import kernel.memory.ArrayListMemoryManager;
import kernel.memory.MemArea;

public class ArrayListMemoryManagerTest {
    public static int test(){

        MemAreaArrayList list = new MemAreaArrayList();
        list.add(new MemArea(0, 10));
        list.add(new MemArea(20, 10));
        list.add(new MemArea(50, 16));

        // join on top
        ArrayListMemoryManager.insertArea(66, 12, list);
        if (list.size() != 3) {
            LowlevelOutput.printInt(list.size(), 10, 10, 45, 15, Color.GREEN);
            return 700;
        }
        if (list.get(2).start != 50) {
            LowlevelOutput.printInt(list.get(2).start, 10, 10, 45, 15, Color.GREEN);
            return 701;
        }
        if (list.get(2).size != 28) {return 702;}

        // join below
        ArrayListMemoryManager.insertArea(40, 10, list);
        if (list.size() != 3) {return 703;}
        if (list.get(2).start != 40) {return 704;}
        if (list.get(2).size != 38) {return 705;}

        // join top and below
        ArrayListMemoryManager.insertArea(10, 10, list);
        if (list.size() != 2) {return 706;}
        if (list.get(0).start != 0) {
            LowlevelOutput.printInt(list.get(1).start, 10, 10, 45, 15, Color.GREEN);
            return 708;
        }
        if (list.get(0).size != 30) {return 710;}

        // add independent
        ArrayListMemoryManager.insertArea(80, 10, list);
        if (list.size() != 3) {return 712;}
        if (list.get(2).start != 80) {return 714;}
        if (list.get(2).size != 10) {return 716;}


        return 0;
    }
}
