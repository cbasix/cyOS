package datastructs.subtypes;

import datastructs.ArrayList;
import tasks.shell.commands.Command;

public class CommandArrayList extends ArrayList {
    public Command get(int i) {
        return (Command) super._get(i);
    }

    public void add(Command o) {
        super._add(o);
    }

}
