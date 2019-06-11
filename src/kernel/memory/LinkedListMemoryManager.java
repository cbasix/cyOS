package kernel.memory;

import datastructs.subtypes.MemAreaLinkedList;
import io.LowlevelLogging;
import kernel.interrupts.core.Interrupts;
import rte.SClassDesc;

public class LinkedListMemoryManager extends MemoryManager {
    private final MemAreaLinkedList areas;
    private final MemAreaLinkedList.MemAreaIterator areaIter;
    public GarbageCollector gc;

    public MemAreaLinkedList getAreas(){
        return areas;
    }


    public LinkedListMemoryManager(){
        areas = SystemMemoryMap.getAvailableGtOneMb();
        gc = new MarkAndSweepGarbageCollector();
        areaIter = areas.iter();

        // ---------------------- no new after this line ------------------------
        int basicFirstFree = BasicMemoryManager.getFirstFreeAddr();
        int basicNextFree = BasicMemoryManager.getNextFreeAddr();

        while(areaIter.next()){
            MemArea a = areaIter.get();

            // Basic manager has already used this area so carve that part out of the "free" area
            if(a.start <= basicFirstFree && basicFirstFree <= a.start + a.size){
                a.size -= basicNextFree - a.start;
                a.start = basicNextFree;

            }
        }
    }

    @Override
    public Object allocate(int scalarSize, int relocEntries, SClassDesc type) {
        Interrupts.disable();

        Object newObj = null;

        int objSize = getAllignedSize(scalarSize, relocEntries);

        areaIter.gotoStart();
        while(areaIter.next()){
            MemArea a = areaIter.get();

            // uses first fitting,  maybe use best fitting later on
            if(a.size >= objSize){
                a.size -= objSize;
                newObj = createObject(scalarSize, relocEntries, type, a.start+a.size, objSize);

                if (a.size == 0){
                    areaIter.removeCurrent();
                }
                break;
            }
        }

        Interrupts.enable();
        if(newObj == null){
            memoryFull();
        }

        return newObj;
    }

    public void memoryFull(){
        MAGIC.inline(0xCC); // breakpoint exception
    }


    @Override
    public void deallocate(Object o) {
        Interrupts.disable();
        // clear memory. not necessary but make sure the object can no longer be used
        int start = MAGIC.cast2Ref(o) - o._r_relocEntries * MAGIC.ptrSize;
        int size = getAllignedSize(o._r_scalarSize, o._r_relocEntries);

        /*for (int i = 0; i < size/4; i++) {
            MAGIC.wMem32(start + i*4, 0);
        }*/

        insertArea(start, size, areaIter);
        Interrupts.enable();
    }

    @SJC.Inline
    public static void insertArea(int start, int size, MemAreaLinkedList.MemAreaIterator iter){
        MemArea expandedOther = null;

        iter.gotoStart();
        while(iter.next()){
            MemArea other = iter.get();

            if (other.start == start + size && expandedOther == null){
                // touching free space on top -> just expand it
                other.size += size;
                other.start = start;
                expandedOther = other;
            }

            if (other.start + other.size == start && expandedOther == null){
                // touching free space on bottom -> expand it
                other.size += size;
                expandedOther = other;
            }

            if (other.start == start + size && expandedOther != null){
                // touching free space on top -> and already joined bottom -> join both of them together
                expandedOther.size += other.size;
                iter.removeCurrent();
            }

            if (other.start + other.size == start && expandedOther != null){
                // touching free space on bottom -> and already joined top -> join both of them together
                expandedOther.start -= other.size;
                expandedOther.size += other.size;
                iter.removeCurrent();
            }
        }

        if (expandedOther == null){
            iter.insert(new MemArea(start, size));
        }
    }

    @Override
    public void gc() {
        Interrupts.disable();
        gc.run(this);
        Interrupts.enable();
    }
}
