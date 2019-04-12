package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;

public class Bluescreen extends InterruptReceiver {
    public static final int BLUESCREEN_COLOR = Color.BLACK << 4 | Color.GREY;  // red on black background

    @Override
    public void handleInterrupt(int interruptNo, int param) {
        LowlevelOutput.clearScreen(BLUESCREEN_COLOR);

        switch(interruptNo) {

            case Interrupts.DIVIDE_ERROR:
                LowlevelOutput.printStr("ZERO DIVISION ERROR", 30, 12, BLUESCREEN_COLOR);
                while (true) ;


            case Interrupts.DEBUG_EXCEPTION:
                LowlevelOutput.printStr("DEBUG EXCEPTION", 30, 12, BLUESCREEN_COLOR);
                Kernel.wait(3);
                break;

            case Interrupts.NMI:
                // is currently missused for switching between tasks
                // todo uncomment later on
                //LowlevelOutput.printChar("NON MASKABLE INTERRUPT (NMI)", 30, 12, BLUESCREEN_COLOR);
                //while (true) ;

            case Interrupts.BREAKPOINT:
                LowlevelOutput.printStr("BREAKPOINT", 30, 12, BLUESCREEN_COLOR);
                Kernel.wait(3);
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

    }
}
