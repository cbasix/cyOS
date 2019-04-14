package tests.highlevel;

import io.LowlevelLogging;
import datastructs.ArrayList;

public class ArrayListTest {
    public static int test(){
        ArrayList a = new ArrayList();

        a._add("eins");
        String r = (String) a._get(0);
        if (!r.equals("eins")) {
            LowlevelLogging.debug(r, LowlevelLogging.ERROR);
            return 601;
        }
        if (a.size() != 1) { return 602;}

        a._add("zwei");
        if (a.size() != 2) { return 603;}

        a._add("drei");
        a._add("vier");
        a._add("fünf");
        if (a.size() != 5) { return 604;}

        // test the grow
        for(int i = 0; i < 40; i++){
            a._add("vierzig mal");
        }
        if (a.size() != 45) { return 608;}

        r = (String) a._get(2);
        if (!r.equals("drei")) {
            LowlevelLogging.debug(r, LowlevelLogging.ERROR);
            return 603;
        }
        r = (String) a._get(3);
        if (!r.equals("vier")) { return 670;}

        r = (String) a._get(4);
        if (!r.equals("fünf")) { return 675;}

        // access to non existing element should return null
        r = (String) a._get(4408);
        if (r != null) { return 676;}


        if (a.size() != 45) { return 680;}
        //LowlevelLogging.debug("ARRAY LIST TESTED");

        return 0;
    }
}
