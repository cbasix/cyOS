package tests.highlevel;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.datastructs.RingBuffer;

public class StringTest {
    public static int test(){
        String test = "test";
        String test2 = "test2";
        String tete = "tete";
        String test_ = "test";

        if (!test.equals(test_)){ return 301;}
        if (test.equals(test2)){ return 302;}
        if (test.equals(tete)){ return 303;}

        String s = "test blubb ubu";
        String[] splitted = s.split(' ');
        if (!splitted[0].equals("test")){ return 310;}

        if (!splitted[1].equals("blubb")){
            LowlevelOutput.printStr(splitted[1], 55, 15, Color.PINK);
            return 311;
        }
        if (!splitted[2].equals("ubu")){
            LowlevelOutput.printStr(splitted[2], 55, 15, Color.PINK);
            return 312;
        }

        String joined = String.join(splitted, "+");
        if (!joined.equals("test+blubb+ubu")){
            LowlevelOutput.printStr(joined, 55, 15, Color.PINK);
            return 320;
        }

        joined = String.join(splitted, "+-+");
        if (!joined.equals("test+-+blubb+-+ubu")){
            LowlevelOutput.printStr(joined, 55, 15, Color.PINK);
            return 326;
        }

        s = "echo bla bla bla";
        splitted = s.split(' ');
        if (!splitted[0].equals("echo")){ return 330;}
        if (!splitted[3].equals("bla")){ return 331;}

        joined = String.join(splitted, "+");
        if (!joined.equals("echo+bla+bla+bla")){
            LowlevelOutput.printStr(joined, 55, 15, Color.PINK);
            return 334;
        }

        s = "   test    ";
        s = s.trim();
        if (!s.equals("test")){
            LowlevelOutput.printStr(s, 55, 15, Color.GREY << 4 |Color.PINK);
            return 336;
        }

        s = "l1\n test\nl3";
        int c = s.countOccurences('\n');
        if (c != 2){
            LowlevelOutput.printStr(s, 55, 15, Color.GREY << 4 |Color.PINK);
            LowlevelOutput.printInt(c, 10, 4, 55, 16, Color.GREY << 4 |Color.PINK);
            return 340;
        }

        s = "blubb78";
        char[] ca = s.toChars();
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) != ca[i]){return 342;};
        }

        return 0;
    }
}
