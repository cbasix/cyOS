package kernel.interrupts;

public class JumpTable {
    public static class InterruptJumpTableEntry extends STRUCT {
        /*
            Each table entry contains the following machine code instructions.
            For each entry the  values are replaced:
             - 0xb0b1b2b3: with the current entry no (=INT no)
             - 0xa0a1a2a3: the memory adress of the "receivedInterruptNo" field of the InterruptJumpTable
             - 0xffffffff: the global handlers address (with or without param)

            00000000: 53                               PUSH EBX                  ; save ebx
            00000001: bbb3b2b1b0                       MOV EBX, 0xb0b1b2b3
            00000006: 891da3a2a1a0                     MOV [0xa0a1a2a3], EBX     ; write current table entry no (=INT no) to mem
            0000000c: 5b                               POP EBX                   ; restore ebx
            0000000d: eaffffffff0800                   JMP FAR 0x8:0xffffff      ; jump to global handler
         */

        private byte PUSH_EBX;
        private byte MOVE_EBX_interruptNo;
        public int interruptNo;
        private byte MOVE_MEM_EBX_commandpart1,
                MOVE_MEM_EBX_commandpart2;
        public int writeToAddr;
        private byte POP_EBX,
                JMP_FAR;
        public int addrOfGlobalHandler;
        public short segmentOfGlobalHandler;
        private int UNUSED_FILLUP_TO_24_BYTES;
        private short UNUSED_FILLUP_TO_24_BYTES2;
    }
    public static final int entrySize = 24;



    public static class InterruptJumpTable extends STRUCT {
        public int receivedInterruptNo;

        @SJC(count = DescriptorTable.entryCount)
        public InterruptJumpTableEntry entries[];
    }
    public static final int scalarSize = 8;


    static void write(int ijtBase){
        InterruptJumpTable ijt = (InterruptJumpTable) MAGIC.cast2Struct(ijtBase);

        for (int i = 0; i < DescriptorTable.entryCount; i++){
            int target;
            if (i >= 0x08 && i <=0x0E) {
                target = Interrupts.handleInterruptWithParamAddr; // = globalHandlerWithoutParamAddr field
            } else {
                target = Interrupts.handleInterruptAddr; // = globalHandlerWithParamAddr field
            }

            // fill machine code with correct parameters for this entry
            ijt.entries[i].PUSH_EBX = (byte)0x53;

            ijt.entries[i].MOVE_EBX_interruptNo = (byte)0xBB;
            ijt.entries[i].interruptNo = i;

            ijt.entries[i].MOVE_MEM_EBX_commandpart1 = (byte)0x89;
            ijt.entries[i].MOVE_MEM_EBX_commandpart2 = (byte)0x1d;
            ijt.entries[i].writeToAddr = ijtBase; // = received interruptNo field

            ijt.entries[i].POP_EBX = (byte)0x5B;

            ijt.entries[i].JMP_FAR = (byte)0xEA;
            ijt.entries[i].addrOfGlobalHandler = target;
            ijt.entries[i].segmentOfGlobalHandler = (byte)0x08;

        }
    }
}
