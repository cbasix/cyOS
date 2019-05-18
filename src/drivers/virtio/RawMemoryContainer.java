package drivers.virtio;

import rte.SArray;

public class RawMemoryContainer {
    public byte[] data;

    public RawMemoryContainer(int size){
        data = new byte[size];
    }

    public int getRawAddr(){
        // todo check
        //return MAGIC.cast2Ref(data) + MAGIC.getInstScalarSize("SArray") ;
        return MAGIC.addr(data[0]);
    }
}
