package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.interrupts.core.InterruptReceiver;

public class ScreenOutput extends InterruptReceiver {
    int cnt = 0;
    @Override
    public void handleInterrupt(int interruptNo, int param) {
        if (interruptNo!= 0x20) {
            LowlevelOutput.printHex(interruptNo, 2, 78, 5, Color.GREEN);
            LowlevelOutput.printHex(cnt, 10, 70, 6, Color.GREEN);
        }
        cnt++;
    }
}
