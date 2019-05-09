package tests.highlevel;

import datastructs.ArrayList;
import drivers.virtio.RawMemoryContainer;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class DivesesAndPlaygroundTest {
    static class T extends STRUCT{
        @SJC(count = 0)
        byte[] data;
        byte wtf;
    }

    public static int test(){


        // memory container
        RawMemoryContainer r = new RawMemoryContainer(8);
        r.data[0] = 0x22;
        r.data[1] = 0x66;

        byte data = MAGIC.rMem8(r.getRawAddr());
        if(0x22 != data){
            LowlevelOutput.printHex(data, 2, 16, 16, Color.RED);
            LowlevelLogging.printHexdump(r.getRawAddr()-0x10);
            return 1801;
        }
        if(0x66 != MAGIC.rMem8(r.getRawAddr()+1)){return 1802;}


        // unchecked arrays
        RawMemoryContainer mem = new RawMemoryContainer(50);
        T test = (T) MAGIC.cast2Struct(mem.getRawAddr());
        for (int i = 0; i < 45; i++) {
            test.data[i] = 0x25;
        }
        // unchecked array overwrites following vars...  shouldn't this be an error???
        if (test.wtf != 0x25){return 1808;}


        return 0;
    }
}
