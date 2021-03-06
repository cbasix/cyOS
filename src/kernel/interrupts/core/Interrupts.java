package kernel.interrupts.core;

import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.memory.BasicMemoryManager;
import rte.SClassDesc;

public class Interrupts {
    public static final int DIVIDE_ERROR = 0x00;
    public static final int DEBUG_EXCEPTION = 0x01;
    public static final int NMI = 0x02;
    public static final int BREAKPOINT = 0x03;
    public static final int INTO_OVERFLOW = 0x04;
    public static final int INDEX_OUT_OF_RANGE = 0x05;
    public static final int INVALID_OPCODE = 0x06;
    public static final int DOUBLE_FAULT = 0x08;
    public static final int GENERAL_PROTECTION_ERROR = 0x0D;
    public static final int PAGE_FAULT = 0x0E;
    public static final int IRQ0 = 0x20;
    public static final int TIMER = 0x20;
    public static final int IRQ1 = 0x21;
    public static final int KEYBOARD = 0x21;
    public static final int IRQ2 = 0x22;
    public static final int IRQ3 = 0x23;
    public static final int IRQ4 = 0x24;
    public static final int IRQ5 = 0x25;
    public static final int IRQ6 = 0x26;
    public static final int IRQ7 = 0x27;
    public static final int IRQ8 = 0x28;
    public static final int IRQ9 = 0x29;
    public static final int IRQ10 = 0x2A;
    public static final int IRQ11 = 0x2B;
    public static final int IRQ12 = 0x2C;
    public static final int IRQ13 = 0x2D;
    public static final int IRQ14 = 0x2E;
    public static final int IRQ15 = 0x2F;

    public static int handleInterruptAddr, handleInterruptWithParamAddr;
    public static InterruptHub interruptHub = null;

    public static InterruptHub init(){
        disable();

        interruptHub = new InterruptHub();

        int idtBase = BasicMemoryManager.interruptDescriptorTableAddr;
        int ijtBase = BasicMemoryManager.interruptJumpTableAddr;
        initDefaultHandlerAddresses();

        initPic();

        JumpTable.write(ijtBase);
        DescriptorTable.write(idtBase, ijtBase);

        loadIDT(idtBase, DescriptorTable.entrySize*DescriptorTable.entryCount-1);
        enable();

        return interruptHub;
    }

    @SJC.Interrupt
    public static void defaultHandler(){
        int interruptEbp=0;
        MAGIC.inline(0x89, 0x6D); MAGIC.inlineOffset(1, interruptEbp); //mov [ebp+xx],ebp
        handleInterrupt(0, interruptEbp);
    }

    @SJC.Interrupt
    public static void defaultHandlerWithParam(int param){
        int interruptEbp=0;
        MAGIC.inline(0x89, 0x6D); MAGIC.inlineOffset(1, interruptEbp); //mov [ebp+xx],ebp
        handleInterrupt(param, interruptEbp);
    }

    //@SJC.Inline
    public static void handleInterrupt(int param, int interruptEbp){
        // interrupts are disabled per default during interrupt handling
        int interruptNo = MAGIC.rMem32(BasicMemoryManager.interruptJumpTableAddr);

        //Interrupts.enable(); //  disabled reentrant interrupts ... for now..

        // if is an exception
        if (interruptNo <= Interrupts.PAGE_FAULT){
            Bluescreen.handleInterrupt(interruptNo, param, interruptEbp);
        } else {
            interruptHub.forwardInterrupt(interruptNo, param);
        }

        ack(interruptNo);

    }


    private static int state = 0;

    @SJC.Inline
    public static void enable(){
        state++;
        if (state >= 1) {
            // set interrupt flag
            MAGIC.inline(0xFB);
            state = 1;
        }
    }

    @SJC.Inline
    public static void forceEnable(){
        MAGIC.inline(0xFB);
        state = 1;
    }

    @SJC.Inline
    public static void disable(){
        state--;
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
    public static void ack(int interruptNo){
        // if interrupt from slave PIC
        if (IRQ8 <= interruptNo && interruptNo <= IRQ15){
            MAGIC.wIOs8(Interrupts.SLAVE, (byte)0x20);
        }
        // if interrupt from any PIC -> ack to master too
        if (IRQ0 <= interruptNo && interruptNo <= IRQ15){
            MAGIC.wIOs8(Interrupts.MASTER, (byte)0x20);
        }
    }

    private static void loadIDT(int baseAddress, int tableLimit){
        //Maschinen-Befehl LIDT („load IDT“) zu verwenden, dessen Codierung mit lokaler Variable
        //tmp (i.e. auf dem Stack) folgendermaßen vorzunehmen ist:
        long tmp=(((long)baseAddress)<<16)|(long)tableLimit;
        MAGIC.inline(0x0F, 0x01, 0x5D); MAGIC.inlineOffset(1, tmp); // lidt [ebp-0x08/tmp]
    }

    @SJC.Inline
    public static void loadProtectedModeIDT(){
        loadIDT(BasicMemoryManager.interruptDescriptorTableAddr, DescriptorTable.entrySize*DescriptorTable.entryCount-1);
    }

    @SJC.Inline
    public static void loadRealModeIDT(){
        loadIDT(0, 1023);
    }

    private static void initDefaultHandlerAddresses(){
        SClassDesc interruptClassDesc = (SClassDesc) MAGIC.clssDesc("Interrupts");
        int mthdOff = MAGIC.mthdOff("Interrupts", "defaultHandler");
        handleInterruptAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOff )+MAGIC.getCodeOff();

        int mthdOffParam = MAGIC.mthdOff("Interrupts", "defaultHandlerWithParam");
        handleInterruptWithParamAddr = MAGIC.rMem32(MAGIC.cast2Ref(interruptClassDesc)+mthdOffParam )+MAGIC.getCodeOff();

        /*SClassDesc bluescreenClassDesc = (SClassDesc) MAGIC.clssDesc("Bluescreen");
        int mthdOffPageFault = MAGIC.mthdOff("Bluescreen", "handlePageFault");
        handlePageFaultAddr = MAGIC.rMem32(MAGIC.cast2Ref(bluescreenClassDesc)+mthdOffPageFault )+MAGIC.getCodeOff();*/
    }
}
