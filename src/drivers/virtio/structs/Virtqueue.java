package drivers.virtio.structs;

public class Virtqueue extends STRUCT{
    public static final int PADDING = 2042;
    public static final int SIZE = DescriptorElement.SIZE * VirtqueueConstants.QUEUE_SIZE
            + AvailableRing.SIZE
            + PADDING
            + UsedRing.SIZE;

    public static final int DESCRIPTOR_OFFSET = 0;
    public static final int AVAILABLE_RING_OFFSET = VirtqueueConstants.QUEUE_SIZE * DescriptorElement.SIZE;
    public static final int USED_RING_OFFSET = AVAILABLE_RING_OFFSET
            + AvailableRing.SIZE
            + PADDING;

    /* This marks a buffer as continuing via the next field. */
    public static final short VIRTQ_DESC_F_NEXT = 1;
    /* This marks a buffer as device writeConfigSpace-only (otherwise device read-only). */
    public static final short VIRTQ_DESC_F_WRITE = 2;
    /* This means the buffer contains a list of buffer descriptors. */
    public static final short VIRTQ_DESC_F_INDIRECT = 4;

    @SJC(count = VirtqueueConstants.QUEUE_SIZE)
    public DescriptorElement[] descriptors; // len 265*16 = 4065

    public AvailableRing availableRing; // len  6 + 265*8 = 2048+6
    @SJC(count = PADDING) //pad to next 4096 boundary = 2048-6
    byte pad[];

    public UsedRing usedRing;


}


