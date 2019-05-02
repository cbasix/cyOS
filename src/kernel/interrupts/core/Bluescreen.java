package kernel.interrupts.core;

import io.Color;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;
import kernel.memory.Paging;
import rte.SClassDesc;
import rte.SMthdBlock;

public class Bluescreen {
    public static final int BLUESCREEN_COLOR = Color.BLUE << 4 | Color.GREY;  // red on black background

    public static class SavedRegs extends STRUCT {
        int EDI, ESI, EBP, ESP, EBX, EDX, ECX, EAX;
    }

    public static boolean handleInterrupt(int interruptNo, int param, int interruptEbp) {
        Interrupts.disable();
        int titleLine = 1;
        int xStart = 1;
        LowlevelOutput.clearScreen(BLUESCREEN_COLOR);

        switch(interruptNo) {

            case Interrupts.DIVIDE_ERROR:
                LowlevelOutput.printStr("ZERO DIVISION ERROR", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.DEBUG_EXCEPTION:
                LowlevelOutput.printStr("DEBUG EXCEPTION", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.NMI:
                LowlevelOutput.printStr("NON MASKABLE INTERRUPT (NMI)", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.BREAKPOINT:
                LowlevelOutput.printStr("BREAKPOINT", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.INTO_OVERFLOW:
                LowlevelOutput.printStr("INTO (Overflow)", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.INVALID_OPCODE:
                LowlevelOutput.printStr("INVALID OPCODE", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.GENERAL_PROTECTION_ERROR:
                LowlevelOutput.printStr("GENERAL PROTECTION ERROR", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            case Interrupts.PAGE_FAULT:
                LowlevelOutput.printStr("PAGE FAULT", xStart, titleLine, BLUESCREEN_COLOR);

                int cause = Paging.getCR2();
                LowlevelOutput.printStr("Address: 0x", xStart, titleLine+2, BLUESCREEN_COLOR);
                LowlevelOutput.printHex(cause, 10, xStart+11, titleLine+2, BLUESCREEN_COLOR);
                LowlevelOutput.printStr("Param:   0x", xStart, titleLine+3, BLUESCREEN_COLOR);
                LowlevelOutput.printHex(param, 10, xStart+11, titleLine+3, BLUESCREEN_COLOR);


                // ACHTUNG Bedeutung BIT 0 ist anders als in AB7 beschrieben! 0 -> Page not present;  1 -> Protection violation
                // Quelle: https://wiki.osdev.org/Page_fault supported by results.
                if ((param & 0x1) == 0) {
                    LowlevelOutput.printStr("Page not present", xStart, titleLine+5, BLUESCREEN_COLOR);
                } else {
                    LowlevelOutput.printStr("Protection violation", xStart, titleLine+5, BLUESCREEN_COLOR);
                }
                if ((param & 0x2) == 0) {
                    LowlevelOutput.printStr("Read access", xStart, titleLine+6, BLUESCREEN_COLOR);
                } else {
                    LowlevelOutput.printStr("Write access", xStart, titleLine+6, BLUESCREEN_COLOR);
                }
                break;

            case Interrupts.INDEX_OUT_OF_RANGE:
                LowlevelOutput.printStr("INDEX OUT OF RANGE", xStart, titleLine, BLUESCREEN_COLOR);
                break;

            default:
                LowlevelOutput.printStr("UNKNOWN EXCEPTION", xStart, titleLine, BLUESCREEN_COLOR);
                LowlevelOutput.printInt(interruptNo, 10, 10, xStart, titleLine+1, BLUESCREEN_COLOR);
                break;
        }

        printRegisters(interruptEbp);
        printCallStack(interruptEbp, interruptNo);

        // allow continue after breakpoint
        //while (interruptNo != Interrupts.BREAKPOINT){}

        // while(true) (with fooling the ide's dead code recognition)
        // and yes just sleeping with interrupts disabled would have the same effect
        int i = 0;
        while(i==0){Kernel.hlt();};

        Interrupts.enable();
        return true;
    }

    public static final int STACK_START = 0x9BFFC;

    private static final String[] pushARegNames = {
            "EDI", "ESI", "EBP", "ESP", "EBX", "EDX", "ECX", "EAX"
    };

    @SJC.Inline
    public static void printRegisters(int interruptEbp) {
        int ebp = interruptEbp;
        int line = 12;
        int xStart = 1;
        int pushABaseAddr = interruptEbp + MAGIC.ptrSize; // start of pusha saved register values

        //SavedRegs regs = (SavedRegs) MAGIC.cast2Struct(pushABaseAddr);

        LowlevelOutput.printStr("---Registers---", xStart, line++, BLUESCREEN_COLOR);

        int i = 0;
        for (String regname : pushARegNames) {
            LowlevelOutput.printStr(regname, xStart, line, BLUESCREEN_COLOR);
            LowlevelOutput.printHex(MAGIC.rMem32(pushABaseAddr + (i++ * MAGIC.ptrSize)), 10, 6, line++, BLUESCREEN_COLOR);
        }
    }

    /* analyze callstack
    *
    * */
    @SJC.Inline
    public static void printCallStack(int interruptEbp, int interruptNo){

        int ebp = interruptEbp;
        int line = 1;
        int xStart = 25;
        LowlevelOutput.printStr("---CALL STACK---", xStart, line++, BLUESCREEN_COLOR);
        LowlevelOutput.printStr("EBPs:", xStart, line, BLUESCREEN_COLOR);
        LowlevelOutput.printStr("EIPs:", xStart+11, line++, BLUESCREEN_COLOR);
        line++;

        int analyzedEbp = 0;
        int analyzedEip = 0;

        do{
            analyzedEbp = MAGIC.rMem32(ebp);
            if (ebp != interruptEbp) {
                analyzedEip = MAGIC.rMem32(ebp + 4);

            } else {
                // interrupt method has different stack frame
                // if it has a param it must be jumped over too
                int offsetAddedByParam = 0;
                if (0x08 <= interruptNo && interruptNo <=0x0E) {
                    offsetAddedByParam = MAGIC.ptrSize;
                }
                analyzedEip = MAGIC.rMem32(ebp + (1 + pushARegNames.length)*MAGIC.ptrSize + offsetAddedByParam);
            }

            LowlevelOutput.printHex(analyzedEbp, 10, xStart, line, BLUESCREEN_COLOR);
            LowlevelOutput.printHex(analyzedEip, 10, xStart+11, line, BLUESCREEN_COLOR);
            //LowlevelOutput.printStr(getMethod(analyzedEip).limit(33),xStart+22, line, BLUESCREEN_COLOR);
            line++;

            ebp = analyzedEbp;

        } while (analyzedEbp < STACK_START-8 && line < 24);
    }

    /*
        find method name of given eip
        todo do it right does currently not handle invalid EIPs in nirvana very well...
        its kind of a "dirty" solution.
     */
    public static String getMethod(int eip){
        // allign
        eip = (eip + 0x3) & ~0x3;

        int methodBlock = MAGIC.cast2Ref(MAGIC.clssDesc("SMthdBlock"));

        SMthdBlock currentBlock = null;

        boolean found = false;
        while (!found) {

            // find the type field of the current code block
            int i = -1;
            while (MAGIC.rMem32(eip - MAGIC.ptrSize * (++i)) != methodBlock) ;
            // now we should be on the type field

            int typeAddr = eip - MAGIC.ptrSize * i;
            int mthAddr = typeAddr + MAGIC.ptrSize;

            currentBlock = (SMthdBlock) MAGIC.cast2Obj(mthAddr);

            // do some validity tests (we may have hit a code pattern that just looks like an type descriptor
            if (currentBlock._r_relocEntries == MAGIC.getInstRelocEntries("SMthdBlock")
                    && currentBlock.owner instanceof SClassDesc) {

                found = true;
            }
        }

        return String.concat(String.concat(currentBlock.owner.name, "."), currentBlock.namePar);
    }
}
