package drivers.virtio.net;

import arithmetics.Unsigned;
import drivers.pci.PciBaseAddr;
import drivers.pci.PciDevice;
import drivers.virtio.RawMemoryContainer;
import drivers.virtio.VirtIo;
import drivers.virtio.structs.*;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;

import drivers.virtio.structs.VirtqueueConstants;
import kernel.interrupts.core.Interrupts;
import network.MacAddress;
import network.Nic;


public class VirtioNic extends Nic{

    public static final int VIRTIO_F_VERSION_1 = 32;
    public static final int VIRTIO_F_RING_EVENT_IDX = 29;
    public static final int VIRTIO_NET_F_MAC = 5;
    public static final int VIRTIO_NET_F_STATUS = 16;

    public static final int INTERRUPT_LINE = 11; // todo verify
    public static final int INTERRUPT_NO = Interrupts.IRQ11; // todo verify

    public static final int QUEUE_COUNT = 2;

    public static final int RECEIVE_QUEUE = 0;
    public static final int TRANSMIT_QUEUE = 1;

    public static final int ALIGN_SPACE = 15 * 2 + 3;


    private RawMemoryContainer rawMem;


    private Virtqueue transmitQueue;
    private Virtqueue receiveQueue;
    private BufferArea bufferArea;
    //private boolean[] inUse; todo implement conditional buffer reuse

    // todo wraparound with negative indices will most likely be a problem
    private short transmitNextToUseIdx = 0;
    private short receiveNextToUseIdx = 0;
    //private short nextBuffer = 0;

    private CommonConfig commonConfig;
    private NotifyConfig notifyConfig;

    private VirtioInterruptAdapter interruptAdapter;
    private PciDevice pciDevice;
    private IsrReg isrReg;
    private NetConfig nicConfReg;


    class VirtioInterruptAdapter extends InterruptReceiver {
        public static final int CONFIG_INT = 1;
        public static final int QUEUE_INT = 2;

        public int interruptCnt = 0;
        @Override
        public boolean handleInterrupt(int interruptNo, int param) {
            // this read resets the interrupt!
            if((isrReg.data & 0x3) != 0){
                LowlevelOutput.printStr("GOT VIRTIO INTERRUPT", 0, 0, Color.PINK);
                LowlevelOutput.printStr(String.from(++interruptCnt), 22, 0, Color.CYAN);
                return true;
            }
            return false;

            // todo
            //  – If the lower bit is set: look through the used rings of all
            //    virtqueues for the device, to see if anyprogress has been made
            //    by the device which requires servicing.
            //  – If the second lower bit is set: re-examine the configuration
            //    space to see what changed. Necessary???
        }
    }

    public VirtioNic(PciDevice pciDevice) {
        this.pciDevice = pciDevice;
        this.pciDevice.setInterruptLine(INTERRUPT_LINE);

        // check interrupt line
        if(this.pciDevice.getInterruptLine() != INTERRUPT_LINE) {
            LowlevelLogging.debug("INTERRUPT Line could not be set");
            LowlevelLogging.debug(String.hexFrom(this.pciDevice.getInterruptLine()));
            Kernel.wait(5);
        }

        parsePciCapabilities();

        interruptAdapter = new VirtioInterruptAdapter();
        Kernel.interruptHub.addObserver(interruptAdapter, INTERRUPT_NO);

        rawMem = new RawMemoryContainer(
                2*Virtqueue.SIZE
                        + BufferArea.SIZE
                        + ALIGN_SPACE
        );

        int transmitQueueAddr = (rawMem.getRawAddr() + 15) & ~15;
        int receiveQueueAddr = (transmitQueueAddr + Virtqueue.SIZE + 15) & ~15;
        int bufferAreaAddr = (receiveQueueAddr + Virtqueue.SIZE + 3) & ~3;

        int tqa = transmitQueueAddr + Virtqueue.USED_RING_OFFSET;
        int tqa2 = transmitQueueAddr + Virtqueue.AVAILABLE_RING_OFFSET;

        // check allignment
        if ((tqa & ~3) != tqa || (tqa2 & ~1) != tqa2){
            LowlevelLogging.debug( "Used ring allignment wrong ");
            LowlevelLogging.debug(String.concat(
                    String.hexFrom(tqa),
                    String.hexFrom(tqa & ~3)));
            Kernel.wait(3);
        }

        transmitQueue = (Virtqueue) MAGIC.cast2Struct(transmitQueueAddr);
        receiveQueue = (Virtqueue) MAGIC.cast2Struct(receiveQueueAddr);
        bufferArea = (BufferArea) MAGIC.cast2Struct(bufferAreaAddr);

        if (transmitQueueAddr != MAGIC.cast2Ref(transmitQueue)) {
            LowlevelLogging.debug("cast2ref doesnt work with struct");
        }
        initDevice();
    }


