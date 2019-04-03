package kernel;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import rte.DynamicRuntime;
import rte.SClassDesc;

public class Interrupts {

    public static final int idtEntryCount = 0x30; // 48

    public static int handeInterruptAddr;
    public static int handleInterruptWithParamAddr;

    public static class Idt extends STRUCT {
        @SJC(offset = 0, count = idtEntryCount)
        public IdtEntry[] entries;
    }

    public static class IdtEntry extends STRUCT {
        public short offsetLowBytes;
        public short segmentSelector;
        public byte zero;
        public byte bitStuff;
        public short offsetHighByte;
    }

    public static void init(){
        disable();

        int idtBase = DynamicRuntime.interuptDescriptorTableAddr;
        getCodeOffsets();

        LowlevelOutput.printHex(handeInterruptAddr,12, 0, 1, Color.RED);
        LowlevelOutput.printHex(handleInterruptWithParamAddr, 12, 0, 2, Color.RED);
        LowlevelOutput.printHex(DynamicRuntime.interuptDescriptorTableAddr, 12, 0, 3, Color.RED);
        // method offsets passen (output addressen mit jar -getMethod kontrolliert

        initPic();
        // sollte passen war ja so vorgegeben

        writeIdt(idtBase);
        loadIDT(idtBase, 4*idtEntryCount);
        // idt enthällt richtigen wert, tabelle enhällt erwartete werte. Denke ich falsch / erwarte ich falsche?
        // (qemu) info registers -> IDT=     001039bc 000000c0
        // (qemu) x /100 0x1039b8
        //001039b8: 0xa1a1a1a1 0x00012c10 0x00108e00 0x00012c10
        //001039c8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //001039d8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //001039e8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //001039f8: 0x00108e00 0x00012c38 0x00108e00 0x00012c38
        //00103a08: 0x00108e00 0x00012c38 0x00108e00 0x00012c38
        //00103a18: 0x00108e00 0x00012c38 0x00108e00 0x00012c38
        //00103a28: 0x00108e00 0x00012c38 0x00108e00 0x00012c10
        //00103a38: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103a48: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103a58: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103a68: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103a78: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103a88: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103a98: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103aa8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103ab8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103ac8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103ad8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103ae8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103af8: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103b08: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103b18: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103b28: 0x00108e00 0x00012c10 0x00108e00 0x00012c10
        //00103b38: 0x00108e00 0xb2b2b2b2 0x00000000 0x00000000

        LowlevelLogging.printHexdump(idtBase+MAGIC.ptrSize*2*40);

        enable(); // 102bf5
        //disable();
        //MAGIC.inline(0xCC);
        while(true){}

    }

    @SJC.Interrupt
    public static void handler(){
        disable();
        LowlevelLogging.debug("WE GOT AN INTERRUPT", LowlevelLogging.ERROR);
        while (true){}
        // acknowledge
        //MAGIC.wIOs8(MASTER, (byte)0x20);

    }

    @SJC.Interrupt
    public static void handlerParam(int param){
        disable();
        LowlevelLogging.debug("WE GOT AN INTERRUPT", LowlevelLogging.ERROR);
        while (true){}
        // acknowledge
        //MAGIC.wIOs8(MASTER, (byte)0x20);
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

    private final static int MASTER = 0x20, SLAVE = 0xA0;
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

    private static void getCodeOffsets(){
       /* SClassDesc interruptClassDesc = (SClassDesc) MAGIC.clssDesc("Interrupts");
        int mthdOff = MAGIC.mthdOff("Interrupts", "handler");
        handeInterruptAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOff )+MAGIC.getCodeOff();

        int mthdOffParam = MAGIC.mthdOff("Interrupts", "handlerParam");
        handleInterruptWithParamAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOffParam )+MAGIC.getCodeOff();
*/
        int cls=(int)MAGIC.cast2Ref(MAGIC.clssDesc("Interrupts"));
        //MAGIC.wMem32(KernelConst.KM_INTDESC, cls);
        handeInterruptAddr=MAGIC.rMem32(cls+MAGIC.mthdOff("Interrupts", "handler"))
                +MAGIC.getCodeOff();
        handleInterruptWithParamAddr=MAGIC.rMem32(cls+MAGIC.mthdOff("Interrupts", "handlerParam"))
                +MAGIC.getCodeOff();
    }

    private static void writeIdt(int idtBase){
        Idt idt = (Idt) MAGIC.cast2Struct(idtBase);
        for (int i = 0; i < idtEntryCount; i++){


            idt.entries[i].segmentSelector = (short)1 << 3;
            if (i >= 0x08 && i <=0x0E) {
                idt.entries[i].offsetLowBytes = (short)(handleInterruptWithParamAddr & 0xFFFF);
                idt.entries[i].offsetHighByte = (short)(((long) handleInterruptWithParamAddr >> 16) & 0xFFFF);

            } else {
                idt.entries[i].offsetLowBytes = (short)(handeInterruptAddr & 0xFFFF);
                idt.entries[i].offsetHighByte = (short)(((long) handeInterruptAddr >> 16 ) & 0xFFFF);
                //LowlevelOutput.printHex( idt.entries[i].offsetLowBytes, 4, 6, 4, Color.GREEN);
                //LowlevelOutput.printHex( idt.entries[i].offsetHighByte, 4, 0, 4, Color.GREEN);
            }
            idt.entries[i].bitStuff = (byte)0x8E; // 0b1000 1110; // siehe 2.1.4
            idt.entries[i].zero = (byte)0x00; // siehe 2.1.4

            /*MAGIC.wMem32(idtBase+i*8, (handeInterruptAddr&0x0000FFFF)|(1<<19));
            MAGIC.wMem32(idtBase+i*8+4, (handeInterruptAddr&0xFFFF0000)|0x00008E00);*/
        }


    }

}
