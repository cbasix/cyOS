package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;

public class Bluescreen extends InterruptReceiver {
    public static final int BLUESCREEN_COLOR = Color.BLUE << 4 | Color.GREY;  // red on black background

    @Override
    public boolean handleInterrupt(int interruptNo, int param) {
        Interrupts.disable();
        LowlevelOutput.clearScreen(BLUESCREEN_COLOR);

        switch(interruptNo) {

            case Interrupts.DIVIDE_ERROR:
                LowlevelOutput.printStr("ZERO DIVISION ERROR", 30, 12, BLUESCREEN_COLOR);
                while (true) ;
                //break;


            case Interrupts.DEBUG_EXCEPTION:
                LowlevelOutput.printStr("DEBUG EXCEPTION", 30, 12, BLUESCREEN_COLOR);
                while (true) ;
                //break;

            case Interrupts.NMI:
                LowlevelOutput.printStr("NON MASKABLE INTERRUPT (NMI)", 30, 12, BLUESCREEN_COLOR);
                while (true) ;

            case Interrupts.BREAKPOINT:
                LowlevelOutput.printStr("BREAKPOINT", 30, 12, BLUESCREEN_COLOR);
                //while (true) ;
                break;

            case Interrupts.INTO_OVERFLOW:
                LowlevelOutput.printStr("INTO (Overflow)", 30, 12, BLUESCREEN_COLOR);
                while (true) ;

            case Interrupts.INVALID_OPCODE:
                LowlevelOutput.printStr("INVALID OPCODE", 30, 12, BLUESCREEN_COLOR);
                while (true) ;

            case Interrupts.GENERAL_PROTECTION_ERROR:
                LowlevelOutput.printStr("GENERAL PROTECTION ERROR", 30, 12, BLUESCREEN_COLOR);
                while (true) ;

            case Interrupts.PAGE_FAULT:
                LowlevelOutput.printStr("PAGE FAULT", 30, 12, BLUESCREEN_COLOR);
                while (true) ;
        }

        Interrupts.enable();
        return true;
    }
}
