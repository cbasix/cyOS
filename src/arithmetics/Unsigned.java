package arithmetics;

public class Unsigned {
    @SJC.Inline
    public static boolean isLessThan(int n1, int n2) {
        if (n1 >>> 1 != n2 >>> 1){
            return  (n1 >>> 1) < (n2 >>> 1);
        } else {
            return (n1 & 1) < (n2 & 1);
        }

        //return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
    }

    @SJC.Inline
    public static boolean isGreaterThan(int n1, int n2){
        if (n1 == n2){
            return false;
        } else {
            return !isLessThan(n1, n2);
        }
    }

    public static void divide(int n1, int n2){
        // todo implement
    }
}
