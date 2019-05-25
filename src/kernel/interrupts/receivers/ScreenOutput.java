package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.interrupts.core.InterruptReceiver;
import kernel.interrupts.core.Interrupts;
/*
    Shows incomming interrupts in the top right corner (except the timer interrupt)
 */
public class ScreenOutput extends InterruptReceiver {
    //int cnt = 0;
    @Override
    public boolean handleInterrupt(int interruptNo, int param) {
        if (interruptNo!= Interrupts.TIMER) {
            LowlevelOutput.printHex(interruptNo, 2, 76, 0, Color.CYAN << 4 | Color.BLACK);
            //LowlevelOutput.printHex(cnt, 10, 70, 6, Color.GREEN);
        }
        //cnt++;
        return false;
    }


}
