package kernel.interrupts.receivers;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.interrupts.core.InterruptReceiver;

public class AliveIndicator extends InterruptReceiver {
    private static final int divider = 4;

    private int cnt = 0;
    private int subCnt = 0;
    private String indicators = "-\\|/-\\|/"; //;"▴▸▾◂"

    @Override
    public void handleInterrupt(int interruptNo, int param) {
        if (cnt % divider == 0) {
            char c = indicators.charAt(subCnt);
            LowlevelOutput.printChar(c, 79, 0, Color.CYAN << 4 | Color.BLACK);
            subCnt = (++subCnt) % indicators.length();
        }
        cnt++;
        //MAGIC.wMem16(0xB8F9E, (short)(2|0x2F00));
    }
}
