package kernel.memory;

import datastructs.subtypes.MemAreaArrayList;
import io.LowlevelLogging;
import kernel.interrupts.core.Interrupts;
import rte.SClassDesc;

public class ArrayListMemoryManager extends MemoryManager {
    private final MemAreaArrayList areas;

    public ArrayListMemoryManager(){
        areas = SystemMemoryMap.getAvailableGtOneMb();
        // BasicMemoryManager.getNextFreeAddr();
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
        // todo not implemented yet
    }
}
