package drivers.virtio.structs;

public class AvailableRing extends STRUCT {
    public static final int SIZE = 2 + 2 + 2*VirtqueueConstants.QUEUE_SIZE + 2;

    public static final int VIRTQ_AVAIL_F_NO_INTERRUPT = 1;

    /* Unused ?*/
    public short flags;
    /* The idx field indicates where the driver would put the next descriptor
       entry in the ring (modulo the queue size).This starts at 0, and increases. */
    public short idx;
    /* each ring entry refers to the head of adescriptor chain.
       It is only written by the driver and read by the device. */
    @SJC(count= VirtqueueConstants.QUEUE_SIZE) // unchecked array
    public short[] ring;
    /* reqest device to send no more interrupts
       Only available if VIRTIO_F_EVENT_IDX feature was negotiated
       See 2.4.7 in http://docs.oasis-open.org/virtio/virtio/v1.0/cs01/virtio-v1.0-cs01.pdf  */
    public short used_event;
}
