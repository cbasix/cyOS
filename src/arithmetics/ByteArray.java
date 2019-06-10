package arithmetics;

import io.LowlevelLogging;

public class ByteArray {
    public static boolean equals(byte[] a1, byte[] a2){
        if (a1.length != a2.length){
            return false;
        }

        for (int i = 0; i < a1.length; i++){
            if (a1[i] != a2[i]){
                return false;
            }
        }

        return true;
    }

    public static int countStartOnes(byte[] a){
        int pos = 0;
        for (int i = 0; i < a.length; i++){
            for (int b = 7; b >= 0; b--){
                if(((a[i] >>> b) & 1)  != 1){
                    return pos;
                }
                pos++;
            }
        }
        return pos;
    }

    public static byte[] not(byte[] a){
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) ~a[i];
        }
        return result;
    }


    public static byte[] and(byte[] a, byte[] b){
        int len = Math.min(a.length, b.length);

        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = (byte) (a[i] & b[i]);
        }
        return result;
    }

    public static byte[] or(byte[] a, byte[] b){
        int len = Math.min(a.length, b.length);

        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = (byte) (a[i] | b[i]);
        }
        return result;
    }

    public static byte[] copy(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }
}
