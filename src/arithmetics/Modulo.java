package arithmetics;

public class Modulo {
    public static int mod(int val, int mod){
        int remainder = val % mod;
        if (remainder < 0){
            remainder = -remainder;
        }

        return remainder;
    }
}
