package drivers.virtio.legacy.structs;

public class Virtqueue extends STRUCT{
    /* This marks a buffer as continuing via the next field. */
    public static final short VIRTQ_DESC_F_NEXT = 1;
    /* This marks a buffer as device writeConfigSpace-only (otherwise device read-only). */
    public static final short VIRTQ_DESC_F_WRITE = 2;
    /* This means the buffer contains a list of buffer descriptors. */
    public static final short VIRTQ_DESC_F_INDIRECT = 4;

    @SJC(count = VirtqueueConf.QUEUE_SIZE)
    DescriptorElement[] descriptors; // len 265*16 = 4065

    AvailableRing availableRing; // len  6 + 265*8 = 2048+6
    @SJC(count = 2042) //pad to next 4096 boundary = 2048-6
            byte pad[];

    UsedRing usedRing;


}