    private void parsePciCapabilities() {
        int currentPtr = pciDevice.capabilitiesPointer;

        boolean cfgFound = false;
        boolean notifyFound = false;
        boolean isrFound = false;
        boolean nicConfFound = false;
        do {
            int tmp = pciDevice.readConfigSpace(currentPtr);
            char cap_vndr = (char)(tmp & 0xFF);            /* Generic PCI field: PCI_CAP_ID_VNDR */
            char cap_next = (char)((tmp >> 8) & 0xFF);     /* Generic PCI field: next ptr. */
            char cap_len = (char)((tmp >> 16) & 0xFF);     /* Generic PCI field: capability length */
            char cfg_type = (char)((tmp >> 24) & 0xFF);    /* Identifies the structure. */

            tmp = pciDevice.readConfigSpace(currentPtr+1);
            char bar = (char)(tmp & 0xFF);

            // get CONF BASE; 0x9 -> vendor specific capability
            if (!cfgFound && cfg_type == VirtIo.VIRTIO_PCI_CAP_COMMON_CFG && cap_vndr == 0x09){

                /* Where to find it. */
                int offset = pciDevice.readConfigSpace(currentPtr+2);      /* Offset within bar. */
                int length = pciDevice.readConfigSpace(currentPtr+3);      /* Length of the structure, in bytes. */

                PciBaseAddr pba = (PciBaseAddr) pciDevice.baseAddresses._get(bar);
                int confBase = (pba.address & ~0xF) + offset;

                commonConfig = (CommonConfig) MAGIC.cast2Struct(confBase);

                if ((pba.address & 1) == 1){
                    LowlevelLogging.debug("notify bar is i/o space, not taking it");
                } else {
                    cfgFound = true;
                }

            }

            // get NOTIFY CONF
            if (!notifyFound && cfg_type == VirtIo.VIRTIO_PCI_CAP_NOTIFY_CFG && cap_vndr == 0x09){

                int notify_off_multiplier = pciDevice.readConfigSpace(currentPtr+4);
                int cap_offset_within_bar = pciDevice.readConfigSpace(currentPtr+2);

                PciBaseAddr pba = (PciBaseAddr) pciDevice.baseAddresses._get(bar);
                int notifyBarAddr = pba.address & ~0xF;

                notifyConfig = new NotifyConfig(notifyBarAddr, cap_offset_within_bar, notify_off_multiplier, QUEUE_COUNT);

                if ((pba.address & 1) == 1){
                    LowlevelLogging.debug("notify bar is i/o space, not taking it");
                } else {
                    notifyFound = true;
                }
            }

            // get ISR CONF
            if (!isrFound && cfg_type == VirtIo.VIRTIO_PCI_CAP_ISR_CFG && cap_vndr == 0x09){
                int offset = pciDevice.readConfigSpace(currentPtr+2);

                PciBaseAddr pba = (PciBaseAddr) pciDevice.baseAddresses._get(bar);
                int isrBase = (pba.address & ~0xF) + offset;

                isrReg = (IsrReg) MAGIC.cast2Struct(isrBase);
                isrFound = true;
            }

            // device specific conf for mac
            if (!nicConfFound && cfg_type == VirtIo.VIRTIO_PCI_CAP_DEVICE_CFG && cap_vndr == 0x09){
                int offset = pciDevice.readConfigSpace(currentPtr+2);

                PciBaseAddr pba = (PciBaseAddr) pciDevice.baseAddresses._get(bar);
                int nicConfBase = (pba.address & ~0xF) + offset;

                nicConfReg = (NetConfig) MAGIC.cast2Struct(nicConfBase);
                nicConfFound = true;
            }

            currentPtr = cap_next / 4;
        } while (currentPtr != 0);

        if (!cfgFound){
            LowlevelLogging.debug("No config capability found. Legacy Device?");
            Kernel.stop();
        }

        if (!notifyFound){
            LowlevelLogging.debug("No notify capability found.");
            Kernel.stop();
        }

        if (!isrFound){
            LowlevelLogging.debug("No isr capability found.");
            Kernel.stop();
        }

        if (!nicConfFound){
            LowlevelLogging.debug("No nic conf capability found.");
            Kernel.stop();
        }
    }

