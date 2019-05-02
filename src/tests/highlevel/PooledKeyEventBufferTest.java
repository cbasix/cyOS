package tests.highlevel;

import datastructs.RingBuffer;
import drivers.keyboard.KeyboardEvent;
import drivers.keyboard.PooledKeyboardEventRingBuffer;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class PooledKeyEventBufferTest {
    public static int test(){
        PooledKeyboardEventRingBuffer buf = new PooledKeyboardEventRingBuffer(5);

        KeyboardEvent e = new KeyboardEvent();
        e.key = 42;
        KeyboardEvent e2 = new KeyboardEvent();


        // null
        if (buf.getCopyInto(e2)){ return 1401;}

        // put something in and get it out again
        buf.pushCopyOf(e);
        if (!buf.getCopyInto(e2)){ return 1403;}
        if (e2.key != 42) { return 1404;}

        // put too much in -> should still be 5
        for (int i = 0; i < 15; i++) {
            buf.pushCopyOf(e);
        }
        if (5 != buf.count()){ return 1406;}

        return 0;
    }
}
