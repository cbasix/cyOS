package kernel;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import rte.DynamicRuntime;
import rte.SClassDesc;

public class Interrupts {

    public static final int idtEntryCount = 0x30; // 48

    public static int handleInterruptAddr, handleInterruptWithParamAddr;
    public static class InterruptDescriptorTable extends STRUCT {
        @SJC(count = idtEntryCount)
        public InterruptDescriptorTableEntry[] entries;
    }

    public static class InterruptDescriptorTableEntry extends STRUCT {
        public short offsetLowBytes;
        public short segmentSelector;
        public byte zero;
        public byte bitStuff;
        public short offsetHighBytes;
    }

    public static class InterruptJumpTableEntry extends STRUCT {
        /*
            DISABLE_INT
            00000000: 50                               PUSH EAX              ; sichere register
            00000001: 53                               PUSH EBX              ; schreibe den festen wert B in adresse A
            00000002: b8a3a2a1a0                       MOV EAX, 0xa0a1a2a3   ; B ist die InterruptNummer, A zeigt auf
            00000007: bbb3b2b1b0                       MOV EBX, 0xb0b1b2b3   ; das "Last interrupt" Feld der Interrupt Jump Table
            0000000c: 8918                             MOV [EAX], EBX
            0000000e: 5b                               POP EBX               ; restore register
            0000000f: 58                               POP EAX
            00000010: ff25c3c2c1c0                     JMP DWORD [0xc0c1c2c3] ; sprige zu Adresse C einem der beiden globalen int handler
         */

        private byte DISABLE_INT, PUSH_EAX,
                PUSH_EBX,
                MOVE_EAX_writeToAddr;
        public int writeToAddr;
        private byte MOVE_EBX_interruptNo;
        public int interruptNo;
        private byte MOVE_IND_EAX_EBX_commandpart1, MOVE_IND_EAX_EBX_commandpart2,
                POP_EBX,
                POP_EAX,
                JMP_addr_commandpart1, JMP_addr_commandpart2;
        public int addrOfGlobalHandlerAddr;
        private byte UNUSED_FILLUP_TO_24_BYTES;
    }
    public static final int interruptJumpTableEntrySize = 24;


    public static class InterruptJumpTable extends STRUCT {
        public int globalHandlerWithoutParamAddr;
        public int globalHandlerWithParamAddr;
        public int receivedInterruptNo;

        @SJC(count = idtEntryCount)
        public InterruptJumpTableEntry entries[];
    }
    public static final int interruptJumpTableScalarSize = 12;


    public static void init(){
        disable();

        int idtBase = DynamicRuntime.interruptDescriptorTableAddr;
        int ijtBase = DynamicRuntime.interruptJumpTableAddr;
        getDefaultHandlerAddresses();

        LowlevelOutput.printHex(handleInterruptAddr,12, 0, 1, Color.RED);
        LowlevelOutput.printHex(handleInterruptWithParamAddr, 12, 0, 2, Color.RED);
        LowlevelOutput.printHex(DynamicRuntime.interruptDescriptorTableAddr, 12, 0, 3, Color.RED);
        LowlevelOutput.printHex(DynamicRuntime.interruptJumpTableAddr, 12, 0, 3, Color.GREEN);
        // method offsets passen (output addressen mit jar -getMethod kontrolliert

        initPic();
        // sollte passen war ja so vorgegeben
        //MAGIC.inline(0xCC);

        writeIdt(idtBase, ijtBase);
        writeInterruptJumpTable(ijtBase);
        loadIDT(idtBase, 8*idtEntryCount);
        // idt enthällt richtigen wert, tabelle enhällt erwartete werte. Denke ich falsch / erwarte ich falsche?
        // (qemu) info registers -> IDT=     001039bc 000000c0
        // (qemu) x /100 0x1039b8
        LowlevelLogging.printHexdump(idtBase+MAGIC.ptrSize*2*40);

        //MAGIC.inline(0xCC);
        enable(); // 102bf5
        //disable();

        while(true){}

    }

    @SJC.Interrupt
    public static void defaultHandler(){
        enable();
        LowlevelOutput.printStr("WE GOT AN INTERRUPT h", 0, 25, Color.RED);
        //while (true){}
        // acknowledge

        MAGIC.wIOs8(SLAVE, (byte)0x20);
        MAGIC.wIOs8(MASTER, (byte)0x20);
    }

    @SJC.Interrupt
    public static void defaultHandlerWithParam(int param){
        enable();
        LowlevelOutput.printStr("WE GOT AN INTERRUPT hP", 0, 25, Color.RED);

        //MAGIC.inline(0xCC);
        //while(true){}
        MAGIC.wIOs8(SLAVE, (byte)0x20);
        MAGIC.wIOs8(MASTER, (byte)0x20);
    }

