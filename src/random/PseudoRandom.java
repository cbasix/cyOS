package random;

import io.RTC;
import kernel.Kernel;
import kernel.interrupts.receivers.TimerCounter;

public class PseudoRandom {
    public static int getRandInt(){
        int rand = 0;
        rand ^= RTC.read(RTC.HOUR);
        rand ^= RTC.read(RTC.MINUTE) << 8;
        rand ^= RTC.read(RTC.SECOND) << 16;
        rand ^= RTC.read(RTC.DAY) << 24;

        rand ^= TimerCounter.getCurrent();

        rand ^= MAGIC.cast2Ref(new byte[1]);
        rand ^= MAGIC.cast2Ref(Kernel.memoryManager);

        return rand;
    }
}
