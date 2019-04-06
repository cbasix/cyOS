package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;

public class ScreenOutput extends InterruptReceiver {
    int cnt = 0;
    @Override
    public void handleInterrupt(int interruptNo, int param) {
        if (interruptNo!= Interrupts.TIMER) {
            LowlevelOutput.printHex(interruptNo, 2, 76, 24, Color.GREEN);
            //LowlevelOutput.printHex(cnt, 10, 70, 6, Color.GREEN);
        }
        cnt++;
    }
}
