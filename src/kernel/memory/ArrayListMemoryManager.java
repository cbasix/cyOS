package kernel.memory;

import datastructs.subtypes.MemAreaArrayList;
import rte.SClassDesc;

public class ArrayListMemoryManager extends MemoryManager {
    private final MemAreaArrayList areas;

    public ArrayListMemoryManager(){
        areas = SystemMemoryMap.getAvailableGtOneMb();
        // BasicMemoryManager.getNextFreeAddr();
        int basicFirstFree = BasicMemoryManager.getFirstFreeAddr();
        int basicNextFree = BasicMemoryManager.getNextFreeAddr();

        for(int i = 1; i < areas.size(); i++){
            MemArea a = areas.get(i);

            // Basic manager has allready used this area so carve that part out of the "free" area
            if(a.start <= basicFirstFree && basicFirstFree <= a.start + a.size){
                a.start = basicNextFree;
            }
        }
    }

    @Override
    public Object allocate(int scalarSize, int relocEntries, SClassDesc type) {
        return null;
    }

    @Override
    public void deallocate(Object o) {

    }
}
