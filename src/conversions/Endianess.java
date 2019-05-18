package conversions;

public class Endianess {
    public static int convert(int i){
        return i<<24 | i>>8 & 0xff00 | i<<8 & 0xff0000 | i>>>24;
    }

    public static short convert(short i){
        return (short)(i<<8 | (i>>8) & 0xff);
    }

    public static byte[] convert(byte[] inpBytes){
        byte[] outBytes = new byte[inpBytes.length];

        for (int i = 0; i < inpBytes.length; i++){
            outBytes[i] = inpBytes[inpBytes.length - 1 - i];
        }

        return outBytes;
    }

    public static void convertInPlace(byte[] b){
        for (int i = 0; i < b.length / 2; i++){
            byte temp = b[b.length - 1 - i];
            b[b.length - 1 - i] = b[i];
            b[i] = temp;
        }
    }
}
