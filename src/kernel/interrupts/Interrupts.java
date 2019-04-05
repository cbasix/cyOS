package kernel.interrupts;

import io.Color;
import io.LowlevelOutput;
import rte.DynamicRuntime;
import rte.SClassDesc;

public class Interrupts {
    public static int handleInterruptAddr, handleInterruptWithParamAddr, handleDoubleFaultAddr;

    public static void init(){
        disable();

        int idtBase = DynamicRuntime.interruptDescriptorTableAddr;
        int ijtBase = DynamicRuntime.interruptJumpTableAddr;
        initDefaultHandlerAddresses();

        initPic();

        JumpTable.write(ijtBase);
        DescriptorTable.write(idtBase, ijtBase);

        loadIDT(idtBase, DescriptorTable.entrySize*DescriptorTable.entryCount);
    }

    @SJC.Interrupt
    public static void defaultHandler(){
        handleInterrupt(0);
    }

    @SJC.Interrupt
    public static void defaultHandlerWithParam(int param){
        handleInterrupt(param);
    }

    @SJC.Interrupt
    public static void doubleFaultHandler(int param){
       LowlevelOutput.printStr("DOUBLE FAULT", 20, 13, Color.RED);
       while (true);
    }

    @SJC.Inline
    public static void handleInterrupt(int param){
        int interruptNo = MAGIC.rMem32(DynamicRuntime.interruptJumpTableAddr);
        // TODO let assebmler disable interrupts and enable again here after reading the no (possible? isr preambel?)

        //if (interruptNo!= 0x20) {
        LowlevelOutput.printHex(interruptNo, 2, 78, 15, Color.RED);
        //}
        //InterruptHub.forwardInterrupt(interruptNo, param);

        ack(interruptNo);
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
    private static void initPic() {
        programmChip(MASTER, 0x20, 0x04); //init offset and slave config of master
        programmChip(SLAVE, 0x28, 0x02); //init offset and slave config of slave
    }

    @SJC.Inline
    private static void programmChip(int port, int offset, int icw3) {
        MAGIC.wIOs8(port++, (byte)0x11); // ICW1
        MAGIC.wIOs8(port, (byte)offset); // ICW2
        MAGIC.wIOs8(port, (byte)icw3); // ICW3
        MAGIC.wIOs8(port, (byte)0x01); // ICW4
    }

    @SJC.Inline
    private static void ack(int interruptNo){
        // if interrupt from slave PIC
        if (0x20 <= interruptNo && interruptNo < 0x28){
            MAGIC.wIOs8(Interrupts.SLAVE, (byte)0x20);
        }
        // if interrupt from any PIC -> ack to master too
        if (0x20 <= interruptNo && interruptNo < 0x30){
            MAGIC.wIOs8(Interrupts.MASTER, (byte)0x20);
        }
    }

    private static void loadIDT(int baseAddress, int tableLimit){
        //Maschinen-Befehl LIDT („load IDT“) zu verwenden, dessen Codierung mit lokaler Variable
        //tmp (i.e. auf dem Stack) folgendermaßen vorzunehmen ist:
        long tmp=(((long)baseAddress)<<16)|(long)tableLimit;
        MAGIC.inline(0x0F, 0x01, 0x5D); MAGIC.inlineOffset(1, tmp); // lidt [ebp-0x08/tmp]
    }

    private static void initDefaultHandlerAddresses(){
        SClassDesc interruptClassDesc = (SClassDesc) MAGIC.clssDesc("Interrupts");

        int mthdOff = MAGIC.mthdOff("Interrupts", "defaultHandler");
        handleInterruptAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOff )+MAGIC.getCodeOff();

        int mthdOffParam = MAGIC.mthdOff("Interrupts", "defaultHandlerWithParam");
        handleInterruptWithParamAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOffParam )+MAGIC.getCodeOff();

        int mthdDoubeFault = MAGIC.mthdOff("Interrupts", "doubleFaultHandler");
        handleDoubleFaultAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdDoubeFault )+MAGIC.getCodeOff();
    }
}