    private void initDevice() {

        // reset device by writing 0 to status reg
        commonConfig.device_status = 0;
        // After writing 0 to device_status, the driver MUST wait for a read ofdevice_statusto return 0 before reinitial-izing the device.
        while (commonConfig.device_status != 0){Kernel.sleep();}// see 4.1.4.3.2

        // acknowledge existence of device
        commonConfig.device_status |= CommonConfig.VIRTIO_CONF_STATUS_ACKNOWLEDGE;

        // os knows how to drive device
        commonConfig.device_status |= CommonConfig.VIRTIO_CONF_STATUS_DRIVER;

        negotiateFeatures();

        // setup descriptors for both qeues // NO CHAINING IMPLEMENTED
        for (int i = 0; i < VirtqueueConstants.QUEUE_SIZE; i++) {
            DescriptorElement descr = transmitQueue.descriptors[i];
            descr.address = MAGIC.cast2Ref(bufferArea) + i * VirtqueueConstants.BUFFER_SIZE;
            descr.length = VirtqueueConstants.BUFFER_SIZE;
            descr.flags = 0;
            descr.next = 0;
        }

        for (int i = 0; i < VirtqueueConstants.QUEUE_SIZE; i++) {
            DescriptorElement descr = receiveQueue.descriptors[i];
            descr.address = MAGIC.cast2Ref(bufferArea) + i * VirtqueueConstants.BUFFER_SIZE;
            descr.length = VirtqueueConstants.BUFFER_SIZE;
            // allow device to write to all descriptors
            descr.flags = DescriptorElement.VIRTQ_DESC_F_WRITE;
            descr.next = 0;
        }

        setupReceiveQueue();
        setupTransmitQueue();

        // check queue addrss assignments
        if(commonConfig.queue_desc != MAGIC.cast2Ref(transmitQueue) + Virtqueue.DESCRIPTOR_OFFSET
                || commonConfig.queue_avail != MAGIC.cast2Ref(transmitQueue) + Virtqueue.AVAILABLE_RING_OFFSET
                || commonConfig.queue_used != MAGIC.cast2Ref(transmitQueue) + Virtqueue.USED_RING_OFFSET) {
            LowlevelLogging.debug("WRONG QUEUE ADDR ASSIGNMENT");
        }

        // now make device go live
        commonConfig.device_status |= CommonConfig.VIRTIO_CONF_STATUS_DRIVER_OK;

        // enable queues
        commonConfig.queue_select = (short) TRANSMIT_QUEUE;
        commonConfig.queue_enable = 1;

        commonConfig.queue_select = (short) RECEIVE_QUEUE;
        commonConfig.queue_enable = 1;

        // disable interrupts for transmit queue
        transmitQueue.availableRing.flags = (short) AvailableRing.VIRTQ_AVAIL_F_NO_INTERRUPT;

        populateReceiveQueue();

    }

    private void populateReceiveQueue() {
        // push buffers into receive available ring
        AvailableRing avail = receiveQueue.availableRing;
        //avail.idx = 0;
        avail.flags = 0;
        avail.used_event = 0;
        for (int i = 0; i < VirtqueueConstants.QUEUE_SIZE; i++){
            avail.ring[i] = (short) i;
        }

        MAGIC.inline(0x0F,0xAE,0xF0); //mfence Memory Fence
        avail.idx += VirtqueueConstants.QUEUE_SIZE;

        // check if notifications are enabled
        if((receiveQueue.usedRing.flags & UsedRing.VIRTQ_USED_F_NO_NOTIFY)==0){
            // notify device see 4.1.4.4
            MAGIC.wMem16(notifyConfig.getQueueNotifyAddr(RECEIVE_QUEUE),(short) RECEIVE_QUEUE);
        }
    }

    private void setupTransmitQueue() {
        // setup transmit queue
        commonConfig.queue_select = (short)TRANSMIT_QUEUE;
        if((commonConfig.queue_size & 0xFFFF) < VirtqueueConstants.QUEUE_SIZE){
            LowlevelLogging.debug("HVs T-Virtqueue size smaller than drivers!");
            Kernel.stop();
        }
        commonConfig.queue_size = (short)VirtqueueConstants.QUEUE_SIZE;
        commonConfig.queue_msix_vector = (short) CommonConfig.VIRTIO_MSI_NO_VECTOR;
        notifyConfig.setQueueNotifyOffset(TRANSMIT_QUEUE, commonConfig.queue_notify_off);
        commonConfig.queue_desc = MAGIC.cast2Ref(transmitQueue) + Virtqueue.DESCRIPTOR_OFFSET;
        commonConfig.queue_avail = MAGIC.cast2Ref(transmitQueue) + Virtqueue.AVAILABLE_RING_OFFSET;
        commonConfig.queue_used = MAGIC.cast2Ref(transmitQueue) + Virtqueue.USED_RING_OFFSET;
        commonConfig.queue_enable = 0;
    }

