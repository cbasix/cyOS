package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelOutput;
import kernel.interrupts.core.InterruptReceiver;

public class TimerCounter extends InterruptReceiver {
    private static int cnt = 0;

    @Override
    public boolean handleInterrupt(int interruptNo, int param) {
        cnt++;
        return true;
    }

    public static int getCurrent(){
        return cnt;
    }
}
