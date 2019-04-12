package tests.highlevel;

import io.LowlevelLogging;
import kernel.datastructs.RingBuffer;

public class RingBufferTest {
    public static int test(){
        RingBuffer rb = new RingBuffer(5);

        rb.push("eins");
        String r = (String) rb.get();
        if (!r.equals("eins")) {
            LowlevelLogging.debug(r, LowlevelLogging.ERROR);
            return 201;
        }
        if (rb.count() != 0) { return 202;}

        rb.push("zwei");
        if (rb.count() != 1) { return 203;}

        rb.push("drei");
        rb.push("vier");
        rb.push("fünf");
        if (rb.count() != 4) { return 204;}
        // todo check test
        //if (((String)rb.peekPushed(0)).equals("fünf")) { return 205;}
        //if (((String)rb.peekPushed(1)).equals("vier")) { return 206;}


        rb.push("sechs");
        if (rb.count() != 5) { return 207;}

        rb.push("sieben");

        if (rb.count() != 5) { return 208;}

        r = (String) rb.get();
        if (!r.equals("drei")) {
            LowlevelLogging.debug(r, LowlevelLogging.ERROR);
            return 203;
        }
        r = (String) rb.get();
        if (!r.equals("vier")) { return 270;}

        r = (String) rb.get();
        if (!r.equals("fünf")) { return 275;}


        if (rb.count() != 2) { return 280;}

        return 0;
    }
}
