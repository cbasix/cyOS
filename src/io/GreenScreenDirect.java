package io;

import kernel.Kernel;

public class GreenScreenDirect {

    private static final String alphabet="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ??????????????????????????";

    public static int printLong(long value, int base, int len, int cursor, int color) {return printLong(value, base, len, cursor, 0, color);}
    public static int printLong(long value, int base, int len, int x, int y, int color) {
        int i = len;
        while(i > 0){
            int remainder =(int) value % base;
            printChar(alphabet.charAt(remainder), x+i-1, y, color);
            GreenScreenDirect.printStr("in", 3, 20, Color.GREEN);
            Kernel.wait(5);
            i--;
            value = value / base;
        }
        return len;
    }

    // TODO remove duplicated code
    public static int printInt(int value, int base, int len, int cursor, int color) {return printInt(value, base, len, cursor, 0, color);}
    public static int printInt(int value, int base, int len, int x, int y, int color) {
        int i = len;
        while(i > 0){
            int remainder = value % base;
            printChar(alphabet.charAt(remainder), x+i-1, y, color);

            i--;
            value = value / base;
        }

        return len;
    }

    public static int printStr(String str, int cursor, int color) {return printStr(str, cursor, 0, color);}
    public static int printStr(String str, int x, int y, int color) {
        int i;
        for (i=0; i<str.length(); i++) {
            printChar(str.charAt(i), x + i, y, color);
        }
        return str.length();
    }

    public static int printChar(char c, int cursor, int color) {return printChar(c, cursor, 0, color); }
    public static int printChar(char c, int x, int y, int color) {
        //if pos is after end of screen, start up top again
        GreenScreenConst.VidMem vidMem = (GreenScreenConst.VidMem) MAGIC.cast2Struct(GreenScreenConst.VID_MEM_BASE);
        GreenScreenConst.VidChar vidChar = vidMem.chars[(y* GreenScreenConst.WIDTH + x) % (GreenScreenConst.WIDTH * GreenScreenConst.HEIGHT)];
        vidChar.ascii = (byte) c;
        vidChar.color = (byte) color;
        return 1;
    }

    public static void clearScreen(int color){
        int vidMem = GreenScreenConst.VID_MEM_BASE;
        int i;
        for (i = 0; i< GreenScreenConst.WIDTH * GreenScreenConst.HEIGHT; i++){
            printChar((char) 0, i, GreenScreenConst.DEFAULT_COLOR);
        }
    }
}
