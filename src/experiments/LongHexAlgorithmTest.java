package experiments;

import io.LowlevelLogging;
import io.LowlevelOutput;

public class LongHexAlgorithmTest {
    public static final String alphabet = "0123456789ABCDEF";
    public static void main(String[] args) {
        long l = 0x24242424242424L;
        String should = "0024242424242424";
        char[] result = toHexString(l, 16);

        //System.out.print(result);

        for (int i = 0; i < should.length(); i++){
            if (result[i] != should.charAt(i)){
                //System.out.print("ERROR");
                LowlevelLogging.debug("ERROR", LowlevelLogging.ERROR);
                while(true){}
            }
        }
        //System.out.print("DONE");
        LowlevelLogging.debug("DONE", LowlevelLogging.ERROR);
        while(true){}
    }

    public static char[] toHexString(long value, int digits){
        char[] ret = new char[digits];
        for (int i = 0; i < digits; i++) {
            //System.out.print((int)(value >> i*4) & 0xF);
            ret[i] = (alphabet.charAt((int)(value >> (digits-1-i)*4) & 0xF));
        }
        return ret;
    }

}