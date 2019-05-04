package drivers.virtio;

/* Implements virtqueues as specified by
   http://docs.oasis-open.org/virtio/virtio/v1.0/cs01/virtio-v1.0-cs01.pdf
*/
public class VirtQueue {

    /* This marks a buffer as continuing via the next field. */
    public static final int  VIRTQ_DESC_F_NEXT = 1;
    /* This marks a buffer as device writeConfigSpace-only (otherwise device read-only). */
    public static final int VIRTQ_DESC_F_WRITE = 2;
    /* This means the buffer contains a list of buffer descriptors. */
    public static final int VIRTQ_DESC_F_INDIRECT = 4;

    static class DescriptorTableElement extends STRUCT{
        /* Address (guest-physical). */
        long address;

        /* Length. */
        int length;

        /* The flags as indicated above. */
        short flags;

        /* Next field if flags & NEXT */
        short next;
    }


    // todo are static final allowed in struct?
    public static final int VIRTQ_AVAIL_F_NO_INTERRUPT = 1;

    static class AvailableRing extends STRUCT{
        /* Unused ?*/
        short flags;

        /* The idx field indicates where the driver would put the next descriptor
           entry in the ring (modulo the queue size).This starts at 0, and increases. */
        short idx;

        /* each ring entry refers to the head of adescriptor chain.
           It is only written by the driver and read by the device. */
        int ringArrayAddr;

        /* reqest device to send no more interrupts
           Only available if VIRTIO_F_EVENT_IDX feature was negotiated
           See 2.4.7 in http://docs.oasis-open.org/virtio/virtio/v1.0/cs01/virtio-v1.0-cs01.pdf  */
        short used_event;
    }


    /* Notification Suppression */
    public static final int VIRTQ_USED_F_NO_NOTIFY = 1;

    /* The used ring is where the device returns buffers once it is done with them:
    it is only written to by the device,and read by the driver. */

    static class UsedRing extends STRUCT {

        short flags;

        short idx;

        /* contains the addres to a array of UsedRingElements*/
        int ringElemArrayAddr;

        /* Allows the device to ask for Notification Suppression, if
           VIRTIO_F_EVENT_IDX was negotiated */
        short avail_event;
    }

    static class UsedRingElement extends STRUCT {
        /* id indicates the head entry of the descriptor chain describing the
           buffer (thismatches an entry placed in the available ring by the guest earlier) */
        /* le32(int) is used here for ids for padding reasons. */
        /* Index of start of used descriptor chain. */
        int id;

        /* Total length of the descriptor chain which was used (written to) */
        /*  =  The total of bytes written into the buffer */
        int len;
    }
}
