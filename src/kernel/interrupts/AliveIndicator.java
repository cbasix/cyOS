package kernel.interrupts;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class AliveIndicator extends InterruptReceiver{
    private static final int divider = 8;

    private int cnt = 0;
    private int subCnt = 0;
    private String indicators = "▴▸▾◂";

    @Override
    public void handleInterrupt(int interruptNo, int param) {
        if (cnt % 8 == 0) {
            char c = indicators.charAt(subCnt);
            LowlevelOutput.printChar(c, 80, 25, Color.GREEN);
            subCnt = (++subCnt) % indicators.length();
        }
        cnt++;
    }
}
