package kernel.interrupts;

public class DescriptorTable {
    public static final int entryCount = 0x30; // 48
    public static final int entrySize = 8;

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

            target = JumpTable.entrySize *i + ijtBase + JumpTable.scalarSize; // begin of ijt entrys array

            idt.entries[i].segmentSelector = (short)(1 << 3);
            idt.entries[i].offsetLowBytes = (short)(target & 0xFFFF);
            idt.entries[i].offsetHighBytes = (short)(((long) target >> 16) & 0xFFFF);
            idt.entries[i].bitStuff = (byte)0x8E; // 0b1000 1110; // siehe 2.1.4
            idt.entries[i].zero = (byte)0x00; // siehe 2.1.4

        }
    }
}
