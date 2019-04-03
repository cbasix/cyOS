package rte;

import io.Color;
import io.LowlevelOutput;
import io.LowlevelLogging;
import kernel.Interrupts;

public class DynamicRuntime {

    public static final int POINTER_SIZE = MAGIC.ptrSize;
    private static int firstObjAddr;
    private static int lastObjAddr;
    private static int nextFreeAddr;
    public static int interuptDescriptorTableAddr;


    public static class ImageInfo extends STRUCT {
        public int start, size; //, classDescStart, codebyteAddr, firstObjInImageAddr, ramInitAddr;
    }

    public static int getNextFreeAddr() {
        return nextFreeAddr;
    }

    public static void initializeMemoryPointers() {
        ImageInfo image = (ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        // make room for interrupt table after image
        // allign to 4 byte, (last two address bits zero)
        interuptDescriptorTableAddr =  (image.start + image.size + 0x3) & ~0x3;

        // write marker into mem
        MAGIC.wMem32(interuptDescriptorTableAddr, 0xA1A1A1A1);
        interuptDescriptorTableAddr += MAGIC.ptrSize;

        nextFreeAddr = interuptDescriptorTableAddr + Interrupts.idtEntryCount*MAGIC.ptrSize*2 ;

        // write marker into mem
        MAGIC.wMem32(nextFreeAddr, 0xB2B2B2B2);
        nextFreeAddr += MAGIC.ptrSize;

        if (image.size != MAGIC.rMem32(MAGIC.imageBase + 4) || image.start != MAGIC.imageBase) {
            LowlevelLogging.debug("Something is wrong with my image info struct", LowlevelLogging.ERROR);
            while (true) {
            }
        }
    }

    // Todo cleanup prints later on
    public static Object newInstance(int scalarSize, int relocEntries, SClassDesc type) {
        // allign scalar size to 4 byte
        scalarSize = (scalarSize + 0x3) & ~0x3;

        int line = 15;
        LowlevelOutput.printStr("Last Obj", 0, line, Color.GREEN);
        LowlevelOutput.printStr("scalarSize", 0, ++line, Color.GREEN);
        LowlevelOutput.printInt(scalarSize, 10, 10, 15, line, Color.GREEN);

        LowlevelOutput.printStr("relocEntries", 0, ++line, Color.GREEN);
        LowlevelOutput.printInt(relocEntries, 10, 10, 15, line, Color.GREEN);

        // calculate memory requirements
        int objSize = scalarSize + relocEntries * POINTER_SIZE;

        LowlevelOutput.printStr("objSize", 0, ++line, Color.GREEN);
        LowlevelOutput.printInt(objSize, 10, 10, 15, line, Color.GREEN);


        // clear allocated memory
        for (int i = 0; i < objSize/4; i++) {
            MAGIC.wMem32(nextFreeAddr + i*4, 0x00000000);
        }

        // calculate object address inside allocated memory
        int objAddr = nextFreeAddr + relocEntries * POINTER_SIZE;


        LowlevelOutput.printStr("objAddr", 0, ++line, Color.GREEN);
        LowlevelOutput.printInt(objAddr, 10, 10, 15, line, Color.GREEN);

        LowlevelOutput.printStr("nextFreeAddr", 0, ++line, Color.GREEN);
        LowlevelOutput.printInt(nextFreeAddr, 10, 10, 15, line, Color.GREEN);

        Object newObject = MAGIC.cast2Obj(objAddr);

        // fill kernel fields of object
        MAGIC.assign(newObject._r_type, type);
        MAGIC.assign(newObject._r_relocEntries, relocEntries);
        MAGIC.assign(newObject._r_scalarSize, scalarSize);


        if (lastObjAddr == 0) {
            // first object ever, save it for having a startpoint for _r_next iteration GC (later on)
            firstObjAddr = objAddr;

        } else {
            // set r_next on last object
            // TODO cast nicht nötig gleich objekt merken
            Object lastObject = MAGIC.cast2Obj(lastObjAddr);
            MAGIC.assign(lastObject._r_next, newObject);

            // print status
      /*LowlevelOutputTest.printStr("Setting last r_next", 2, 23, Color.CYAN);
      LowlevelOutputTest.printInt(MAGIC.addr(lastObject._r_next), 10, 10, 2, 24, Color.CYAN);
      LowlevelOutputTest.printStr(" to ", 12, 24, Color.CYAN);
      LowlevelOutputTest.printInt(MAGIC.rMem32(MAGIC.addr(lastObject._r_next)), 10, 10, 16, 24, Color.CYAN);
      Kernel.wait(1);*/
        }

        // allocate requested memory
        // this is automatically alligned because scalar size was aligned at the beginning of this method
        // and all the reloc entrys are always 4 bytes on ia32
        nextFreeAddr = nextFreeAddr + objSize;

        lastObjAddr = objAddr;

        return newObject;
    }


    public static SArray newArray(int length, int arrDim, int entrySize, int stdType,
                                  SClassDesc unitType) { //unitType is not for sure of type SClassDesc
        int scS, rlE;
        SArray me;

        if (stdType == 0 && unitType._r_type != MAGIC.clssDesc("SClassDesc"))
            MAGIC.inline(0xCC); //check type of unitType, we don't support interface arrays
        scS = MAGIC.getInstScalarSize("SArray");
        rlE = MAGIC.getInstRelocEntries("SArray");
        if (arrDim > 1 || entrySize < 0) rlE += length;
        else scS += length * entrySize;
        me = (SArray) newInstance(scS, rlE, (SClassDesc) MAGIC.clssDesc("SArray")); // I ADDED THE CHAST HERE !!!!!!!
        MAGIC.assign(me.length, length);
        MAGIC.assign(me._r_dim, arrDim);
        MAGIC.assign(me._r_stdType, stdType);
        MAGIC.assign(me._r_unitType, unitType);
        return me;
    }

    public static void newMultArray(SArray[] parent, int curLevel, int destLevel,
                                    int length, int arrDim, int entrySize, int stdType, SClassDesc clssType) {
        int i;

        if (curLevel + 1 < destLevel) { //step down one level
            curLevel++;
            for (i = 0; i < parent.length; i++) {
                newMultArray((SArray[]) ((Object) parent[i]), curLevel, destLevel,
                        length, arrDim, entrySize, stdType, clssType);
            }
        } else { //create the new entries
            destLevel = arrDim - curLevel;
            for (i = 0; i < parent.length; i++) {
                parent[i] = newArray(length, destLevel, entrySize, stdType, clssType);
            }
        }
    }

    public static boolean isInstance(Object o, SClassDesc dest, boolean asCast) {
        SClassDesc check;

        if (o == null) {
            if (asCast) return true; //null matches all
            return false; //null is not an instance
        }
        check = o._r_type;
        while (check != null) {
            if (check == dest) return true;
            check = check.parent;
        }
        if (asCast) MAGIC.inline(0xCC);
        return false;
    }

    public static SIntfMap isImplementation(Object o, SIntfDesc dest, boolean asCast) {
        SIntfMap check;

        if (o == null) return null;
        check = o._r_type.implementations;
        while (check != null) {
            if (check.owner == dest) return check;
            check = check.next;
        }
        if (asCast) MAGIC.inline(0xCC);
        return null;
    }

    public static boolean isArray(SArray o, int stdType, SClassDesc clssType, int arrDim, boolean asCast) {
        SClassDesc clss;

        //in fact o is of type "Object", _r_type has to be checked below - but this check is faster than "instanceof" and conversion
        if (o == null) {
            if (asCast) return true; //null matches all
            return false; //null is not an instance
        }
        if (o._r_type != MAGIC.clssDesc("SArray")) { //will never match independently of arrDim
            if (asCast) MAGIC.inline(0xCC);
            return false;
        }
        if (clssType == MAGIC.clssDesc("SArray")) { //special test for arrays
            if (o._r_unitType == MAGIC.clssDesc("SArray"))
                arrDim--; //an array of SArrays, make next test to ">=" instead of ">"
            if (o._r_dim > arrDim) return true; //at least one level has to be left to have an object of type SArray
            if (asCast) MAGIC.inline(0xCC);
            return false;
        }
        //no specials, check arrDim and check for standard type
        if (o._r_stdType != stdType || o._r_dim < arrDim) { //check standard types and array dimension
            if (asCast) MAGIC.inline(0xCC);
            return false;
        }
        if (stdType != 0) {
            if (o._r_dim == arrDim) return true; //array of standard-type matching
            if (asCast) MAGIC.inline(0xCC);
            return false;
        }
        //array of objects, make deep-check for class type (PicOS does not support interface arrays)
        if (o._r_unitType._r_type != MAGIC.clssDesc("SClassDesc")) MAGIC.inline(0xCC);
        clss = o._r_unitType;
        while (clss != null) {
            if (clss == clssType) return true;
            clss = clss.parent;
        }
        if (asCast) MAGIC.inline(0xCC);
        return false;
    }

    public static void checkArrayStore(SArray dest, SArray newEntry) {
        if (dest._r_dim > 1) isArray(newEntry, dest._r_stdType, dest._r_unitType, dest._r_dim - 1, true);
        else if (dest._r_unitType == null) MAGIC.inline(0xCC);
        else isInstance(newEntry, dest._r_unitType, true);
    }
}
