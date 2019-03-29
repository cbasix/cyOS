package io;

public class GreenScreenConst {
    public static final int VID_MEM_BASE = 0xB8000;
    public static final int DEFAULT_COLOR = Color.CYAN << 4 | Color.BLACK; // black on cyan background
    public static final int WIDTH = 80;
    public static final int HEIGHT = 25;
    public static final int ERROR_COLOR = Color.BLACK << 4 | Color.RED;  // red on black background

    public static class VidChar extends STRUCT {
        public byte ascii, color;
    }

    public static class VidMem extends STRUCT {
        @SJC(offset = 0, count = 2000)
        public VidChar[] chars;
        //@SJC(offset=0,count=2000)
        //public short[] chars;
    }
}
