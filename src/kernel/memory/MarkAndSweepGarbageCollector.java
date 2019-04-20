package kernel.memory;

import datastructs.subtypes.MemAreaArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class MarkAndSweepGarbageCollector extends GarbageCollector{
    private int deleted = 0;

    @Override
    public void run(MemoryManager mgr) {
        Object currentObj = MemoryManager.firstObj;

        if (currentObj == null){
            LowlevelLogging.debug("There are no objects on the heap. Thats impossible. YOUR GC RUNS AMOK");
            while (true);
        }

        int cnt = 0;
        // mark all objects on heap with kill mark = 0
        do {
            MAGIC.assign(currentObj._s_gcUsedBy, 0);
            currentObj = currentObj._r_next;
            cnt++;
        } while(currentObj != null);


        // remove mark on used objects (objects accessable from one of the pre created in image objects)
        BasicMemoryManager.ImageInfo image = (BasicMemoryManager.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        Object rootObj = MAGIC.cast2Obj(image.firstObjInImageAddr);

        // loop over all root objects
        while(rootObj != null){
            iterativeMarkUsed(rootObj);

            rootObj = rootObj._r_next;
        }

        /*LowlevelOutput.printInt(MAGIC.getInstRelocEntries("Object"), 10, 5, 0, 10, Color.DEFAULT_COLOR);
        LowlevelOutput.printInt(root, 10, 5, 0, 2, Color.DEFAULT_COLOR);
        LowlevelOutput.printInt(descended, 10, 5, 0, 3, Color.DEFAULT_COLOR);
        LowlevelOutput.printInt(cnt, 10, 5, 0, 4, Color.DEFAULT_COLOR);
        LowlevelOutput.printInt(maxDepth, 10, 5, 4, 5, Color.DEFAULT_COLOR);
        LowlevelLogging.debug("root / descended / total obj");
        LowlevelLogging.debug("root / descended / total obj");
        LowlevelLogging.debug("root / descended / total obj");
        LowlevelLogging.debug("root / descended / total obj");*/


        // recycle all still kill marked objects
        currentObj = MemoryManager.firstObj;
        Object last = null;
        do {
            Object next = currentObj._r_next;

            if(currentObj._s_gcUsedBy == 0){
                deleted++;
                if (currentObj instanceof MemAreaArrayList){
                    LowlevelLogging.debug("deleting memAreaArrayList");

                }
                mgr.deallocate(currentObj);
                if(next == null){
                    // its the last object -> update memmanager
                    MemoryManager.lastObj = last;
                    //LowlevelLogging.debug("deleting last ");
                }

                if (last == null) {
                    // its the first obj -> update memmanager
                    MemoryManager.firstObj = next;
                    //LowlevelLogging.debug("deleting first ");
                   /* if (next == null) {
                        LowlevelLogging.debug("but next is null!");
                    }*/


                } else {
                    // its somewhere in the middle update the last objects _r_next to the following object / or null if there is none
                    MAGIC.assign(last._r_next, next);
                }
            } else {
                // if the current obj was not deleted it becomes the "last" one for the next obj
                last = currentObj;
            }

            // go to the next one
            currentObj = next;

        } while(currentObj != null);

        LowlevelOutput.printInt(deleted, 10, 5, 0, 0, Color.DEFAULT_COLOR);
        //LowlevelLogging.debug("Deleted Objects:");

    }

    @SJC.Inline
    public void iterativeMarkUsed(Object rootObj){
        int line = 0;
        // root objects are inside image (and not on heap) means: they never get killed anyway.
        // so the kill mark can be set here to simplify the algorithm
        MAGIC.assign(rootObj._s_gcUsedBy, 0);

        Object currentObj = rootObj;

        boolean isFirstTime = true;
        // mark all outgoing pointers as used
        do {
            if (isFirstTime){
                isFirstTime = false;

            } else {
                /*LowlevelOutput.printHex(currentObj._s_gcUsedBy,8, 21, line, Color.GREY);
                LowlevelOutput.printStr("<-", 29, line, Color.RED);
                LowlevelOutput.printHex(MAGIC.cast2Ref(currentObj),8, 31, line, Color.GREY);
                line++;*/

                // go back in dependency hierarchy
                currentObj = MAGIC.cast2Obj(currentObj._s_gcUsedBy);


            }

            for (int i = MAGIC.getInstRelocEntries("Object")+1; i <= currentObj._r_relocEntries; i++) {
                Object child = MAGIC.cast2Obj(MAGIC.rMem32(MAGIC.cast2Ref(currentObj) - MAGIC.ptrSize * i));
                if (child != null && child._s_gcUsedBy == 0) {
                    MAGIC.assign(child._s_gcUsedBy, MAGIC.cast2Ref(currentObj));
                    // descend into hierarchy
                    /*LowlevelOutput.printHex(MAGIC.cast2Ref(currentObj),8, 21, line, Color.GREY);
                    LowlevelOutput.printStr("->", 29, line, Color.GREEN);
                    LowlevelOutput.printHex(MAGIC.cast2Ref(child),8, 31, line, Color.GREY);
                    line++;*/

                    currentObj = child;
                    isFirstTime = true;

                    break;
                }
            }

        } while (currentObj != rootObj);
    }

}
