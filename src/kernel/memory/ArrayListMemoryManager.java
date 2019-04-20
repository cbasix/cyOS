package kernel.memory;

import datastructs.subtypes.MemAreaArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.interrupts.core.Interrupts;
import rte.SClassDesc;

public class ArrayListMemoryManager extends MemoryManager {
    private final MemAreaArrayList areas;
    private GarbageCollector gc;

    public MemAreaArrayList getAreas(){
        return areas;
    }

    public ArrayListMemoryManager(){
        areas = SystemMemoryMap.getAvailableGtOneMb();
        gc = new MarkAndSweepGarbageCollector();

        // ---------------------- no new after this line ------------------------
        int basicFirstFree = BasicMemoryManager.getFirstFreeAddr();
        int basicNextFree = BasicMemoryManager.getNextFreeAddr();

        for(int i = 0; i < areas.size(); i++){
            MemArea a = areas.get(i);

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
        for(int i = 0; i < areas.size(); i++){
            MemArea a = areas.get(i);

            // uses first fitting,  maybe use best fitting later on
            if(a.size >= objSize){
                a.size -= objSize;
                newObj = createObject(scalarSize, relocEntries, type, a.start+a.size, objSize);

                if (a.size == 0){
                    areas.remove(a);
                }
                break;
            }
        }

        Interrupts.enable();
        return newObj;
    }


    @Override
    public void deallocate(Object o) {
        Interrupts.disable();
        // clear memory. not necessary but make sure the object can no longer be used
        int start = MAGIC.cast2Ref(o) - o._r_relocEntries * MAGIC.ptrSize;
        int size = getAllignedSize(o._r_scalarSize, o._r_relocEntries);

        for (int i = 0; i < size/4; i++) {
            MAGIC.wMem32(start + i*4, 0);
        }

        insertArea(start, size, areas);
        Interrupts.enable();
    }

    public static void insertArea(int start, int size, MemAreaArrayList list){
        Interrupts.disable();
        MemArea expandedOther = null;
        MemArea deleteSlot1 = null;
        MemArea deleteSlot2 = null;
        // backwards to allow deletion
        for (int i = 0; i < list.size() ; i++){
            MemArea other = list.get(i); // todo out ouf bounds here

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
                deleteSlot1 = other;

            }

            if (other.start + other.size == start && expandedOther != null){
                // touching free space on bottom -> and already joined top -> join both of them together
                expandedOther.start -= other.size;
                expandedOther.size += other.size;
                deleteSlot2 = other;


            }
        }

        if (deleteSlot1 != null){
            list.remove(deleteSlot1);
        }

        if (deleteSlot2 != null){
            list.remove(deleteSlot2);
        }

        if (expandedOther == null){
            list.add(new MemArea(start, size));
        }
        Interrupts.enable();
    }

    @Override
    public void gc() {
        gc.run(this);
    }
}
