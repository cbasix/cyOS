package kernel.interrupts;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import rte.DynamicRuntime;
import rte.SClassDesc;

public class Interrupts {


    public static int handleInterruptAddr, handleInterruptWithParamAddr;

    public static void init(){
        disable();

        int idtBase = DynamicRuntime.interruptDescriptorTableAddr;
        int ijtBase = DynamicRuntime.interruptJumpTableAddr;
        getDefaultHandlerAddresses();

        initPic();

        JumpTable.write(ijtBase);

        DescriptorTable.write(idtBase, ijtBase);

        loadIDT(idtBase, DescriptorTable.entrySize*DescriptorTable.entryCount);
        // idt enthällt richtigen wert, tabelle enhällt erwartete werte. Denke ich falsch / erwarte ich falsche?
        // (qemu) info registers -> IDT=     001039bc 000000c0
        // (qemu) x /100 0x1039b8
        LowlevelLogging.printHexdump(idtBase+MAGIC.ptrSize*2*40);

        //MAGIC.inline(0xCC);
        enable(); // 102bf5
        //disable();

        //while(true){}

    }

    @SJC.Inline
    private static void ack(int interruptNo){
        // if interrupt from slave PIC
        if (0x20 <= interruptNo && interruptNo < 0x28){
            // ack to slave
            MAGIC.wIOs8(Interrupts.SLAVE, (byte)0x20);

        }
        // if interrupt from any PIC -> ack to master too
        if (0x20 <= interruptNo && interruptNo < 0x30){
            // ack to master
            MAGIC.wIOs8(Interrupts.MASTER, (byte)0x20);
        }
    }

    @SJC.Interrupt
    public static void defaultHandler(){
        int interruptNo = MAGIC.rMem32(DynamicRuntime.interruptJumpTableAddr);
        //enable();

        if (interruptNo!= 30) {
            LowlevelOutput.printInt(interruptNo, 10, 12, 25, 1, Color.GREEN);
        }

        ack(interruptNo);
    }

    @SJC.Interrupt
    public static void defaultHandlerWithParam(int param){
        int interruptNo = MAGIC.rMem32(DynamicRuntime.interruptJumpTableAddr);

        if (interruptNo!= 30) {
            LowlevelOutput.printInt(interruptNo, 10, 12, 25, 1, Color.GREEN);
        }

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
}
