package drivers.virtio.structs;

import drivers.virtio.structs.VirtqueueConstants;

public class BufferArea extends STRUCT {
    public static final int SIZE = Buffer.SIZE * VirtqueueConstants.QUEUE_SIZE;

    @SJC(count= VirtqueueConstants.QUEUE_SIZE)
    public Buffer[] ring;
}
