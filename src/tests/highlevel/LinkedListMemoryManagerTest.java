package tests.highlevel;

import datastructs.subtypes.MemAreaLinkedList;
import io.Color;
import io.LowlevelOutput;
import kernel.memory.LinkedListMemoryManager;
import kernel.memory.MemArea;

public class LinkedListMemoryManagerTest {
    public static int test(){
        MemAreaLinkedList list = new MemAreaLinkedList();
        MemAreaLinkedList.MemAreaIterator iter = list.iter();

        iter.insert(new MemArea(0, 10));
        iter.insert(new MemArea(20, 10));
        iter.insert(new MemArea(50, 20));

        // join on top
        LinkedListMemoryManager.insertArea(70, 10, list.iter());

        int size = 0;
        iter.gotoStart();
        while(iter.next()){size++;}
        if (size != 3) {
            LowlevelOutput.printInt(size, 10, 10, 45, 15, Color.RED);
            return 700;
        }

        //3rd elem must start at 50 and have len 80
        iter.gotoStart();
        iter.next();
        iter.next();
        iter.next();
        if (iter.get().start != 50) {
            LowlevelOutput.printInt(iter.get().start, 10, 10, 45, 15, Color.RED);
            printList(list);
            return 701;
        }
        if (iter.get().size != 20+10) {return 702;}

        // join below
        LinkedListMemoryManager.insertArea(40, 10, list.iter());
        size = 0;
        iter.gotoStart();
        while(iter.next()){size++;}
        if (size != 3) {return 703;}
        iter.gotoStart();
        iter.next();
        iter.next();
        iter.next();
        if (iter.get().start != 40) {
            printList(list);
            return 704;
        }
        if (iter.get().size != 20+10+10) {return 705;}

        // join top and below
        LinkedListMemoryManager.insertArea(10, 10, list.iter());
        size = 0;
        iter.gotoStart();
        while(iter.next()){size++;}
        if (size != 2) {

            LowlevelOutput.printInt(size, 10, 10, 45, 15, Color.RED);
            return 706;
        }
        iter.gotoStart();
        iter.next();
        if (iter.get().start != 0) {
            LowlevelOutput.printInt(iter.get().start, 10, 10, 45, 15, Color.RED);
            return 708;
        }
        if (iter.get().size != 30) {return 710;}

        // insert independent
        LinkedListMemoryManager.insertArea(90, 10, list.iter());
        size = 0;
        iter.gotoStart();
        while(iter.next()){size++;}
        if (size != 3) {
            LowlevelOutput.printInt(size, 10, 10, 45, 15, Color.RED);
            printList(list);
            return 712;
        }
        iter.gotoStart();
        iter.next();
        iter.next();
        iter.next();
        if (iter.get().start != 90) {return 714;}
        if (iter.get().size != 10) {return 716;}

        // they all must be one block in the end
        LinkedListMemoryManager.insertArea(33423000, 28, list.iter());
        LinkedListMemoryManager.insertArea(33422800, 64, list.iter());
        LinkedListMemoryManager.insertArea(33422892, 64, list.iter());
        LinkedListMemoryManager.insertArea(33422956, 44, list.iter());
        LinkedListMemoryManager.insertArea(33423028, 44, list.iter());
        LinkedListMemoryManager.insertArea(33422864, 28, list.iter());

        size = 0;
        iter.gotoStart();
        while(iter.next()){size++;}
        if (size != 4) {
            LowlevelOutput.printInt(size, 10, 10, 45, 15, Color.RED);
            printList(list);
            return 722;
        }


        return 0;
    }

    static void printList(MemAreaLinkedList list){
        MemAreaLinkedList.MemAreaIterator iter = list.iter();
        int line = 0;
        while(iter.next()){
            MemArea a = iter.get();
            LowlevelOutput.printStr("Start: ", 45, line, Color.GREY);
            LowlevelOutput.printInt(a.start, 10, 3, 52, line, Color.GREY);
            LowlevelOutput.printStr("Len: ", 56, line, Color.GREY);
            LowlevelOutput.printInt(a.size, 10, 3, 61, line++, Color.GREY);
        }
    }
}
