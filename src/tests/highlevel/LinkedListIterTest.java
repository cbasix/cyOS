package tests.highlevel;

import datastructs.LinkedListIter;
import datastructs.subtypes.MemAreaArrayList;
import io.Color;
import io.LowlevelOutput;
import kernel.memory.ArrayListMemoryManager;
import kernel.memory.MemArea;

public class LinkedListIterTest {
    public static int test(){

        LinkedListIter list = new LinkedListIter();

        list._insert("eins");
        list._insert("zwei");
        list._insert("drei");

        // test current position
        if (!((String)list._get()).equals("drei")) {
            LowlevelOutput.printStr((String)list._get(), 45, 13, Color.RED);
            return 1012;
        }

        // step through with next and get elements
        list.gotoFirst();
        if (!((String)list.peekNext()).equals("zwei")) {return 1012;}

        if (!((String)list._get()).equals("eins")) {
            LowlevelOutput.printStr((String)list._get(), 45, 13, Color.RED);
            return 1014;
        }
        if (!list.next()) {return 1016;}
        if (!((String)list._get()).equals("zwei")) {return 1018;}
        if (!((String)list.peekNext()).equals("drei")) {return 1019;}
        if (!list.next()) {return 1020;}
        if (!((String)list._get()).equals("drei")) {
            LowlevelOutput.printStr((String)list._get(), 45, 13, Color.RED);
            return 1021;
        }
        if (list.next()) {return 1023;}

        // if next was false the current item should not have changed
        if (!((String)list._get()).equals("drei")) {
            LowlevelOutput.printStr((String)list._get(), 45, 13, Color.RED);
            return 1024;
        }

        // test remove last element
        list.removeCurrent();
        // next must still return false
        if (list.next()) {return 1025;}
        if (!((String)list._get()).equals("zwei")) {return 1027;}

        // add via append
        list._insert("drei");
        if (!((String)list._get()).equals("drei")) {return 1028;}
        if (!((String)list.peekPrevious()).equals("zwei")) {
            LowlevelOutput.printStr((String)list.peekPrevious(), 45, 13, Color.RED);
            return 1030;
        }


        return 0;
    }
}