    private void setupReceiveQueue() {
        // setup receive queue
        commonConfig.queue_select = (short)RECEIVE_QUEUE;
        if(Unsigned.isLessThan(commonConfig.queue_size, VirtqueueConstants.QUEUE_SIZE)){
            LowlevelLogging.debug("HVs R-Virtqueue size smaller than drivers!");
            Kernel.stop();
        }
        commonConfig.queue_size = (short) VirtqueueConstants.QUEUE_SIZE;
        commonConfig.queue_msix_vector = (short) CommonConfig.VIRTIO_MSI_NO_VECTOR;
        notifyConfig.setQueueNotifyOffset(RECEIVE_QUEUE, commonConfig.queue_notify_off);
        commonConfig.queue_desc = MAGIC.cast2Ref(receiveQueue) + Virtqueue.DESCRIPTOR_OFFSET;
        commonConfig.queue_avail = MAGIC.cast2Ref(receiveQueue) + Virtqueue.AVAILABLE_RING_OFFSET;
        commonConfig.queue_used = MAGIC.cast2Ref(receiveQueue) + Virtqueue.USED_RING_OFFSET;
        commonConfig.queue_enable = 0;
    }

    private void negotiateFeatures() {
        if (!setFeature(VIRTIO_F_VERSION_1)){
            LowlevelLogging.debug("Virtio device does not support spec 1");
        }

        if (!setFeature(VIRTIO_NET_F_MAC)){
            LowlevelLogging.debug("Virtio device does not support mac feature");
        }

        if (!setFeature(VIRTIO_NET_F_STATUS)){
            LowlevelLogging.debug("Virtio device does not support status feature");
        }

        // set negotiation done
        commonConfig.device_status |= CommonConfig.VIRTIO_CONF_STATUS_FEATURES_OK;

        // check if device agrees
        if ((commonConfig.device_status & CommonConfig.VIRTIO_CONF_STATUS_FEATURES_OK) == 0){
            LowlevelLogging.debug("Virtio device does not support selected features");
        }
    }

    private boolean setFeature(int featureId){
        int featureRegister = featureId / 32;
        int offsetInRegister = featureId % 32;

        // check if device supports it
        commonConfig.device_feature_select = featureRegister;
        if ((commonConfig.device_feature & (1 << offsetInRegister))== 0){
            LowlevelLogging.debug(String.concat("Wanted feature not offered by device: ", String.from(featureId)));
            return false;
        }

        // confirm feature bits
        commonConfig.driver_feature_select = featureRegister;
        commonConfig.driver_feature = commonConfig.driver_feature | (1 << offsetInRegister);

        return true;

    }

    // todo save state used/unused of entry
    // for now this works like a ring buffer and may overwrite buffers that are currently in use by the device
    public void send(byte[] data){

        // todo set buffer size to data.length and reset on

        // check for
        if(transmitQueue.usedRing.idx != transmitNextToUseIdx){
            // do nothing
            transmitNextToUseIdx = transmitQueue.usedRing.idx;
            //LowlevelLogging.debug("WE TRANSMITTED A PACKET ");
        }

        if (data.length > VirtqueueConstants.BUFFER_SIZE - VirtioNetHeader.SIZE){
            LowlevelLogging.debug("Package to big!");
        }

        int nextIndex = (transmitQueue.availableRing.idx) % VirtqueueConstants.QUEUE_SIZE;
        DescriptorElement nextDescr = this.transmitQueue.descriptors[nextIndex];
        int nextBufAddr = (int) nextDescr.address;
        nextDescr.length = VirtioNetHeader.SIZE + data.length; // use only part of the buffer

        VirtioNetHeader header = (VirtioNetHeader) MAGIC.cast2Struct(nextBufAddr);
        header.flags = 0;
        header.gso_type = (byte)VirtioNetHeader.VIRTIO_NET_HDR_GSO_NONE;
        header.hdr_len = 0;
        header.csum_start = 0;
        header.csum_off = 0;
        header.num_buffers = 0;

        // copy to be send data into buf
        for (int i = 0; i < data.length; i++){
            MAGIC.wMem8(nextBufAddr + VirtioNetHeader.SIZE + i, data[i]);
        }

        // put index of buffer head into available queue
        transmitQueue.availableRing.ring[nextIndex] = (short) nextIndex;

        MAGIC.inline(0x0F,0xAE,0xF0); //mfence Memory Fence
        transmitQueue.availableRing.idx += 1;

        // check if notifications are enabled
        if((transmitQueue.usedRing.flags & UsedRing.VIRTQ_USED_F_NO_NOTIFY)==0){
            // notify device see 4.1.4.4
            MAGIC.wMem16(notifyConfig.getQueueNotifyAddr(TRANSMIT_QUEUE), (short) TRANSMIT_QUEUE);
        }
    }

