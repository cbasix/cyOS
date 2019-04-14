package kernel.memory;

import datastructs.subtypes.MemAreaArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import rte.BIOS;


public class SystemMemoryMap {
    public static final int AVAILABLE_TO_OS = 0x01;
    public static final int GET_SYSTEM_MEMORY_MAP = 0x0000E820;
    public static final int SMAP = 0x534D4150;

    public static class Smap extends STRUCT {
        long base, len;
        int type;
    }


    public static MemAreaArrayList getAvailableGtOneMb(){
        MemAreaArrayList memAreas = new MemAreaArrayList();

        // reserve some space on stack for bios answers
        int bytes16 =0;
        int bytes12 = 0;
        int bytes8 = 0;
        int bytes4 = 0;
        int bytes0 = 0;


        Smap s = (Smap) MAGIC.cast2Struct(MAGIC.addr(bytes0));

        int addr = MAGIC.addr(bytes0);
        int segment =addr / 16;
        int rm_addr_offset = addr % segment;

        int next_offset = 0;
        do {

            rte.BIOS.regs.EAX = GET_SYSTEM_MEMORY_MAP; //
            rte.BIOS.regs.EBX = next_offset;
            rte.BIOS.regs.ECX = 20;  // size of buffer
            rte.BIOS.regs.EDX = SMAP;
            rte.BIOS.regs.ES = (short) segment;
            rte.BIOS.regs.EDI = rm_addr_offset;
            rte.BIOS.rint(0x15);

            if (rte.BIOS.regs.EAX != SMAP){
                LowlevelLogging.debug("Get memory map: Error");
                while (true){}
            }

            if (s.type == AVAILABLE_TO_OS && s.base >= 0x00100000) {
                memAreas.add(new MemArea((int) s.base, (int) s.len));
            }



            next_offset = BIOS.regs.EBX;
        } while (next_offset != 0);

        return memAreas;
    }
}
