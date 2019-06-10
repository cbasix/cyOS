package tests.highlevel;

import arithmetics.ByteArray;
import arithmetics.Unsigned;
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


        // Unsigned
        if (!Unsigned.isLessThan(0, 0xF0000000)) {return 1809;}
        if (!Unsigned.isGreaterThan(0xFF000000, 0)) {return 1810;}


        //byte array count
        byte[] cntData = new byte[2];
        cntData[0] = (byte) 0xff;
        cntData[1] = (byte) 0xf0;

        if (ByteArray.countStartOnes(cntData) != 12){
            LowlevelOutput.printStr(String.from(ByteArray.countStartOnes(cntData)), 2, 2, Color.RED);
            LowlevelOutput.printStr(String.from(12), 2, 3, Color.GREEN);
            return 1815;
        }

        cntData[0] = (byte) 0xff;
        cntData[1] = (byte) 0xff;
        if (ByteArray.countStartOnes(cntData) != 16){return 1817;}

        cntData[0] = (byte) 0xf0;
        cntData[1] = (byte) 0xff;
        if (ByteArray.countStartOnes(cntData) != 4){return 1818;}

        if(!ByteArray.equals(ByteArray.and(cntData, cntData), cntData)){
            LowlevelOutput.printStr(String.hexFrom(ByteArray.and(cntData, cntData)[0]), 2, 2, Color.RED);
            LowlevelOutput.printStr(String.hexFrom(ByteArray.and(cntData, cntData)[1]), 5, 2, Color.RED);
            LowlevelOutput.printStr(String.hexFrom(cntData[0]), 2, 3, Color.GREEN);
            LowlevelOutput.printStr(String.hexFrom(cntData[1]), 5, 3, Color.GREEN);
            return 1819;
        }

        if(!ByteArray.equals(ByteArray.or(cntData, cntData), cntData)){return 1819;}



        return 0;
    }
}
