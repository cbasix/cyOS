package tests.highlevel;

import io.LowlevelLogging;
import datastructs.RingBuffer;

public class RingBufferTest {
    public static int test(){
        RingBuffer rb = new RingBuffer(5);

        rb.push("one");
        String r = (String) rb.get();
        if (!r.equals("one")) {
            LowlevelLogging.debug(r, LowlevelLogging.ERROR);
            return 201;
        }
        if (rb.count() != 0) { return 202;}

        rb.push("two");
        if (rb.count() != 1) { return 203;}

        rb.push("three");
        rb.push("four");
        rb.push("five");
        if (rb.count() != 4) { return 204;}

        // todo check test
        if (!((String)rb.peekPushed(0)).equals("five")) {
            LowlevelLogging.debug(((String)rb.peekPushed(0)), LowlevelLogging.ERROR);
            return 205;
        }
        if (!((String)rb.peekPushed(1)).equals("four")) { return 206;}


        rb.push("six");
        if (rb.count() != 5) { return 207;}

        rb.push("seven");

        if (rb.count() != 5) { return 208;}

        r = (String) rb.get();
        if (!r.equals("three")) {
            LowlevelLogging.debug(r, LowlevelLogging.ERROR);
            return 203;
        }
        r = (String) rb.get();
        if (!r.equals("four")) { return 270;}

        r = (String) rb.get();
        if (!r.equals("five")) { return 275;}


        if (rb.count() != 2) { return 280;}

        for (int i = 0; i < 30000; i++){
            rb.push("test");
        }

        for (int i = 0; i < 30000; i++){
            Object o = rb.get();
        }

        return 0;
    }
}
