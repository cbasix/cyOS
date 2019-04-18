package datastructs.subtypes;

import datastructs.ArrayList;
import kernel.interrupts.core.InterruptHub;
import tasks.Task;

public class ObserverBindingArrayList extends ArrayList {
    public InterruptHub.ObserverBinding get(int i) {
        return (InterruptHub.ObserverBinding) super._get(i);
    }

    public void add(InterruptHub.ObserverBinding o) {
        super._add(o);
    }

}
