package tests.lowlevel;

import io.Color;
import io.GreenScreenOutput;
import io.LowlevelOutput;

public class LowlevelOutputTest {
    private static GreenScreenOutput.VidMem vidMem;
    public static int test() {
        vidMem = (GreenScreenOutput.VidMem) MAGIC.cast2Struct(GreenScreenOutput.VID_MEM_BASE);

        // PRINT INT
        // test printInt with different bases
        if (!printIntIsEqual(755, " 0755", 5)) {return 105;}

        // integer to long for given length -> show last digits
        if (!printIntIsEqual(784138845, " 8845", 5)) {return 110;}

        // negative number
        if (!printIntIsEqual(-784138845, "-784138845", 10)) {return 115;}


        // PRINT HEX
        if (!printHexIsEqual(0xFBC0505, "BC0505", 6)) {return 120;}


        return 0;
    }

    public static boolean printIntIsEqual(int integer, String string, int len){
        // 80 chars per line so x=4 y=1 should mean pos 84
        int pos = 84;
        LowlevelOutput.printInt(integer, 10, len, pos % GreenScreenOutput.WIDTH, pos / GreenScreenOutput.WIDTH, Color.RED);

        return checkVidMemEquals(string, pos);
    }

    public static boolean printHexIsEqual(int integer, String string, int len){
        // 80 chars per line so x=4 y=1 should mean pos 84
        int pos = 84;
        LowlevelOutput.printHex(integer, len, pos % GreenScreenOutput.WIDTH, pos / GreenScreenOutput.WIDTH, Color.RED);

        return checkVidMemEquals(string, pos);
    }

    private static boolean checkVidMemEquals(String string, int pos) {
        for (int i = 0; i < string.length(); i++) {
            if (vidMem.chars[pos + i].ascii != (byte) string.charAt(i)) {
                return false;
            }
        }
        return true;
    }
}
