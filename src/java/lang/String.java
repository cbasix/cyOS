package java.lang;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class String {
    private char[] value;
    private int count;

    public String(char[] value) {
        this.value = new char[value.length];
        for (int i = 0; i < value.length; i++){
            this.value[i] = value[i];
        }
        this.count = value.length;
    }

    // todo improve
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
        return substring(from , this.length()-1);
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
}
