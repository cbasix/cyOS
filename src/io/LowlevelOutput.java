package io;

public class LowlevelOutput {

    private static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";


    /*@SJC.Inline
    public static void printLong(long value, int base, int len, int cursor, int color) {
        return printLong(value, base, len, cursor, 0, color);
    }*/

    public static void printLong(long value, int base, int len, int x, int y, int color) {

        if (base > alphabet.length()+1 || base < 2) {
            LowlevelLogging.debug("Invalid Base", LowlevelLogging.ERROR);
            while (true) {}
        }

        if (value < 0) {
            printChar('-', x, y, color);
            // negative number convert 2er complement to positive
            value -= 1;
            value = ~value;
        } else {
            printChar(' ', x, y, color);
        }

        char tmp;
        for (int i = len - 1; i >= 1; i--) {
            printChar(alphabet.charAt((int) (value % base)), x + i, y, color);
            value /= base;
        }

    }

    /*@SJC.Inline
    public static void printInt(int value, int base, int len, int cursor, int color) {
        return printInt(value, base, len, cursor, 0, color);
    }*/

    // TODO find out how to not duplicate code here without using long for everything
    public static void printInt(int value, int base, int len, int x, int y, int color) {
        if (base > alphabet.length()+1 || base < 2) {
            LowlevelLogging.debug("Invalid Base", LowlevelLogging.ERROR);
            while (true) {}
        }

        if (value < 0) {
            printChar('-', x, y, color);
            // negative number convert 2er complement to positive
            value -= 1;
            value = ~value;
        } else {
            printChar(' ', x, y, color);
        }

        char tmp;
        for (int i = len - 1; i >= 1; i--) {
            printChar(alphabet.charAt((int) (value % base)), x + i, y, color);
            value /= base;
        }

    }

    /*@SJC.Inline
    public static void printHex(long value, int len, int cursor, int color) {
        return printHex(value, len, cursor, 0, color);
    }*/

    public static void printHex(long value, int len, int x, int y, int color) {
        for (int i = len - 1; i >= 0; i--) {
            printChar(alphabet.charAt((int)(value & 0xF)), x + i, y, color);
            value = value >> 4;
        }

    }

    /*@SJC.Inline
    public static void printStr(String str, int cursor, int color) {
        return printStr(str, cursor, 0, color);
    }*/

    public static void printStr(String str, int x, int y, int color) {
        int i;
        for (i = 0; i < str.length(); i++) {
            printChar(str.charAt(i), x + i, y, color);
        }

    }

    /*@SJC.Inline
    public static void printChar(char c, int cursor, int color) {
        return printChar(c, cursor, 0, color);
    }*/

    public static void printChar(char c, int x, int y, int color) {
        // TODO find a way not to cast struct for every printed char (is it cheap or not???).
        // TODO Can not be static. Rember: new / object stuff not wanted here, because this here is uses from within newInstance!
        GreenScreenConst.VidMem vidMem =(GreenScreenConst.VidMem) MAGIC.cast2Struct(GreenScreenConst.VID_MEM_BASE);
        //if pos is after end of screen, start up top again
        GreenScreenConst.VidChar vidChar = vidMem.chars[(y * GreenScreenConst.WIDTH + x) % (GreenScreenConst.WIDTH * GreenScreenConst.HEIGHT)];
        vidChar.ascii = (byte) c;
        vidChar.color = (byte) color;

    }

    public static void clearScreen(int color) {
        for (int i = 0; i < GreenScreenConst.WIDTH * GreenScreenConst.HEIGHT; i++) {
            printChar((char) 0, i, 0, color);
        }
    }
}
