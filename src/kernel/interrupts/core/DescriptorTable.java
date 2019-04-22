package kernel.interrupts.core;

public class DescriptorTable {
    public static final int entryCount = 0x30; // 48
    public static final int entrySize = 8;
    public static final boolean DIRECT_MODE = false;

    public static class InterruptDescriptorTable extends STRUCT {
        @SJC(count = entryCount)
        public InterruptDescriptorTableEntry[] entries;
    }

    public static class InterruptDescriptorTableEntry extends STRUCT {
        public short offsetLowBytes;
        public short segmentSelector;
        public byte zero;
        public byte bitStuff;
        public short offsetHighBytes;
    }

    static void write(int idtBase, int ijtBase){
        InterruptDescriptorTable idt = (InterruptDescriptorTable) MAGIC.cast2Struct(idtBase);

        int target;
        for (int i = 0; i < entryCount; i++){

            // Handle via jump table
            target = JumpTable.entrySize *i + ijtBase + JumpTable.scalarSize; // begin of ijt entrys array

            // for testing direct jump to global handler (looses interruptNo)
            if (DIRECT_MODE){
                if (i >= 0x08 && i <=0x0E) {
                    target = Interrupts.handleInterruptWithParamAddr; // = globalHandlerWithoutParamAddr field
                } else {
                    target = Interrupts.handleInterruptAddr; // = globalHandlerWithParamAddr field
                }
            }

            // handle page fault outside of the system
            /*if (i == Interrupts.PAGE_FAULT) {
                target = Interrupts.handlePageFaultAddr;
            }*/

            idt.entries[i].segmentSelector = (short)(1 << 3);
            idt.entries[i].offsetLowBytes = (short)(target & 0xFFFF);
            idt.entries[i].offsetHighBytes = (short)(((long) target >> 16) & 0xFFFF);
            idt.entries[i].bitStuff = (byte)0x8E; // 0b1000 1110; // siehe 2.1.4
            idt.entries[i].zero = (byte)0x00; // siehe 2.1.4

        }
    }
}
