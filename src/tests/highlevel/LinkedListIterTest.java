package tests.highlevel;

import datastructs.LinkedList;
import io.Color;
import io.LowlevelOutput;

public class LinkedListIterTest {
    public static int test(){

        LinkedList list = new LinkedList();
        LinkedList.Iterator iter = list._iter();

        iter._insert("eins");
        iter._insert("zwei");
        iter._insert("drei");

        // test current position
        if (!((String)iter._get()).equals("drei")) {
            LowlevelOutput.printStr((String)iter._get(), 45, 13, Color.RED);
            return 1012;
        }

        // step through with next and _get elements
        iter.gotoStart();
        iter.next();
        if (!((String)iter._peekNext()).equals("zwei")) {return 1013;}

        if (!((String)iter._get()).equals("eins")) {
            LowlevelOutput.printStr((String)iter._get(), 45, 13, Color.RED);
            return 1014;
        }
        if (!iter.next()) {return 1016;}
        if (!((String)iter._get()).equals("zwei")) {return 1018;}
        if (!((String)iter._peekNext()).equals("drei")) {return 1019;}
        if (!iter.next()) {return 1020;}
        if (!((String)iter._get()).equals("drei")) {
            LowlevelOutput.printStr((String)iter._get(), 45, 13, Color.RED);
            return 1021;
        }
        if (iter.next()) {return 1023;}

        // if next was false the current item should not have changed
        if (!((String)iter._get()).equals("drei")) {
            LowlevelOutput.printStr((String)iter._get(), 45, 13, Color.RED);
            return 1024;
        }

        // test remove last element
        iter.removeCurrent();
        // next must still return false
        if (iter.next()) {return 1025;}
        if (!((String)iter._get()).equals("zwei")) {return 1027;}

        // add via append
        iter._insert("drei");
        if (!((String)iter._get()).equals("drei")) {return 1028;}
        if (!((String)iter._peekPrevious()).equals("zwei")) {
            LowlevelOutput.printStr((String)iter._peekPrevious(), 45, 13, Color.RED);
            return 1030;
        }


        return 0;
    }
}
