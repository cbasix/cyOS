package kernel.memory;

public class Paging {

    public static final int pageTableEntryCount = 1024;

    static void writePageTable(int pageTableBase, int tableNo){
        for (int i = 0; i < pageTableEntryCount; i++) {
            int entry = 0;
            entry |= (i + pageTableEntryCount * tableNo) << 12; // page base address
            entry |= 0 << 7; // page size
            entry |= 0 << 4; // cache disable (0) -> cache on
            entry |= 0 << 3; // write trough (0) -> write back
            entry |= 0 << 2; // user/system (0) -> system
            entry |= 1 << 1; // read/r+w (1) -> r+w

            // let mmu create exceptions on nullpointer (first or last page access
            if((i == 0 && tableNo == 0) || (i == pageTableEntryCount -1 && tableNo == pageDirEntryCount -1)){
                entry |= 0 << 0; // present (0) -> seite nicht da
            } else {
                entry |= 1 << 0; // present (1) -> seite vorhanden
            }

            MAGIC.wMem32(pageTableBase+i*4, entry);
        }
    }



    public static final int pageDirEntryCount = 1024;

    public static void writePageDirectory(int pageDirectoryBase){
        for (int pTableNo = 0; pTableNo < pageDirEntryCount; pTableNo++) {
            int entry = 0;
            int pageTableAddr = ((1+pTableNo) << 12 ) + pageDirectoryBase; // (make space for myself) the page tables follow in ascencing order
            entry |= pageTableAddr ; // page table base address
            entry |= 0 << 7; // page size
            entry |= 0 << 4; // cache disable (0) -> cache on
            entry |= 0 << 3; // write trough (0) -> write back
            entry |= 0 << 2; // user/system (0) -> system
            entry |= 1 << 1; // read/r+w (1) -> r+w
            entry |= 1 << 0; // present (1) -> seite vorhanden

            MAGIC.wMem32(pageDirectoryBase+pTableNo*4, entry);

            writePageTable(pageTableAddr, pTableNo);
        }
    }

    public static void enable(){
        int pageDirAddr = 1024*1024*10; // at 20mb in nirvana just for testing
        // todo find good free place. Is it necessary? or is it copied?  -> not necessary processor keeps cached copy, only if i wanted to change it after the memory has been used i would have to find someplace else
        writePageDirectory(pageDirAddr);
        setCR3(pageDirAddr);
        enableVirtualMemory();

    }

    public static void disable(){
        // todo needed?
    }

    @SJC.Inline
    public static void setCR3(int addr) {
        MAGIC.inline(0x8B, 0x45); MAGIC.inlineOffset(1, addr); //mov eax,[ebp+8]
        MAGIC.inline(0x0F, 0x22, 0xD8); //mov cr3,eax
    }

    @SJC.Inline
    public static void enableVirtualMemory() {
        MAGIC.inline(0x0F, 0x20, 0xC0); //mov eax,cr0
        MAGIC.inline(0x0D, 0x00, 0x00, 0x01, 0x80); //or eax,0x80010000
        MAGIC.inline(0x0F, 0x22, 0xC0); //mov cr0,eax
    }

    @SJC.Inline
    public static int getCR2() {
        int cr2=0;
        MAGIC.inline(0x0F, 0x20, 0xD0); //mov e/rax,cr2
        MAGIC.inline(0x89, 0x45); MAGIC.inlineOffset(1, cr2); //mov [ebp-4],eax
        return cr2;
    }
}
