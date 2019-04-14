package datastructs.subtypes;

import datastructs.ArrayList;
import tasks.Task;

public class TaskArrayList extends ArrayList {
    public Task get(int i) {
        return (Task) super._get(i);
    }

    public void add(Task o) {
        super._add(o);
    }

}
