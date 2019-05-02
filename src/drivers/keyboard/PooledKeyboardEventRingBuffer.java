package drivers.keyboard;

import datastructs.RingBuffer;
import io.LowlevelLogging;

public class PooledKeyboardEventRingBuffer {
    private RingBuffer buffer;
    private KeyboardEvent[] keyboardEvents;

    public PooledKeyboardEventRingBuffer(int size){

        buffer = new RingBuffer(size);
        Object[] bufferData = buffer.getRawArray();
        keyboardEvents = new KeyboardEvent[bufferData.length];

        for (int i = 0; i < bufferData.length;  i++){
            keyboardEvents[i] = new KeyboardEvent();

            // prefill buffer with empty event objects
            bufferData[i] = keyboardEvents[i];
        }
    }

    /* copy into buffer */
    public void pushCopyOf(KeyboardEvent event){
        // todo prettify generate object.copy helper maybe...
        // use the object which is already there
        KeyboardEvent e = (KeyboardEvent) buffer.getRawArray()[buffer.getNextPushIndex()];
        e.modifiers = event.modifiers;
        e.key = event.key;
        e.pressed = event.pressed;

        // yes it is allready there, but we need to set the ring buffers start and end counts correctly.
        buffer.push(e);
    }

    public boolean getCopyInto(KeyboardEvent e){
        KeyboardEvent event = (KeyboardEvent) buffer.get();
        if (event != null) {
            e.modifiers = event.modifiers;
            e.key = event.key;
            e.pressed = event.pressed;

            return true;
        }

        return false;
    }

    public int count (){
        return buffer.count();
    }
}
