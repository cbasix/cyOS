package tests.lowlevel;

import datastructs.ArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.memory.Paging;

public class PagingTest {
    public static int test(){


        int pageDirAddr = 1024*1024*5; // at 10mb in nirvana  todo just for testing
        //LowlevelLogging.debug("before writeConfigSpace");
        Paging.writePageDirectory(pageDirAddr);
        //LowlevelLogging.debug("written");


        // check page directory entry adresses point to page table adresses
        for (int i = 1; i <= Paging.pageDirEntryCount; i++) {

            int dirEntry = MAGIC.rMem32(pageDirAddr+4*(i-1));
            int shouldEntry = pageDirAddr + 4096*i;

            if ((dirEntry & ~0xFFF) != shouldEntry) {
                LowlevelOutput.printHex(i, 10, 45, 10, Color.GREY);
                LowlevelOutput.printHex(shouldEntry, 10, 45, 12, Color.GREEN);
                LowlevelOutput.printHex((dirEntry & ~0xFFF), 10, 45, 13, Color.RED);
                return 1101;
            }
        }

        // check first table all entrys
        for (int i = 0; i < Paging.pageTableEntryCount; i++) {

            int dirEntry = MAGIC.rMem32(pageDirAddr+4096+4*i);
            int shouldEntry = 4096*i;

            if ((dirEntry & ~0xFFF) != shouldEntry) {
                LowlevelOutput.printHex(i, 10, 45, 10, Color.GREY);
                LowlevelOutput.printHex(shouldEntry, 10, 45, 12, Color.GREEN);
                LowlevelOutput.printHex((dirEntry & ~0xFFF), 10, 45, 13, Color.RED);
                return 1104;
            }
        }


        return 0;
    }
}
