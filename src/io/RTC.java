package io;

public class RTC {
    public static final byte SECOND = 0;
    public static final byte MINUTE = 2;
    public static final byte HOUR = 4;
    public static final byte DAY = 7;
    public static final byte MONTH = 8;
    public static final byte YEAR = 9;

    public static int read(byte type) {
        MAGIC.wIOs8(0x70, type); //Register with addr auswählen
        return ((int)MAGIC.rIOs8(0x71)) & 0xFF; //Wert auslesen und den unsigned byte wert als signed int konvertieren
    }
}
