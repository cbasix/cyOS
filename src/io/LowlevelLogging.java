package io;

import kernel.Kernel;

public class LowlevelLogging {


    public static final int FINER = 2;
    public static final int FINE = 3;
    public static final int INFO = 4;
    public static final int ERROR = 5;

    public static int DEBUG_LEVEL = ERROR;

    public static void printHexdump(int dumpStartAddr) {

        debug("Hexdmp A", FINER);
        for (int i = 0; i < GreenScreenConst.HEIGHT * 4; i++) {
            if (i % 4 == 0) {
                debug("Hexdmp B", FINER);

                //LowlevelOutput.printInt(dumpStartAddr + i, 10, 10, 39, GreenScreenConst.HEIGHT - 1 - i / 4, GreenScreenConst.ERROR_COLOR);
                LowlevelOutput.printHex(dumpStartAddr + i,10, 39, GreenScreenConst.HEIGHT - 1 - i / 4, GreenScreenConst.ERROR_COLOR);
                debug("Hexdmp C", FINER);

                int v = MAGIC.rMem32(dumpStartAddr + i);
                debug("Hexdmp D", FINER);

                LowlevelOutput.printInt(v, 10, 10, 70, GreenScreenConst.HEIGHT - 1 - i / 4, GreenScreenConst.DEFAULT_COLOR);
            }
            debug("Hexdmp X", FINER);

            byte t = MAGIC.rMem8(dumpStartAddr + i);
            debug("Hexdmp Y", FINER);

            LowlevelOutput.printHex(t, 2, 58 + 4-(i % 4) * 4, GreenScreenConst.HEIGHT - 1 - i / 4, GreenScreenConst.DEFAULT_COLOR);
            //LowlevelOutputTest.printInt(t,10, 3, 50, 0, GreenScreenConst.DEFAULT_COLOR);
        }
    }

    public static void debug(String str, int lvl) {
        if (lvl >= DEBUG_LEVEL) {
            LowlevelOutput.printStr(str, 0, 0, GreenScreenConst.DEFAULT_COLOR);
            Kernel.wait(3);
        }
    }
}
