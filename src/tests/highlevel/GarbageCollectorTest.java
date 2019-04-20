package tests.highlevel;

import datastructs.ArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.memory.GarbageCollector;
import kernel.memory.MarkAndSweepGarbageCollector;

public class GarbageCollectorTest {
    public static int test(){

        ArrayList a = new ArrayList();
        ArrayList b = new ArrayList();
        ArrayList c = new ArrayList();
        ArrayList d = new ArrayList();

        // mark
        MAGIC.assign(a._s_gcUsedBy, 0);
        MAGIC.assign(b._s_gcUsedBy, 0);
        MAGIC.assign(c._s_gcUsedBy, 0);
        MAGIC.assign(d._s_gcUsedBy, 0);


        // add usages
        a._add(b);
        b._add(c);

        LowlevelOutput.printHex(MAGIC.cast2Ref(a),10, 10, 19, Color.GREY);
        LowlevelOutput.printHex(MAGIC.cast2Ref(b),10, 10, 20, Color.GREY);
        LowlevelOutput.printHex(MAGIC.cast2Ref(c),10, 10, 21, Color.GREY);
        LowlevelOutput.printHex(MAGIC.cast2Ref(d),10, 10, 22, Color.GREY);

        MarkAndSweepGarbageCollector gc = new MarkAndSweepGarbageCollector();

        gc.iterativeMarkUsed(a);

        // root objects stay marked as "unused" but never get deleted, because they are not on heap
        if(a._s_gcUsedBy != 0){ return 805;}
        // a and be should be used by their predecessor
        /*if(b._s_gcUsedBy != MAGIC.cast2Ref(a)){
            LowlevelOutput.printHex(MAGIC.cast2Ref(a),  10, 10, 15, Color.GREEN);
            LowlevelOutput.printHex(b._s_gcUsedBy,  10, 10, 16, Color.RED);

            LowlevelOutput.printHex(MAGIC.cast2Ref(a),10, 10, 19, Color.GREY);
            LowlevelOutput.printHex(MAGIC.cast2Ref(b),10, 10, 20, Color.GREY);
            LowlevelOutput.printHex(MAGIC.cast2Ref(c),10, 10, 21, Color.GREY);
            LowlevelOutput.printHex(MAGIC.cast2Ref(d),10, 10, 22, Color.GREY);
            LowlevelOutput.printHex(MAGIC.cast2Ref(gc),10, 10, 23, Color.GREY);

            LowlevelLogging.printHexdump(MAGIC.cast2Ref(a)-64);
            return 808;
        }*/
        //if(c._s_gcUsedBy != MAGIC.cast2Ref(b)){ return 809;}

        if(b._s_gcUsedBy == 0){ return 806;}
        if(c._s_gcUsedBy == 0){ return 807;}

        // d should still be marked for deallocation
        if(d._s_gcUsedBy != 0){ return 812;}
        // gc object should not be touched
        if(gc._s_gcUsedBy != -1){ return 810;}



        return 0;
    }
}
