package io;


public class GreenScreenOutput {
    public static final String alphabet = "0123456789ABCDEF";
    public static final int WIDTH = 80;
    public static final int HEIGHT = 25;
    private int virtualCursor = 0;
    public static final int VID_MEM_BASE = 0xB8000;
    private VidMem vidMem =(VidMem) MAGIC.cast2Struct(VID_MEM_BASE);
    private int color = Color.DEFAULT_COLOR;
    char[] stringBuffer = new char[20];


    public static class VidChar extends STRUCT {
        public byte ascii, color;
    }

    public static class VidMem extends STRUCT {
        @SJC(offset=0,count=2000)
        public VidChar[] chars;
        //@SJC(offset=0,count=2000)
        //public short[] chars;
    }


    public void setColor(int fg, int bg) {
        color = bg << 4 | fg;
    }
    public int getColorIState() {
        return color;
    }
    public void setColorState(int c){
        color = c;
    }

    public void setCursor(int x, int y) {
        virtualCursor = y * WIDTH + x;
    }
    public void setCursor(int cursor) {
        virtualCursor = cursor;
    }

    public int getCursor() {
        return virtualCursor;
    }

     // ---------- STRING / CHAR ---------------
    @SJC.Inline
    public void print(char c) {
        VidChar vidChar = vidMem.chars[virtualCursor];
        vidChar.ascii = (byte) c;
        vidChar.color = (byte) color;

        virtualCursor++;

        // If at end of screen jump to top left
        if (virtualCursor >= WIDTH * HEIGHT){
            virtualCursor = 0;
        }
    }

    /* print string with linebreaks

     */
    public void print(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c){
                case '\n': println(); break;
                case '\t': print("    "); break;
                default: print(str.charAt(i));
            }
        }
    }

    /* print char array in ONE line

     */
    public void print(char[] str) {
        for (int i = 0; i < str.length; i++) {
            print(str[i]);
        }
    }

    // ---------- HEX ---------------

    @SJC.Inline
    public void printHex(int value, int digits){
        for (int i = digits-1; i >= 0; i--) {
            print(alphabet.charAt((int)(value >> i*4) & 0xF));
        }
    }
    // TODO remove duplication?!
    @SJC.Inline
    public void printHex(long value, int digits){
        for (int i = digits-1; i >= 0; i--) {
            print(alphabet.charAt((int)(value >> i*4) & 0xF));
        }
    }

    public void printHex(byte value) {
        printHex((int) value);
    }

    public void printHex(short value) {
        printHex((int) value, 4);
    }

    public void printHex(int value) {
        printHex(value, 8);
    }

    public void printHex(long value) {
        printHex(value, 16);
        //printHex((int) value >> );
    }

    //-------- boolean -----------
    public void print(boolean b) {
        if (b){
            print("True");
        } else {
            print("False");
        }
    }

    // ---------- INT /LONG ---------------
    @SJC.Inline
    public void print(int value){
        print(value, 0);
    }

    /**
     * @param value the value to convert to a string using base 10
     * @param wantedDigits 0 means -> use as many digits as the number needs
     *                     all other values -> use exacly n digits
     */
    public void print(int value, int wantedDigits) {
        // long can have max 19 digits in base 10 plus a sign up front
        // limit wanted digits to buffer size
        if (wantedDigits > stringBuffer.length-1){
            wantedDigits = stringBuffer.length-1;
        }
        // int can have max 10 digits in base 10 plus a sign up front
        boolean isNegative = value < 0;
        if (isNegative) {
            // 2er komplement positiv machen
            value -= 1;
            value = ~value;
        }

        int remainder;
        int pos = 19;
        do {
            remainder = value % 10;
            stringBuffer[pos] = alphabet.charAt(remainder);
            value /= 10;
            pos--;
        } while ((value > 0 && wantedDigits == 0) || wantedDigits >  stringBuffer.length - 1 - pos);

        // if orig value was positive _add minus sign
        if (isNegative) {
            stringBuffer[pos] = '-';
        } else {
            pos++;
        }

        for (int i = pos; i < stringBuffer.length; i++) {
            print(stringBuffer[i]);
        }
    }

    @SJC.Inline
    public void print(long value){
        print(value, 0);
    }

    // TODO How to avoid duplicated code here? without casting everything to long?
    /**
     * @param value the value to convert to a string using base 10
     * @param wantedDigits 0 means -> use as many digits as the number needs
     *                     all other values -> use exacly n digits
     */
    public void print(long value, int wantedDigits) {
        // long can have max 19 digits in base 10 plus a sign up front
        // limit wanted digits to buffer size
        if (wantedDigits > stringBuffer.length-1){
            wantedDigits = stringBuffer.length-1;
        }
        /*if (value <= 0x7FFFFFFFL && value >= -0x7FFFFFFFL){
            print((int) value, wantedDigits);
        } else {*/

        boolean isNegative = value < 0L;
        if (isNegative) {
            // positiv machen
            value = -value;
        }

        int remainder;
        int pos = stringBuffer.length - 1;
        do {
            remainder = (int) (value % 10);
            stringBuffer[pos] = alphabet.charAt(remainder);
            value /= 10;
            pos--;
        } while ((value > 0 && wantedDigits == 0) || wantedDigits >  stringBuffer.length - 1 - pos);

        // if orig value was positive _add minus sign
        if (isNegative) {
            stringBuffer[pos] = '-';
        } else {
            pos++;
        }

        for (int i = pos; i < stringBuffer.length; i++) {
            print(stringBuffer[i]);
        }

    }

    // ---------- PRINTLN ---------------

    public void println() {
        print((char) 0);
        while ((virtualCursor % WIDTH) != 0) {
            print((char) 0);
        }
    }

    //vorgegebene methoden
    public void println(char c) {
        print(c);
        println();
    }

    public void println(int i) {
        print(i);
        println();
    }

    public void println(long l) {
        print(l);
        println();
    }

    public void println(String str) {
        print(str);
        println();
    }

    public void println(char[] str) {
        print(str);
        println();
    }

    public void println(boolean b) {
        print(b);
        println();
    }


}
