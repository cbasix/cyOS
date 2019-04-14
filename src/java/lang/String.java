package java.lang;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class String {
    public static final String alphabet = "0123456789ABCDEF";
    private char[] value;
    private int count;

    private String(){}

    public String(char[] value) {
        this.value = new char[value.length];
        for (int i = 0; i < value.length; i++){
            this.value[i] = value[i];
        }
        this.count = value.length;
    }

    // todo improve nearly everything below this line...
    public String[] split(char delimiter){
        int parts = 1;
        for (int i = 0; i < value.length; i++){
            if (this.charAt(i) == delimiter){
                parts++;
            }
        }
        LowlevelOutput.printHex(parts, 2, 55, 13, Color.RED);

        String[] out = new String[parts];

        int last = 0;
        parts = 0;
        for (int i = 0; i < value.length; i++){
            if (this.charAt(i) == delimiter){
                out[parts] = substring(last, i);

                last=i+1;
                parts++;
            }
            if (i == value.length-1){
                out[parts] = substring(last, value.length);
            }
        }

        return out;
    }

    public String substring(int from, int to){

        int j = 0;
        char[] newChars = new char[to-from];
        for (int i = from; i < to; i++){
            newChars[j++] = value[i];
        }

        return new String(newChars);
    }

    public String substring(int from) {
        return substring(from , this.length());
    }

    public static String join(String[] parts, String join){
        int newLen = 0;
        for (int i = 0; i < parts.length; i++){
            newLen += parts[i].length();
        }
        newLen += join.length() * (parts.length-1);

        char[] newChars = new char[newLen];

        int cnt = 0;
        for (int i = 0; i < parts.length; i++){
            for (int j = 0; j < parts[i].length(); j++) {
                newChars[cnt++] = parts[i].charAt(j);
            }
            if (i != parts.length-1) {
                for (int j = 0; j < join.length(); j++) {
                    newChars[cnt++] = join.charAt(j);
                }
            }
        }
        return new String(newChars);
    }

    public String trim(){
        int start = 0, stop = value.length;

        for (int i = value.length-1; i >= 0; i--){
            if (value[i] != ' '){
                stop = i+1;
                break;
            }
        }

        for (int i = 0; i < value.length; i++){
            if (value[i] != ' '){
                start = i;
                break;
            }
        }

        return substring(start, stop);
    }

    @SJC.Inline
    public int length() {
        return count;
    }

    @SJC.Inline
    public char charAt(int i) {
        return value[i];
    }

    public boolean equals(String str) {
        if (str.length() != length()){
            return false;
        }

        for (int i = 0; i < length(); i++){
            if (str.charAt(i) != charAt(i)){
                return false;
            }
        }

        return true;
    }

    public int countOccurences(char c){
        int cnt = 0;
        for (int i = 0; i < length(); i++){
            if (c == charAt(i)){
                cnt++;
            }
        }
        return cnt;
    }

    public char[] toChars(){
        char[] chars = new char[count];
        for(int i = 0; i < count; i++){
            chars[i] = value[i];
        }
        return chars;
    }

    // dirty
    public static String concat(String first, String second) {
        String[] temp = new String[2];
        temp[0] = first;
        temp[1] = second;
        return String.join(temp, "");
    }

    public String concat(String other){
        return String.concat(this, other);
    }

    // feels filthy -> min 3 array copys ...
    public static String from(int value){
        char[] tempStringBuffer = new char[11];
        // int can have max 10 digits in base 10 plus a sign up front
        boolean isNegative = value < 0;
        if (isNegative) {
            // 2er komplement positiv machen
            value -= 1;
            value = ~value;
        }
        int remainder;
        int pos = tempStringBuffer.length - 1;;
        do {
            remainder = value % 10;
            tempStringBuffer[pos] = alphabet.charAt(remainder);
            value /= 10;
            pos--;
        } while (value > 0);

        // if orig value was positive _add minus sign
        if (isNegative) {
            tempStringBuffer[pos] = '-';
        } else {
            pos++;
        }

        // avoid unnecessary copy
        String ret = new String();
        ret.value = tempStringBuffer;
        ret.count = ret.value.length;
        return ret.substring(pos);
    }


    public static String hexFrom(int value){
        final int DIGITS = 8;
        char[] strBuf = new char[DIGITS];
        for (int i = DIGITS-1; i >= 0; i--) {
            strBuf[DIGITS-1-i] = String.alphabet.charAt((int)(value >> i*4) & 0xF);
        }

        // avoid copy
        // todo avoid duplication
        String ret = new String();
        ret.value = strBuf;
        ret.count = ret.value.length;
        return ret;
    }
}
