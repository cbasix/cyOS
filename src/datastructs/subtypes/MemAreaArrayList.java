package datastructs.subtypes;

import datastructs.ArrayList;
import kernel.memory.MemArea;

public class MemAreaArrayList extends ArrayList {
    public MemArea get(int i) {
        return (MemArea) super._get(i);
    }
    public void add(MemArea o) {
        super._add(o);
    }
}
