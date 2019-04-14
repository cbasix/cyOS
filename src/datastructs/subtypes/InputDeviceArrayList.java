package datastructs.subtypes;

import drivers.InputDevice;
import datastructs.ArrayList;

public class InputDeviceArrayList extends ArrayList {
    public InputDevice get(int i) {
        return (InputDevice) super._get(i);
    }
    public void add(InputDevice o) {
        super._add(o);
    }
}
