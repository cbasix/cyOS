package io;

public class Color {
    public static final int BLACK = 0;
    public static final int BLUE = 1;
    public static final int GREEN = 2;
    public static final int CYAN = 3;
    public static final int RED = 4;
    public static final int PINK = 5;
    public static final int GREY = 7;

    public static final int MOD_BRIGHT = 1 << 3;
    public static final int MOD_BLINK = 1 << 7;

    public static final int DEFAULT_COLOR = CYAN << 4 | BLACK; // black on cyan background
    public static final int ERROR_COLOR = BLACK << 4 | RED;  // red on black background
}
