package tests.highlevel;

import conversions.Endianess;

public class EndianessTest {
    public static int test(){


        int i = 0x12345678;
        if (Endianess.convert(i) != 0x78563412){ return 2401; }

        short s = 0x1234;
        if (Endianess.convert(s) != (short)0x3412){ return 2402; }

        // test small even array
        byte[] t = new byte[2];
        t[0] = 55;
        t[1] = 88;

        Endianess.convertInPlace(t);

        if (t[0] != 88 || t[1] != 55) { return 2405;}

        //test big uneven array
        byte[] t2 = new byte[125];
        for(byte j = 0; j < 125; j++){
            t2[j] = j;
        }

        byte[] t3 = Endianess.convert(t2);

        for(byte j = 0; j < 125; j++) {
            if (t3[j] != t2[t2.length - 1 -j]) {
                return 2406;
            }
        }

        // convert back
        byte[] t4 = Endianess.convert(t3);
        for(byte j = 0; j < 125; j++) {
            if (t2[j] != t4[j]) {
                return 2408;
            }
        }

        return 0;
    }
}
