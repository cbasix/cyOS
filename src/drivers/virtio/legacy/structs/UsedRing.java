package drivers.virtio.legacy.structs;

/* The used ring is where the device returns buffers once it is done with them:
it is only written to by the device,and read by the driver. */

class UsedRing extends STRUCT {
    /* Notification Suppression */
    public static final int VIRTQ_USED_F_NO_NOTIFY = 1;

    public short flags;

    public short idx;

    /* unchecked array */
    @SJC(count = VirtqueueConf.QUEUE_SIZE)
    public UsedRingElement ring[];

    /* Allows the device to ask for Notification Suppression, if
       VIRTIO_F_EVENT_IDX was negotiated */
    public short avail_event;
}
