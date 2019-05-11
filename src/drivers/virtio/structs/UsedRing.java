package drivers.virtio.structs;

/* The used ring is where the device returns buffers once it is done with them:
it is only written to by the device,and read by the driver. */

public class UsedRing extends STRUCT {
    public static final int SIZE = 2 + 2 + UsedRingElement.SIZE * VirtqueueConstants.QUEUE_SIZE + 2;

    /* Notification Suppression */
    public static final int VIRTQ_USED_F_NO_NOTIFY = 1;

    public short flags;

    public short idx;

    /* unchecked array */
    @SJC(count = VirtqueueConstants.QUEUE_SIZE)
    public UsedRingElement[] ring;

    /* Allows the device to ask for Notification Suppression, if
       VIRTIO_F_EVENT_IDX was negotiated */
    public short avail_event;
}
