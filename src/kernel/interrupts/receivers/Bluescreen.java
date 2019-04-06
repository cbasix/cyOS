package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.interrupts.core.InterruptReceiver;

public class Bluescreen extends InterruptReceiver {
    public static final int BLUESCREEN_COLOR = Color.BLACK << 4 | Color.GREY;  // red on black background

    @Override
    public void handleInterrupt(int interruptNo, int param) {
        LowlevelOutput.clearScreen(BLUESCREEN_COLOR);
        LowlevelOutput.printStr("ZERO DIVISION ERROR", 30, 12, BLUESCREEN_COLOR);
        while (true);
    }
}