    // todo check method
    @Override
    public byte[] receive() {
        if (receiveQueue.usedRing.idx != receiveNextToUseIdx){

            LowlevelLogging.debug(String.concat("rec q used ring ", String.from(receiveQueue.usedRing.idx)));


            int currentAvailIndex = (receiveNextToUseIdx) % VirtqueueConstants.QUEUE_SIZE;
            UsedRingElement usedElem = receiveQueue.usedRing.ring[currentAvailIndex];

            int buffAddr = (int) receiveQueue.descriptors[usedElem.id].address;
            DescriptorElement desc = (DescriptorElement) MAGIC.cast2Struct(buffAddr);

            LowlevelLogging.debug(String.concat("WE MAY HAVE RECEIVED SOMETHING", String.from(usedElem.len)));


            LowlevelLogging.debug(String.from(usedElem.len));
            byte[] data = new byte[usedElem.len];

            for (int i = 0; i < usedElem.len - VirtioNetHeader.SIZE; i++){
                data[i] = MAGIC.rMem8(buffAddr + VirtioNetHeader.SIZE + i);
            }

            // recycle buffer -> put back into available queue
            MAGIC.inline(0x0F,0xAE,0xF0); //mfence Memory Fence
            receiveQueue.availableRing.idx += 1;

            // check if notifications are enabled
            if((receiveQueue.usedRing.flags & UsedRing.VIRTQ_USED_F_NO_NOTIFY)==0){
                // notify device see 4.1.4.4
                MAGIC.wMem16(notifyConfig.getQueueNotifyAddr(RECEIVE_QUEUE),(short) RECEIVE_QUEUE);
            }

            receiveNextToUseIdx++;
            return data;
        }

        return null;
    }

    @Override
    public MacAddress getMacAddress() {
        byte[] addr = new byte[MacAddress.MAC_LEN];
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            addr[i] = nicConfReg.mac[i];
        }
        // todo bug melden: fehlermeldung wenn versucht wird an den konstruktor ein struct zu übergeben ist nicht sprechend.
        //  constructor MacAddress(byte[]) not found (has parent an explicit constructor?) in method getMacAddress() in unit VirtioNic
        return new MacAddress(addr);
    }

    @Override
    public boolean hasLink() {
        return (nicConfReg.status & NetConfig.VIRTIO_NET_S_LINK_UP) != 0;
    }

    public void printConf(CommonConfig conf, GreenScreenOutput out){
        out.print("driver feature: "); out.print(String.hexFrom(conf.driver_feature)); out.println();
        out.print("msix_config: "); out.print(String.hexFrom(conf.msix_config)); out.println();
        out.print("num_queues: "); out.print(String.hexFrom(conf.num_queues)); out.println();
        out.print("device status: "); out.print(String.hexFrom(conf.device_status)); out.println();
        out.print("config generation: "); out.print(String.hexFrom(conf.config_generation)); out.println();
        out.print("queue select: "); out.print(String.hexFrom(conf.queue_select)); out.println();
        out.print("queue size: "); out.print(String.hexFrom(conf.queue_size)); out.println();
        out.print("queue msix vector: "); out.print(String.hexFrom(conf.msix_config)); out.println();
        out.print("queue enabled: "); out.print(String.hexFrom(conf.queue_enable)); out.println();
        out.print("notify offset: "); out.print(String.hexFrom(conf.queue_notify_off)); out.println();
        out.print("queue descriptor: "); out.print(String.concat(String.hexFrom((int)(conf.queue_desc >> 32)), String.hexFrom((int)conf.queue_desc))); out.println();
        out.print("queue available: "); out.print(String.concat(String.hexFrom((int)(conf.queue_avail >> 32)), String.hexFrom((int)conf.queue_avail))); out.println();
        out.print("queue used: "); out.print(String.concat(String.hexFrom((int)(conf.queue_used >> 32)), String.hexFrom((int)conf.queue_used))); out.println();

    }
}
