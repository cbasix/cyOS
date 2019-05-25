package io;

public class LowlevelOutput {

    private static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    /*
    public static void printLong(long value, int base, int len, int cursor, int color) {
        return printLong(value, base, len, cursor, 0, color);
    }*/
    //~@SJC.Inline
    public static void printLong(long value, int base, int len, int x, int y, int color) {

        if (base > alphabet.length()+1 || base < 2) {
            LowlevelLogging.debug("Invalid Base", LowlevelLogging.ERROR);
            while (true) {}
        }

        if (value < 0) {
            printChar('-', x, y, color);
            // negative number convert 2er complement to positive
            value = -value;
        } else {
            printChar(' ', x, y, color);
        }

        char tmp;
        for (int i = len - 1; i >= 1; i--) {
            printChar(alphabet.charAt((int) (value % base)), x + i, y, color);
            value /= base;
        }

    }

    //~@SJC.Inline
    public static void printInt(int value, int base, int len, int x, int y, int color) {
        if (base > alphabet.length()+1 || base < 2) {
            LowlevelLogging.debug("Invalid Base", LowlevelLogging.ERROR);
            while (true) {}
        }

        if (value < 0) {
            printChar('-', x, y, color);
            // negative number convert positive
            value = -value;
        } else {
            printChar(' ', x, y, color);
        }

        char tmp;
        for (int i = len - 1; i >= 1; i--) {
            printChar(alphabet.charAt((int) (value % base)), x + i, y, color);
            value /= base;
        }

    }

    //~@SJC.Inline
    public static void printHex(long value, int len, int x, int y, int color) {
        for (int i = len - 1; i >= 0; i--) {
            printChar(alphabet.charAt((int)(value & 0xF)), x + i, y, color);
            value = value >> 4;
        }

    }

    //-------- boolean -----------
    //~@SJC.Inline
    public static void printBool(boolean b, int x, int y, int color) {
        if (b){
            printStr("True", x, y, color);
        } else {
            printStr("False", x, y, color);
        }
    }

    //~@SJC.Inline
    public static void printStr(String str, int x, int y, int color) {
        int i;
        for (i = 0; i < str.length(); i++) {
            printChar(str.charAt(i), x + i, y, color);
        }

    }

    //~@SJC.Inline
    public static void printChar(char c, int x, int y, int color) {
        // vidMem can not be static. Rember: new / object stuff not wanted here, because this here is used from within newInstance!
        GreenScreenOutput.VidMem vidMem =(GreenScreenOutput.VidMem) MAGIC.cast2Struct(GreenScreenOutput.VID_MEM_BASE);
        //if pos is after end of screen, start up top again
        GreenScreenOutput.VidChar vidChar = vidMem.chars[(y * GreenScreenOutput.WIDTH + x) % (GreenScreenOutput.WIDTH * GreenScreenOutput.HEIGHT)];
        vidChar.ascii = (byte) c;
        vidChar.color = (byte) color;

    }

    //~@SJC.Inline
    public static void clearScreen(int color) {
        for (int i = 0; i < GreenScreenOutput.WIDTH * GreenScreenOutput.HEIGHT; i++) {
            printChar((char) 0, i, 0, color);
        }
    }

    //~@SJC.Inline
    public static void disableCursor(){
        MAGIC.wIOs8(0x3D4, (byte)0x0A); // index    setzen e                    hexa f
        MAGIC.wIOs8(0x3D5, (byte)0x20); // value    datum pos oberere 8 bits    untere 8bits
    }
}
