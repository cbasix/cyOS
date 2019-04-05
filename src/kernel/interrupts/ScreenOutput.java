package kernel.interrupts;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class ScreenOutput extends InterruptReceiver{
    @Override
    public void handleInterrupt(int interruptNo, int param) {
        if (interruptNo!= 0x20) {
            LowlevelOutput.printHex(interruptNo, 2, 78, 5, Color.GREEN);
        }
    }
}
