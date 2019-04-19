package kernel.memory;

public abstract class GarbageCollector {
    public abstract void run(MemoryManager mgr);
}
