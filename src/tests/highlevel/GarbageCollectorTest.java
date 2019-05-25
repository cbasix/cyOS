package tests.highlevel;

import datastructs.ArrayList;
import drivers.virtio.RawMemoryContainer;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.memory.GarbageCollector;
import kernel.memory.MarkAndSweepGarbageCollector;
import kernel.memory.RecursiveGarbageCollector;
import rte.SClassDesc;

public class GarbageCollectorTest {
    public static class TestObject{
        int[] data;
        int[] data1;
    }

    public static int test(){

  /*      ArrayList a = new ArrayList();
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

    */    RecursiveGarbageCollector gc = new RecursiveGarbageCollector();

      /*  gc.recursiveMarkUsed(a);

        // root objects stay marked as "unused" but never get deleted, because they are not on heap
        //if(a._s_gcUsedBy != 0){ return 805;}
        if(a._s_gcUsedBy != 1){ return 805;}
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
        }*//*
        //if(c._s_gcUsedBy != MAGIC.cast2Ref(b)){ return 809;}

        if(b._s_gcUsedBy == 0){ return 806;}
        if(c._s_gcUsedBy == 0){ return 807;}

        // d should still be marked for deallocation
        if(d._s_gcUsedBy != 0){ return 812;}
        // gc object should not be touched
        if(gc._s_gcUsedBy != -1){ return 810;}


        // full run over all objects three times
        gc.markAllUnused();
        gc.markUsedByImage();
        gc.markAllUnused();
        gc.markUsedByImage();
        gc.markAllUnused();
        gc.markUsedByImage();
        // it would have deleted itself ... which is never correct
        if(gc._s_gcUsedBy == -1){ return 888;}

*/
        // specialy prepared object


       /* RawMemoryContainer r = new RawMemoryContainer(100);

        Object o = MAGIC.cast2Obj(r.getRawAddr()+20);
        MAGIC.assign(o._r_type, (SClassDesc) MAGIC.clssDesc("TestObject"));
        TestObject t = (TestObject) o;

        int[] nextFree = new int[1];
        int addr = MAGIC.cast2Ref(nextFree);

        MAGIC.assign(t._r_relocEntries, 4);
        // plant "mines" before and after
        MAGIC.assign(t._r_type, new SClassDesc());
        MAGIC.assign(t._r_next, new Object());
        t.data = new int[1];
        t.data1 = new int[1];
        MAGIC.wMem32(MAGIC.addr(t.data1) - MAGIC.ptrSize, addr);

        gc.recursiveMarkUsed(t);

        //if ( t._s_gcUsedBy != 1) { return 890; }
        if ( t.data1._s_gcUsedBy == 0) { return 892; }
        if ( t.data._s_gcUsedBy == 0) {
            LowlevelOutput.printStr(String.from(t.data._s_gcUsedBy), 50, 3, Color.RED);
            return 893; }


        if ( t._r_type._s_gcUsedBy != -1) { return 894; }
        if ( nextFree._s_gcUsedBy > 0) {
            LowlevelOutput.printStr(String.from(nextFree._s_gcUsedBy), 50, 3, Color.RED);
            return 895; }
*/
        return 0;
    }
}
