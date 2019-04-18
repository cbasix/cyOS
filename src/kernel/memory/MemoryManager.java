package kernel.memory;

import io.LowlevelLogging;
import kernel.Kernel;
import rte.SClassDesc;

public abstract class MemoryManager {
    protected static Object firstObj;
    protected static Object lastObj;

    // all memory managers must take over from BasicMemory manager!
    public abstract Object allocate(int scalarSize, int relocEntries, SClassDesc type);
    public abstract void deallocate(Object o);

    @SJC.Inline
    public static int getAllignedSize(int scalarSize, int relocEntries){
        // allign scalar size to 4 byte
        scalarSize = (scalarSize + 0x3) & ~0x3;

        // calculate memory requirements
        return scalarSize + relocEntries * MAGIC.ptrSize;
    }

    public static Object createObject(int scalarSize, int relocEntries, SClassDesc type, int startAddr, int objSize){
        // clear allocated memory
        for (int i = 0; i < objSize/4; i++) {
            MAGIC.wMem32(startAddr + i*4, 0x00000000);
        }

        // calculate object address inside allocated memory
        int objAddr = startAddr + relocEntries * MAGIC.ptrSize;

        Object newObject = MAGIC.cast2Obj(objAddr);

        // fill kernel fields of object
        MAGIC.assign(newObject._r_type, type);
        MAGIC.assign(newObject._r_relocEntries, relocEntries);
        MAGIC.assign(newObject._r_scalarSize, scalarSize);

        if (lastObj == null) {
            // first object ever, save it for having a startpoint for _r_next iteration GC (later on)
            firstObj = newObject;

        } else {
            // set r_next on last object
            MAGIC.assign(lastObj._r_next, newObject);
        }

        lastObj = newObject;

        return newObject;
    }

}
