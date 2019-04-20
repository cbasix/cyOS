package datastructs.subtypes;

import datastructs.ArrayList;
import datastructs.LinkedListIter;
import kernel.memory.MemArea;

public class MemAreaLinkedListIter extends LinkedListIter {
    public MemArea get(int i) {
        return (MemArea) super._get();
    }
    public void add(MemArea o) {
        super._insert(o);
    }
    public void add(MemArea o, boolean before) {
        super._insert(o, before);
    }
}
