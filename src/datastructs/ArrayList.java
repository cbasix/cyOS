package datastructs;

import io.LowlevelLogging;
import io.LowlevelOutput;

public class ArrayList {
    private static final int GROWTH = 10;
    private Object[] array = new Object[GROWTH];
    private int count = 0;

    public void _add(Object o){
        if (count >= array.length){
            grow();
        }
        array[count] = o;
        count++;
    }

    private void grow(){
        Object[] newArray = new Object[array.length + GROWTH];

        for (int i = 0; i < array.length; i++){
            newArray[i] = array[i];
        }
        array = newArray;
    }

    public boolean remove(Object o){
        boolean found = false;
        for (int i = 0; i < array.length; i++){
            if (array[i] == o){
                found = true;
            }
            if (found && i < array.length - 1){
                array[i] = array[i+1];
            }
        }
        if (found){
            count--;
        }
        return found;
    }

    public Object _get(int i){
        if (i < count) {
            return array[i];
        } else {
            return null;
        }
    }

    public int size(){
        return count;
    }
}
