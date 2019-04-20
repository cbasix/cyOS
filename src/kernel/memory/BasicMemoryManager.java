package kernel.memory;

import io.LowlevelLogging;
import kernel.Kernel;
import kernel.interrupts.core.DescriptorTable;
import kernel.interrupts.core.Interrupts;
import kernel.interrupts.core.JumpTable;
import rte.SClassDesc;

public class BasicMemoryManager extends MemoryManager{
    private static int nextFreeAddr;
    private static int firstFreeAddr;

    public static int interruptDescriptorTableAddr, interruptJumpTableAddr;

    public static class ImageInfo extends STRUCT {
        public int start, size, classDescStart, codebyteAddr, firstObjInImageAddr, ramInitAddr;
    }

    public static int getNextFreeAddr() {
        return nextFreeAddr;
    }

    public static int getFirstFreeAddr(){
        return firstFreeAddr;
    }

    public static MemoryManager initialize() {
        ImageInfo image = (ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        // make room for interrupt table after image
        // allign to 4 byte, (last two address bits zero)
        interruptDescriptorTableAddr =  (image.start + image.size + 0x3) & ~0x3;

        // write marker into mem
        MAGIC.wMem32(interruptDescriptorTableAddr, 0xA1A1A1A1);
        interruptDescriptorTableAddr += MAGIC.ptrSize;

        int idtSize = DescriptorTable.entryCount * DescriptorTable.entrySize;
        int ijtSize = DescriptorTable.entryCount * JumpTable.entrySize + JumpTable.scalarSize;

        interruptJumpTableAddr = interruptDescriptorTableAddr + idtSize;

        MAGIC.wMem32(interruptJumpTableAddr, 0xA6A6A6A6);
        interruptJumpTableAddr += MAGIC.ptrSize;

        firstFreeAddr = interruptJumpTableAddr + ijtSize;

        // write marker into mem
        MAGIC.wMem32(firstFreeAddr, 0xB2B2B2B2);
        firstFreeAddr += MAGIC.ptrSize;


        nextFreeAddr = firstFreeAddr;

        // concept shamelessly stolen from picos
        // get an instance of BasicMemoryManager allocated
        return (MemoryManager) staticAllocate(
                MAGIC.getInstScalarSize("BasicMemoryManager"),
                MAGIC.getInstRelocEntries("BasicMemoryManager"),
                (SClassDesc) MAGIC.clssDesc("BasicMemoryManager"));
    }

    @Override
    public Object allocate(int scalarSize, int relocEntries, SClassDesc type) {
        return staticAllocate(scalarSize, relocEntries, type);
    }

    @Override
    public void deallocate(Object o) {
        // basic manager can not deallocate
    }

    @Override
    public void gc() {
        // basic manager has no garbage collection
    }

    public static Object staticAllocate(int scalarSize, int relocEntries, SClassDesc type) {
        int objSize = getAllignedSize(scalarSize, relocEntries);
        // allocate requested memory
        // this is automatically alligned because scalar size was aligned at the beginning of this method
        // and all the reloc entrys are always 4 bytes on ia32
        nextFreeAddr = nextFreeAddr + objSize;

        return createObject(scalarSize, relocEntries, type, nextFreeAddr-objSize, objSize);
    }
}
