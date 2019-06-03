package kernel.interrupts.core;

import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.receivers.ScreenOutput;
import kernel.memory.BasicMemoryManager;
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
                LowlevelOutput.printHex(cause, 8, xStart+11, titleLine+2, BLUESCREEN_COLOR);
                LowlevelOutput.printStr("Param:   0x", xStart, titleLine+3, BLUESCREEN_COLOR);
                LowlevelOutput.printHex(param, 8, xStart+11, titleLine+3, BLUESCREEN_COLOR);


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
        while(i==0){Kernel.sleep();};

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
            LowlevelOutput.printHex(MAGIC.rMem32(pushABaseAddr + (i++ * MAGIC.ptrSize)), 8, 6, line++, BLUESCREEN_COLOR);
        }
    }

    /* analyze callstack
    *
    * */
    @SJC.Inline
    public static void printCallStack(int interruptEbp, int interruptNo){

        int ebp = interruptEbp;
        int line = 1;
        int xStart = 22;
        LowlevelOutput.printStr("---CALL STACK---", xStart, line++, BLUESCREEN_COLOR);
        LowlevelOutput.printStr("EBPs:", xStart, line, BLUESCREEN_COLOR);
        LowlevelOutput.printStr("EIPs:", xStart+9, line++, BLUESCREEN_COLOR);
        //line++;

        int analyzedEbp = 0;
        int analyzedEip = 0;

        int page = 0;

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

            LowlevelOutput.printHex(analyzedEbp, 8, xStart, line, BLUESCREEN_COLOR);
            LowlevelOutput.printHex(analyzedEip, 8, xStart+9, line, BLUESCREEN_COLOR);
            LowlevelOutput.printStr(getEipMethodInfo(analyzedEip).limit(39),xStart+18, line, BLUESCREEN_COLOR);
            line++;

            ebp = analyzedEbp;

            // rudimentary multi page call stacks...
            if (line == 24){ line = 0; LowlevelLogging.debug(String.concat(String.from(page++), "     "));}

        } while (analyzedEbp != 0 && analyzedEbp < STACK_START-8 && line <= 24);


    }


    public static SMthdBlock getMethodBlockAt(int eip){
        BasicMemoryManager.ImageInfo image = (BasicMemoryManager.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        Object o = MAGIC.cast2Obj(image.firstObjInImageAddr);
        Object bestMatch = null;
        int bestMatchDistance = 0;

        // out of image eips are not valid...
        if (eip < MAGIC.imageBase || eip > MAGIC.imageBase + image.size){
            return null;
        }

        while (o._r_next != null) {

            if (o instanceof SMthdBlock){

                int distance = eip - MAGIC.cast2Ref(o);
                if (distance >= 0 &&
                        (bestMatch == null || distance < bestMatchDistance)
                ){
                    bestMatch = o;
                    bestMatchDistance = distance;
                }
            }
            o = o._r_next;
        }
        return (SMthdBlock) bestMatch;
    }

    // todo this whole method does not work
    public static int getSourceLine(SMthdBlock mthdBlock, int eip){
        // todo ask what the ints in the lineIncodeOffset array mean. no documentation found.

        int offset = eip - (MAGIC.cast2Ref(mthdBlock) + MAGIC.getCodeOff()); //MAGIC.getInstScalarSize("SMthdBlock"));
        //int offset = eip - MAGIC.cast2Ref(mthdBlock);

        if (mthdBlock.lineInCodeOffset == null){
            return -1;
        }
        if (offset < 0){
            return -3;
        }


        /*GreenScreenOutput out = new GreenScreenOutput();
        out.setCursor(0, 1);
        out.print("mthd: "); out.println(mthdBlock.namePar);
        out.print("len: "); out.println(String.from(mthdBlock.lineInCodeOffset.length));
        out.print("offset: "); out.println(String.from(offset));

        LowlevelLogging.printHexdump(MAGIC.addr(mthdBlock.lineInCodeOffset[0]));
        Kernel.wait(10);*/

        // for each code line there are two entries the first is the offset the line begins at
        // within the opcodes, the second one is the line no within the real code
        for (int line = 0; line < mthdBlock.lineInCodeOffset.length; line += 2){
            if (mthdBlock.lineInCodeOffset[line] > offset && line > 0){ // todo check
                return mthdBlock.lineInCodeOffset[line-1]; // we are stepping over the error line, so it should be the last entry
            }
        }

        return mthdBlock.lineInCodeOffset[mthdBlock.lineInCodeOffset.length-1];
    }


    public static String getEipMethodInfo(int eip){
        SMthdBlock currentBlock = getMethodBlockAt(eip);

        if (currentBlock != null) {
            return String.concat(
                    String.concat(
                           String.from(getSourceLine(currentBlock, eip)),
                            String.concat(":", currentBlock.owner != null ? currentBlock.owner.name : "noOwner")
                    ),
                    String.concat(
                            ".",
                            currentBlock.namePar == null || currentBlock.namePar.length() == 0 ? "noName" : currentBlock.namePar
                    )

            );
        } else {
            return "No method block found";
        }
    }
}
