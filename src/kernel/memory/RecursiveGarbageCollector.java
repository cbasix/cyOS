package kernel.memory;

import io.LowlevelLogging;
import kernel.Kernel;

public class RecursiveGarbageCollector extends MarkAndSweepGarbageCollector{

    private static class RefList extends STRUCT {
        @SJC(count = 0)
        int[] refs;
    }

    /**
     * remove mark on used objects (objects accessable from one of the pre created in image objects)
     */
    public void markUsedByImage() {
        BasicMemoryManager.ImageInfo image = (BasicMemoryManager.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        Object rootObj = MAGIC.cast2Obj(image.firstObjInImageAddr);

        // loop over all root objects
        while(rootObj != null){
            MAGIC.assign(rootObj._s_gcUsedBy, 0);
            rootObj = rootObj._r_next;
            rootObjects++;
        }

        while(rootObj != null){
            recursiveMarkUsed(rootObj);
            rootObj = rootObj._r_next;
            rootObjects++;
        }


    }

    public void recursiveMarkUsed(Object obj){
        if (obj._s_gcUsedBy != 0){
            return;
        }

        // set used
        MAGIC.assign(obj._s_gcUsedBy, 1);

        RefList refList = (RefList) MAGIC.cast2Struct(MAGIC.cast2Ref(obj) - obj._r_relocEntries*MAGIC.ptrSize );
        int refSize = obj._r_relocEntries - MAGIC.getInstRelocEntries("Object"); // ignore the reloc entrys on object class

        LowlevelLogging.debug(String.from(refSize));
        Kernel.stop();

        for (int refNo = 0; refNo < refSize; refNo++){
            recursiveMarkUsed(MAGIC.cast2Obj(refList.refs[refNo]));
        }
    }

}
