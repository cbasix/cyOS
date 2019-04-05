package kernel.interrupts;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class Bluescreen extends InterruptReceiver{

    @Override
    public void handleInterrupt(int interruptNo, int param) {
        LowlevelOutput.clearScreen(Color.BLUE);
        LowlevelOutput.printStr("ZERO DIVISION ERROR", 25, 13, Color.RED);
        while (true);
    }
}
