package kernel.memory;

import kernel.Kernel;

public class MarkAndSweepGarbageCollector extends GarbageCollector{
    @Override
    public void run(MemoryManager mgr) {
        Object o = mgr.getFirstObject();

        // mark
        do {
            o._s_inUse = false;
            o = o._r_next;
        } while(o != null);


    }
}
