package kernel.memory;

import datastructs.subtypes.MemAreaArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.TaskManager;
import kernel.interrupts.core.Interrupts;
import rte.SClassDesc;

public class MarkAndSweepGarbageCollector extends GarbageCollector{
    public int deleted = 0;
    public int visitedRootObjects = 0;
    public int visitedHeapObjects = 0;
    public int heapObjects = 0;
    public int rootObjects = 0;
    public int invalid = 0;
    public int logAddr = 1024*1024*6; //6mb

    @Override
    public void run(MemoryManager mgr) {
        /*if (Kernel.gcRun == 1){
            LowlevelLogging.debug("gc run");
        }*/
        heapObjects = 0;
        visitedRootObjects = 0;
        deleted = 0;
        rootObjects = 0;
        invalid = 0;

        //log up
        MAGIC.wMem32(logAddr, 0x01234567); logAddr += 4;

        if (MemoryManager.firstObj == null){
            LowlevelLogging.debug("There are no objects on the heap. Thats impossible. YOUR GC RUNS AMOK");
            Kernel.stop();
        }

        markAllUnused();

        /*if (Kernel.gcRun == 1){
            LowlevelLogging.debug("marking done");
        }*/

        markUsedByImage();

        deleteUnused(mgr);


        /*if (Kernel.gcRun == 1){
            LowlevelLogging.debug("gc deletion done");
            while(true){};
        }*/


        LowlevelOutput.printInt(deleted, 10, 5, 0, 0, Color.DEFAULT_COLOR);
        //LowlevelLogging.debug("Deleted Objects:");

    }

    @SJC.Inline
    public void deleteUnused(MemoryManager mgr) {
        // recycle all still kill marked objects
        Object currentObj = MemoryManager.firstObj;
        Object last = null;
        while(currentObj != null) {
            Object next = currentObj._r_next;

            if(currentObj._s_gcUsedBy == 0){
                if (currentObj instanceof MemAreaArrayList){
                    LowlevelLogging.debug("deleting memAreaArrayList");
                }
                if (currentObj instanceof TaskManager){
                    LowlevelLogging.debug("deleting taskManager");
                }

                SClassDesc cd = currentObj._r_type;
                if (cd.name != null
                        && !cd.name.equals("String")
                        && !cd.name.equals("SArray")) {
                    LowlevelOutput.printStr(String.concat(cd.name, "                           "), 0, 0, Color.GREY);
                    Kernel.wait(1);
                } else {
                    LowlevelOutput.printHex(MAGIC.cast2Ref(currentObj), 10, 0, 1, Color.GREY);
                    LowlevelOutput.printHex(MAGIC.cast2Ref(cd), 10, 0, 2, Color.BLUE);
                    //LowlevelOutput.printHex(, 10, 0, 1, Color.GREY);
                    //LowlevelLogging.debug("name is null");
                    invalid++;
                    /*LowlevelLogging.printHexdump(MAGIC.cast2Ref(cd)-16);
                    Interrupts.disable();
                    Kernel.hlt();*/
                }

                mgr.deallocate(currentObj);
                deleted++;
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

        }
    }

    @SJC.Inline
    public void markAllUnused() {
        Object currentObj = MemoryManager.firstObj;

        // mark all objects on heap with kill mark = 0
        while(currentObj != null){
            MAGIC.assign(currentObj._s_gcUsedBy, 0);
            currentObj = currentObj._r_next;
            heapObjects++;
        }
    }

    /**
     * remove mark on used objects (objects accessable from one of the pre created in image objects)
     */
    @SJC.Inline
    public void markUsedByImage() {
        BasicMemoryManager.ImageInfo image = (BasicMemoryManager.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        Object rootObj = MAGIC.cast2Obj(image.firstObjInImageAddr);

        // loop over all root objects
        while(rootObj != null){
            iterativeMarkUsed(rootObj);

            rootObj = rootObj._r_next;
            rootObjects++;
        }
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

                //log up
                MAGIC.wMem32(logAddr, 0x0000000B); logAddr += 4;
                MAGIC.wMem32(logAddr, MAGIC.cast2Ref(currentObj));  logAddr += 4;
                MAGIC.wMem32(logAddr, currentObj._s_gcUsedBy);  logAddr += 4;

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

                    //log descend
                    MAGIC.wMem32(logAddr, 0x0000DE3C); logAddr += 4;
                    MAGIC.wMem32(logAddr, MAGIC.cast2Ref(currentObj));  logAddr += 4;
                    MAGIC.wMem32(logAddr, MAGIC.cast2Ref(child) );  logAddr += 4;

                    currentObj = child;
                    isFirstTime = true;

                    if (MAGIC.cast2Ref(child) >= MAGIC.cast2Ref(MemoryManager.firstObj)){
                        visitedHeapObjects++;
                    } else {
                        visitedRootObjects++;
                    }
                    break;
                }
            }

        } while (currentObj != rootObj);

    }

}
