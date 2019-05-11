package drivers.virtio.structs;

public class Buffer extends STRUCT {
    public static final int SIZE = VirtqueueConstants.BUFFER_SIZE;

    @SJC(count=VirtqueueConstants.BUFFER_SIZE)
    public byte[] data;
}