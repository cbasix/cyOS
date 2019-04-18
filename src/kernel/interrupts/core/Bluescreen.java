package kernel.interrupts.core;

import io.Color;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;

public class Bluescreen {
    public static final int BLUESCREEN_COLOR = Color.BLUE << 4 | Color.GREY;  // red on black background

    public static class SavedRegs extends STRUCT {
        int EDI, ESI, EBP, ESP, EBX, EDX, ECX, EAX;
    }

    public static boolean handleInterrupt(int interruptNo, int param, int interruptEbp) {
        Interrupts.disable();
        LowlevelOutput.clearScreen(BLUESCREEN_COLOR);

        switch(interruptNo) {

            case Interrupts.DIVIDE_ERROR:
                LowlevelOutput.printStr("ZERO DIVISION ERROR", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.DEBUG_EXCEPTION:
                LowlevelOutput.printStr("DEBUG EXCEPTION", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.NMI:
                LowlevelOutput.printStr("NON MASKABLE INTERRUPT (NMI)", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.BREAKPOINT:
                LowlevelOutput.printStr("BREAKPOINT", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.INTO_OVERFLOW:
                LowlevelOutput.printStr("INTO (Overflow)", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.INVALID_OPCODE:
                LowlevelOutput.printStr("INVALID OPCODE", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.GENERAL_PROTECTION_ERROR:
                LowlevelOutput.printStr("GENERAL PROTECTION ERROR", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.PAGE_FAULT:
                LowlevelOutput.printStr("PAGE FAULT", 30, 12, BLUESCREEN_COLOR);
                break;

            case Interrupts.INDEX_OUT_OF_RANGE:
                LowlevelOutput.printStr("INDEX OUT OF RANGE", 30, 12, BLUESCREEN_COLOR);
                break;

            default:
                LowlevelOutput.printStr("UNKNOWN EXCEPTION", 30, 12, BLUESCREEN_COLOR);
                LowlevelOutput.printInt(interruptNo, 10, 10, 33, 13, BLUESCREEN_COLOR);
                break;
        }

        printRegisters(interruptEbp);
        printCallStack(interruptEbp, interruptNo);

        // allow continue after breakpoint
        //while (interruptNo != Interrupts.BREAKPOINT){}

        while(true){};

        Interrupts.enable();
        //return true;
    }

    public static final int STACK_START = 0x9BFFC;

    private static final String[] pushARegNames = {
            "EDI", "ESI", "EBP", "ESP", "EBX", "EDX", "ECX", "EAX"
    };

    @SJC.Inline
    public static void printRegisters(int interruptEbp) {
        int ebp = interruptEbp;
        int line = 1;
        int pushABaseAddr = interruptEbp + MAGIC.ptrSize; // start of pusha saved register values

        //SavedRegs regs = (SavedRegs) MAGIC.cast2Struct(pushABaseAddr);

        LowlevelOutput.printStr("Registers", 4, line++, BLUESCREEN_COLOR);
        line++;

        int i = 0;
        for (String regname : pushARegNames) {
            LowlevelOutput.printStr(regname, 1, line, BLUESCREEN_COLOR);
            LowlevelOutput.printHex(MAGIC.rMem32(pushABaseAddr + (i++ * MAGIC.ptrSize)), 10, 6, line++, BLUESCREEN_COLOR);
        }
    }

    /* analyze callstack */
    @SJC.Inline
    public static void printCallStack(int interruptEbp, int interruptNo){

        int ebp = interruptEbp;
        int line = 1;
        LowlevelOutput.printStr("CallStack", 61, line++, BLUESCREEN_COLOR);
        LowlevelOutput.printStr("EBPs:", 58, line, BLUESCREEN_COLOR);
        LowlevelOutput.printStr("EIPs:", 69, line++, BLUESCREEN_COLOR);
        line++;

        int analyzedEbp = 0;
        int analyzedEip = 0;

        do{
            analyzedEbp = MAGIC.rMem32(ebp);
            if (ebp != interruptEbp) {
                analyzedEip = MAGIC.rMem32(ebp + 4);

            } else {
                // interrupt method has different stack frame (addressing with offset from last ebp)
                // if it has a param it must be jumped over too
                int offsetAddedByParam = 0;
                if (0x08 <= interruptNo && interruptNo <=0x0E) {
                    offsetAddedByParam = MAGIC.ptrSize;
                }
                analyzedEip = MAGIC.rMem32(ebp + (1 + pushARegNames.length)*MAGIC.ptrSize + offsetAddedByParam);// todo check
            }

            LowlevelOutput.printHex(analyzedEbp, 10, 58, line, BLUESCREEN_COLOR);
            LowlevelOutput.printHex(analyzedEip, 10, 69, line++, BLUESCREEN_COLOR);

            ebp = analyzedEbp;

        } while (analyzedEbp < STACK_START-8 && line < 24);
    }
}
