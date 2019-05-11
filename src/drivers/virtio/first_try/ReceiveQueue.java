package drivers.virtio.first_try;

public class ReceiveQueue extends VirtQueue {
    public ReceiveQueue(short queueIndex, int notifierAddr) {
        super(queueIndex, notifierAddr);


        setupBuffers();

        for (short i = 0; i < QUEUE_SIZE; i++){
            VirtQueue.DescriptorTableElement e = this.descriptorTable.elements[i];
            // allow device to write
            e.flags = VirtQueue.VIRTQ_DESC_F_WRITE;

            // add it to the available ring
            this.availableRing.ring[(int)i] = i;
        }

        // populate all the new avaiable entrys to the device
        this.populateAvailable(QUEUE_SIZE);

    }

    public byte[] receiveOne(){
        if (lastSeenUsed != usedRing.idx) {
            UsedRingElement usedRingElem = usedRing.ring[usedRing.idx % QUEUE_SIZE];
            DescriptorTableElement dte = descriptorTable.elements[usedRingElem.id];

            // todo buffers spanning over multiple descriptors

            byte[] data = new byte[usedRingElem.len];


            int buffAddr = (int) dte.address;

            for (int i = 0; i < usedRingElem.len; i++){
                data[i] = MAGIC.rMem8(buffAddr + i);

                // re initalize buffer with zero
                MAGIC.wMem8(buffAddr + i, (byte) 0);
            }

            return data;
        }

        return null;
    }
}
