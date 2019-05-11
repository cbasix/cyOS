package drivers.virtio.first_try;

import io.LowlevelLogging;

public class TransmitQueue extends VirtQueue {
    public static final int NET_HEADER_SIZE = 12;

    public static final byte VIRTIO_NET_HDR_F_NEEDS_CSUM = 1;
    public static final byte VIRTIO_NET_HDR_GSO_NONE = 0;
    public static final byte VIRTIO_NET_HDR_GSO_TCPV4 = 1;
    public static final byte VIRTIO_NET_HDR_GSO_UDP = 3;
    public static final byte VIRTIO_NET_HDR_GSO_TCPV6 = 4;
    public static final byte VIRTIO_NET_HDR_GSO_ECN = (byte)0x80;

    public static class NetHeader extends STRUCT {
        public byte flags;
        public byte gso_type;

        public short hdr_len;
        public short gso_size;
        public short csum_start;
        public short csum_off;
        public short num_buffers;
    }

    public TransmitQueue(short queueIndex, int notifierAddr) {
        super(queueIndex, notifierAddr);

        setupBuffers();
        // ask for disabled intterrupts // not a dependable operation
        availableRing.flags = (short)VIRTQ_AVAIL_F_NO_INTERRUPT;
    }

    /** puts data into the transmit queue */
    public void transmit(byte[] data){
        if (((data.length + NET_HEADER_SIZE) / BUFFER_SIZE) != 0){
            LowlevelLogging.debug("only one buffer messages are supported for now");
            // todo implement chaining
            return;
        }

        // get the next buffer to use
        DescriptorTableElement dte = descriptorTable.elements[nextBuffer % QUEUE_SIZE];
        // package is preceeded by the header defined above so its longer ;)
        dte.length = data.length + NET_HEADER_SIZE;

        int buffAddr = (int) dte.address;

        // see 5.1.6.2
        NetHeader header = (NetHeader) MAGIC.cast2Struct(buffAddr);
        header.flags = 0;
        header.gso_type = VIRTIO_NET_HDR_GSO_NONE;
        header.num_buffers = 0;

        for (int i = 0; i < data.length; i++){
            MAGIC.wMem8(buffAddr + NET_HEADER_SIZE + i, data[i]);
        }

        // put into available queue
        availableRing.ring[availableRing.idx % QUEUE_SIZE] = (short)(nextBuffer % QUEUE_SIZE);
        populateAvailable(1);

        nextBuffer++;
        // todo jump over in use ones ...

        recycleUsed();
    }
    /** retrive the used buffers from the used ring and set them as unused */
    public void recycleUsed(){
        // todo implement
        /*LowlevelLogging.debug(String.concat("headIndex:     ", String.hexFrom(headIndex)));
        LowlevelLogging.debug(String.concat("e addr:        ", String.hexFrom((int)e.address)));

        LowlevelLogging.printHexdump(availableRingAddr);
        Kernel.stop();*/
        if (usedRing.idx != lastSeenUsed){
            LowlevelLogging.debug("WE DID SEND A PACKET. HOPEFULLY...");
        }
    }
}
