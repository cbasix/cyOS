package io;



public class GreenScreenOutput {
    private static int virtualCursor = 0;

    private int color = GreenScreenConst.DEFAULT_COLOR;

    public void setColor(int fg, int bg) {
        color = bg << 4 | fg;
    }

    public void setCursor(int x, int y) {
        virtualCursor = y * GreenScreenConst.WIDTH + x;
    }

    public void print(char c) {
        virtualCursor += GreenScreenDirect.printChar(c, virtualCursor, 0, color);
    }

    public void print(int x) {
        virtualCursor += GreenScreenDirect.printInt(x, 10, 10, virtualCursor, color);
    }

    public void printHex(byte b) {
        virtualCursor += GreenScreenDirect.printInt((int) b, 16, 2, virtualCursor, color);
    }
    public void printHex(short s) {
        virtualCursor += GreenScreenDirect.printInt((int) s, 16, 4, virtualCursor, color);
    }
    public void printHex(int x) {
        virtualCursor += GreenScreenDirect.printInt(x, 16, 8, virtualCursor, color);
    }
    public void printHex(long x) {
        virtualCursor += GreenScreenDirect.printLong(x, 16, 16, virtualCursor, color);
    }
    public void print(long x) {
        virtualCursor += GreenScreenDirect.printLong(x, 10, 16, virtualCursor, color);
    }

    public void print(String str) {
        virtualCursor += GreenScreenDirect.printStr(str, virtualCursor, color);
    }

    public void println() {
        while((virtualCursor % GreenScreenConst.WIDTH) != 0) {
            print((char) 0);
        }
    }

    //vorgegebene methoden
    public void println(char c) { print(c); println(); }
    public void println(int i) { print(i); println(); }
    public void println(long l) { print(l); println(); }
    public void println(String str) { print(str); println(); }

}