    @SJC.Inline
    public static void enable(){
        // set interrupt flag
        MAGIC.inline(0xFB);
    }

    @SJC.Inline
    public static void disable(){
        // clear interrupt flag
        MAGIC.inline(0xFA);
    }

    protected final static int MASTER = 0x20, SLAVE = 0xA0;
    public static void initPic() {
        programmChip(MASTER, 0x20, 0x04); //init offset and slave config of master
        programmChip(SLAVE, 0x28, 0x02); //init offset and slave config of slave
    }

    private static void programmChip(int port, int offset, int icw3) {
        MAGIC.wIOs8(port++, (byte)0x11); // ICW1
        MAGIC.wIOs8(port, (byte)offset); // ICW2
        MAGIC.wIOs8(port, (byte)icw3); // ICW3
        MAGIC.wIOs8(port, (byte)0x01); // ICW4
    }

    private static void loadIDT(int baseAddress, int tableLimit){
        //Maschinen-Befehl LIDT („load IDT“) zu verwenden, dessen Codierung mit lokaler Variable
        //tmp (i.e. auf dem Stack) folgendermaßen vorzunehmen ist:
        long tmp=(((long)baseAddress)<<16)|(long)tableLimit;
        MAGIC.inline(0x0F, 0x01, 0x5D); MAGIC.inlineOffset(1, tmp); // lidt [ebp-0x08/tmp]
    }

    private static void getDefaultHandlerAddresses(){
        SClassDesc interruptClassDesc = (SClassDesc) MAGIC.clssDesc("Interrupts");

        int mthdOff = MAGIC.mthdOff("Interrupts", "defaultHandler");
        handleInterruptAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOff )+MAGIC.getCodeOff();

        int mthdOffParam = MAGIC.mthdOff("Interrupts", "defaultHandlerWithParam");
        handleInterruptWithParamAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOffParam )+MAGIC.getCodeOff();
    }

    private static void writeInterruptJumpTable(int ijtBase){
        InterruptJumpTable ijt = (InterruptJumpTable) MAGIC.cast2Struct(ijtBase);
        ijt.globalHandlerWithoutParamAddr = handleInterruptAddr;
        ijt.globalHandlerWithParamAddr = handleInterruptWithParamAddr;


        for (int i = 0; i < idtEntryCount; i++){
            // todo verify that the jump is absolute
            int target;
            if (i >= 0x08 && i <=0x0E) {
                target = ijtBase; // = globalHandlerWithoutParamAddr field
            } else {
                target = ijtBase + 4; // = globalHandlerWithParamAddr field
            }

            ijt.entries[i].interruptNo = i;
            ijt.entries[i].addrOfGlobalHandlerAddr = target;
            ijt.entries[i].writeToAddr = ijtBase + 8; // = received interruptNo field

            // machine codes
            ijt.entries[i].DISABLE_INT = (byte)0xFA;
            ijt.entries[i].PUSH_EAX = (byte)0x50;
            ijt.entries[i].PUSH_EBX = (byte)0x53;
            ijt.entries[i].MOVE_EAX_writeToAddr = (byte)0xB8;
            ijt.entries[i].MOVE_EBX_interruptNo = (byte)0xBB;
            ijt.entries[i].MOVE_IND_EAX_EBX_commandpart1 = (byte)0x89;
            ijt.entries[i].MOVE_IND_EAX_EBX_commandpart2 = (byte)0x18;
            ijt.entries[i].POP_EBX = (byte)0x5B;
            ijt.entries[i].POP_EAX = (byte)0x58;
            ijt.entries[i].JMP_addr_commandpart1 = (byte)0xff;
            ijt.entries[i].JMP_addr_commandpart2 = (byte)0x25;

        }
    }


    private static void writeIdt(int idtBase, int ijtBase){
        InterruptDescriptorTable idt = (InterruptDescriptorTable) MAGIC.cast2Struct(idtBase);
        int target;
        for (int i = 0; i < idtEntryCount; i++){

            target = interruptJumpTableEntrySize*i + ijtBase + interruptJumpTableScalarSize; // begin of ijt entrys array

            idt.entries[i].segmentSelector = (short)(1 << 3);
            idt.entries[i].offsetLowBytes = (short)(target & 0xFFFF);
            idt.entries[i].offsetHighBytes = (short)(((long) target >> 16) & 0xFFFF);
            idt.entries[i].bitStuff = (byte)0x8E; // 0b1000 1110; // siehe 2.1.4
            idt.entries[i].zero = (byte)0x00; // siehe 2.1.4

            /*MAGIC.wMem32(idtBase+i*8, (handleInterruptAddr&0x0000FFFF)|(1<<19));
            MAGIC.wMem32(idtBase+i*8+4, (handleInterruptAddr&0xFFFF0000)|0x00008E00);*/
        }


    }

}
