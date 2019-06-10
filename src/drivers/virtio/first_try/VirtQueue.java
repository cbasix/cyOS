package drivers.virtio.first_try;

import drivers.virtio.RawMemoryContainer;
import io.LowlevelLogging;
import kernel.Kernel;

/* Implements virtqueues as specified by
   http://docs.oasis-open.org/virtio/virtio/v1.0/cs01/virtio-v1.0-cs01.pdf
*/
public class VirtQueue {

    /* This marks a buffer as continuing via the next field. */
    public static final short VIRTQ_DESC_F_NEXT = 1;
    /* This marks a buffer as device writeConfigSpace-only (otherwise device read-only). */
    public static final short VIRTQ_DESC_F_WRITE = 2;
    /* This means the buffer contains a list of buffer descriptors. */
    public static final short VIRTQ_DESC_F_INDIRECT = 4;

    public static class DescriptorTable extends STRUCT {
        @SJC(count = QUEUE_SIZE) // unchecked
        public DescriptorTableElement[] elements;
    }

    public static class Virtqueue {
        DescriptorTable descriptorTable;
        AvailableRing availableRing;
        UsedRing usedRing;
    }

    public static class DescriptorTableElement extends STRUCT {
        /* Address (guest-physical). */
        public long address;

        /* Length. */
        public int length;

        /* The flags as indicated above. */
        public short flags;

        /* Next field if flags & NEXT */
        public short next;
    }


    // todo are static final allowed in struct?
    public static final int VIRTQ_AVAIL_F_NO_INTERRUPT = 1;

    public static class AvailableRing extends STRUCT {
        /* Unused ?*/
        public short flags;

        /* The idx field indicates where the driver would put the next descriptor
           entry in the ring (modulo the queue size).This starts at 0, and increases. */
        public short idx;

        /* each ring entry refers to the head of adescriptor chain.
           It is only written by the driver and read by the device. */
        @SJC(count=QUEUE_SIZE) // unchecked array
        public short[] ring;

        /* reqest device to send no more interrupts
           Only available if VIRTIO_F_EVENT_IDX feature was negotiated
           See 2.4.7 in http://docs.oasis-open.org/virtio/virtio/v1.0/cs01/virtio-v1.0-cs01.pdf  */
        public short used_event;
    }


    /* Notification Suppression */
    public static final int VIRTQ_USED_F_NO_NOTIFY = 1;

    /* The used ring is where the device returns buffers once it is done with them:
    it is only written to by the device,and read by the driver. */

    public static class UsedRing extends STRUCT {

        public short flags;

        public short idx;

        /* unchecked array */
        @SJC(count = QUEUE_SIZE)
        public UsedRingElement[] ring;

        /* Allows the device to ask for Notification Suppression, if
           VIRTIO_F_EVENT_IDX was negotiated */
        public short avail_event;
    }

    public static class UsedRingElement extends STRUCT {
        /* id indicates the head entry of the descriptor chain describing the
           buffer (thismatches an entry placed in the available ring by the guest earlier) */
        /* le32(int) is used here for ids for padding reasons. */
        /* Index of start of used descriptor chain. */
        public int id;

        /* Total length of the descriptor chain which was used (written to) */
        /*  =  The total of bytes written into the buffer */
        public int len;
    }

    // must be a power of two
    public static final int QUEUE_SIZE = 128;
    public static final int BUFFER_SIZE = 1526; // see 5.1.6.3.1

    public static final int MAX_ALIGNMENT = 16 + 2 + 4; // see 2.4
    public static final int DESCRIPTOR_TABLE_ENTRY_SIZE = 16;

    RawMemoryContainer virtqueueRawContainer;
    RawMemoryContainer[] buffers;

    public final int descriptorTableAddr;
    public final int usedRingAddr;
    public final int availableRingAddr;

    public final AvailableRing availableRing;
    public final UsedRing usedRing;
    public final DescriptorTable descriptorTable;

    private final int notifierAddr;
    private final short queueIndex;

    public short lastSeenUsed = 0;
    public short nextBuffer = 0;

    // only two types of queues are supported for now: pure device -> driver or pure driver -> device (mixed rw queues are currently not implemented)
    public VirtQueue(short queueIndex,  int notifierAddr) {
        this.notifierAddr = notifierAddr;
        this.queueIndex = queueIndex;

        int descriptorTableSize = MAGIC.getInstScalarSize("DescriptorTableElement") * QUEUE_SIZE;
        int usedRingSize = 4 + 4 * QUEUE_SIZE;  // used ring
        int availableRingSize = 6 + 2 * QUEUE_SIZE;  // available ring

        virtqueueRawContainer = new RawMemoryContainer(descriptorTableSize + usedRingSize + availableRingSize + MAX_ALIGNMENT);

        descriptorTableAddr = allign(virtqueueRawContainer.getRawAddr(), 4) ; // allign to 16, see 2.4
        //LowlevelLogging.debug(String.concat("descr tab addr: ", String.hexFrom(descriptorTableAddr)));
        usedRingAddr = allign(descriptorTableAddr + descriptorTableSize, 1);
        availableRingAddr = allign(usedRingAddr + usedRingSize, 2);

        availableRing = (AvailableRing) MAGIC.cast2Struct(availableRingAddr);
        usedRing = (UsedRing) MAGIC.cast2Struct(usedRingAddr);
        descriptorTable = (DescriptorTable) MAGIC.cast2Struct(descriptorTableAddr);

        //todo new part 4.1.5.5 Driver Handling Interrupts

    }
    public int allign(int addr, int bits){
        int mask = 0;
        for (int i = 0; i < bits; i++){
            mask |= ( 1 << i);
        }

        return (addr + (mask-1)) & ~mask;
    }

    public void setupBuffers(){
        // 3.2.1 supplying buffers to the device
        // setup buffers
        buffers = new RawMemoryContainer[QUEUE_SIZE];
        for (int i = 0; i < buffers.length; i++) {
            buffers[i] = new RawMemoryContainer(BUFFER_SIZE);
        }

        // The driver places the buffer into free descriptor(s) in the descriptor tabl
        for (short i = 0; i < QUEUE_SIZE; i++) {
            DescriptorTableElement e = descriptorTable.elements[i];
            e.address = buffers[i].getRawAddr();
            e.length = buffers[i].data.length;
        }


    }

    public void populateAvailable(int count) {
        // The driver places the index of the head of the descriptor chain into the next ring entry of the availablering
        MAGIC.inline(0x0F,0xAE,0xF0); //mfence Memory Fence
        availableRing.idx += count;

        // check if notifications are enabled
        if((usedRing.flags & VIRTQ_USED_F_NO_NOTIFY)==0){
            // notify device see 4.1.4.4
            /*if (queueIndex == 1) {
                LowlevelLogging.printHexdump(notifierAddr);
                Kernel.wait(2);
            }*/
            MAGIC.wMem16(notifierAddr, queueIndex);
            /*if (queueIndex == 1) {
                LowlevelLogging.printHexdump(notifierAddr);
                Kernel.stop();
            }*/
        }
    }
}
