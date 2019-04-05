package kernel;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import rte.DynamicRuntime;
import rte.SClassDesc;

public class Interrupts {

    public static final int idtEntryCount = 0x30; // 48

    public static int handleInterruptAddr, handleInterruptWithParamAddr, handleDoubleFaultAddr;
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
            00000000: 53                               PUSH EBX
            00000001: bbb3b2b1b0                       MOV EBX, 0xb0b1b2b3
            00000006: 891da3a2a1a0                     MOV [0xa0a1a2a3], EBX
            0000000c: 5b                               POP EBX
            0000000d: eacf00ffff0800                   JMP FAR 0x8:0xffff00cf
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
    public static final int interruptJumpTableEntrySize = 24;


    public static class InterruptJumpTable extends STRUCT {
        public int receivedInterruptNo;

        @SJC(count = idtEntryCount)
        public InterruptJumpTableEntry entries[];
    }
    public static final int interruptJumpTableScalarSize = 8;


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

        writeInterruptJumpTable(ijtBase);
        writeIdt(idtBase, ijtBase);

        loadIDT(idtBase, 8*idtEntryCount);
        // idt enthällt richtigen wert, tabelle enhällt erwartete werte. Denke ich falsch / erwarte ich falsche?
        // (qemu) info registers -> IDT=     001039bc 000000c0
        // (qemu) x /100 0x1039b8
        LowlevelLogging.printHexdump(idtBase+MAGIC.ptrSize*2*40);

        //MAGIC.inline(0xCC);
        enable(); // 102bf5
        //disable();

        //while(true){}

    }

    public static int c = 0;
    @SJC.Interrupt
    public static void defaultHandler(){
        //enable();
        LowlevelOutput.printStr("WE GOT AN INTERRUPT h", 0, 0, Color.RED);
        LowlevelOutput.printInt(c, 10, 12, 25, 0, Color.RED);

        int interruptNo = MAGIC.rMem32(DynamicRuntime.interruptJumpTableAddr);
        LowlevelOutput.printInt(interruptNo, 10, 12, 25, 1, Color.GREEN);

        //while (true){}
        // acknowledge

        MAGIC.wIOs8(SLAVE, (byte)0x20);
        MAGIC.wIOs8(MASTER, (byte)0x20);
        c++;
    }

    @SJC.Interrupt
    public static void defaultHandlerWithParam(int param){
        int interruptNo = MAGIC.rMem32(DynamicRuntime.interruptJumpTableAddr);
        //enable();
        //LowlevelOutput.printStr("WE GOT AN INTERRUPT hP", 0, 0, Color.BLUE);
        LowlevelOutput.printInt(c, 10, 12, 25, 2, Color.RED);
        LowlevelOutput.printInt(interruptNo, 10, 12, 25, 1, Color.GREEN);

        //MAGIC.inline(0xCC);
        //while(true){}
        MAGIC.wIOs8(SLAVE, (byte)0x20);
        MAGIC.wIOs8(MASTER, (byte)0x20);

        c++;
    }

    @SJC.Interrupt
    public static void doubleFaultHandler(int param){
        int interruptNo = MAGIC.rMem32(DynamicRuntime.interruptJumpTableAddr);
        //enable();
        LowlevelOutput.printStr("DOUBLE FAULT", 0, 0, Color.BLUE);
        //LowlevelOutput.printInt(interruptNo, 10, 12, 25, 0, Color.BLUE);

        //MAGIC.inline(0xCC);
        //while(true){}
        MAGIC.wIOs8(SLAVE, (byte)0x20);
        MAGIC.wIOs8(MASTER, (byte)0x20);

        c++;
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

        int mthdOffDoubleFault = MAGIC.mthdOff("Interrupts", "doubleFaultHandler");
        handleDoubleFaultAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOffDoubleFault )+MAGIC.getCodeOff();

        int mthdOffIsr = MAGIC.mthdOff("Interrupts", "isr");
        int isr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOffIsr )+MAGIC.getCodeOff();
    }

    private static void writeInterruptJumpTable(int ijtBase){
        InterruptJumpTable ijt = (InterruptJumpTable) MAGIC.cast2Struct(ijtBase);
        ijt.receivedInterruptNo = 0xFFFFFFFF;

        for (int i = 0; i < idtEntryCount; i++){
            int target;
            if (i >= 0x08 && i <=0x0E) {
                target = handleInterruptWithParamAddr; // = globalHandlerWithoutParamAddr field
            } else {
                target = handleInterruptAddr; // = globalHandlerWithParamAddr field
            }
            // todo interrupts wieder auf die alten handler schalten und per inline asm direkt anspringen zum debuggen
            //LowlevelOutput.printHex(relativeJump,12, 0, 5, Color.GREEN);
            //LowlevelOutput.printInt(relativeJump, 10, 12, 0, 6, Color.RED);





            // machine codes
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

        //while (true);
    }


    private static void writeIdt(int idtBase, int ijtBase){
        InterruptDescriptorTable idt = (InterruptDescriptorTable) MAGIC.cast2Struct(idtBase);

        int target;
        for (int i = 0; i < idtEntryCount; i++){

            target = interruptJumpTableEntrySize*i + ijtBase + interruptJumpTableScalarSize; // begin of ijt entrys array
            // todo remove set old
            if (i == 2) {
                LowlevelOutput.printHex(target, 12, 0, i, Color.BLUE);
            }
            /* sprungliste
                104CC8
             */


            boolean old = false;
            if (old) {
                if (i > 0x08) {
                    if (i >= 0x08 && i <= 0x0E) {
                        target = handleInterruptWithParamAddr; //- ownAddr - 26; // = globalHandlerWithoutParamAddr field
                    } else {
                        target = handleInterruptAddr; //- ownAddr - 26; // = globalHandlerWithParamAddr field
                    }
                }
            }

            // double fault handler -> simplest possible
            if (i == 0x08){
                target = handleDoubleFaultAddr;
            }

            idt.entries[i].segmentSelector = (short)(1 << 3);
            idt.entries[i].offsetLowBytes = (short)(target & 0xFFFF);
            idt.entries[i].offsetHighBytes = (short)(((long) target >> 16) & 0xFFFF);
            idt.entries[i].bitStuff = (byte)0x8E; // 0b1000 1110; // siehe 2.1.4
            idt.entries[i].zero = (byte)0x00; // siehe 2.1.4

            /*MAGIC.wMem32(idtBase+i*8, (handleInterruptAddr&0x0000FFFF)|(1<<19));
            MAGIC.wMem32(idtBase+i*8+4, (handleInterruptAddr&0xFFFF0000)|0x00008E00);*/
        }
        //while (true);
    }

    @SJC.Interrupt
    private static void isr(){
        MAGIC.wMem32(0xA1A2A3A4, 0x01234567);
    }

}
