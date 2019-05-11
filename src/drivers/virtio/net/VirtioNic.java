package drivers.virtio.net;

import arithmetics.Unsigned;
import drivers.pci.PciBaseAddr;
import drivers.pci.PciDevice;
import drivers.virtio.RawMemoryContainer;
import drivers.virtio.VirtIo;
import drivers.virtio.first_try.TransmitQueue;
import drivers.virtio.structs.*;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;

import drivers.virtio.structs.VirtqueueConstants;
import kernel.interrupts.core.Interrupts;
import kernel.memory.MarkAndSweepGarbageCollector;

public class VirtioNic {


    public static final int VIRTIO_F_VERSION_1 = 32;
    public static final int VIRTIO_F_RING_EVENT_IDX = 29;

    public static final int INTERRUPT_LINE = 11; // todo verify
    public static final int INTERRUPT_NO = Interrupts.IRQ11; // todo verify

    public static final int QUEUE_COUNT = 2;

    public static final int RECEIVE_QUEUE = 0;
    public static final int TRANSMIT_QUEUE = 1;

    public static final int ALIGN_SPACE = 15 * 2 + 3;


    private RawMemoryContainer rawMem;

    // addresen nötig da MAGIC.cast2Ref nicht mit STRUCTS umgehen kann...
    private int transmitQueueAddr;
    private Virtqueue transmitQueue;
    private int receiveQueueAddr;
    private Virtqueue receiveQueue;
    private int bufferAreaAddr;
    private BufferArea bufferArea;

    private CommonConfig commonConfig;
    private NotifyConfig notifyConfig;

    private VirtioInterruptAdapter interruptAdapter;
    private PciDevice pciDevice;
    private IsrReg isrReg;


    class VirtioInterruptAdapter extends InterruptReceiver {
        public static final int CONFIG_INT = 1;
        public static final int QUEUE_INT = 2;

        @Override
        public boolean handleInterrupt(int interruptNo, int param) {
            LowlevelLogging.debug("GOT AN INTERRUPT");
            // this read resets the interrupt!
            if((isrReg.data & 0x3) != 0){
                LowlevelLogging.debug("GOT VIRTIO INTERRUPT");
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

    public VirtioNic(PciDevice pciDevice){
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

        transmitQueueAddr = (rawMem.getRawAddr() + 15) & ~15;
        receiveQueueAddr = (transmitQueueAddr + Virtqueue.SIZE + 15) & ~15;
        bufferAreaAddr = (receiveQueueAddr + Virtqueue.SIZE + 3) & ~3;

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

        initDevice();
    }


    private void parsePciCapabilities() {
        int currentPtr = pciDevice.capabilitiesPointer;

        boolean cfgFound = false;
        boolean notifyFound = false;
        boolean isrFound = false;
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
            // todo device specific conf for mac

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

        // simplified negotiation // todo check
        commonConfig.device_feature_select = 1;
        if ((commonConfig.device_feature & (1 << (VIRTIO_F_VERSION_1 - 32)))== 0){
            LowlevelLogging.debug("Virtio device does not support spec 1");
        }

        commonConfig.driver_feature_select = 1; // select second feature register (bits 32 -> 63)
        commonConfig.driver_feature = 1 << (VIRTIO_F_VERSION_1 - 32); // -32 because bit 0-31 are in feature reg 0

        commonConfig.device_status |= CommonConfig.VIRTIO_CONF_STATUS_FEATURES_OK;

        if ((commonConfig.device_status & CommonConfig.VIRTIO_CONF_STATUS_FEATURES_OK) == 0){
            LowlevelLogging.debug("Virtio device does not support selected features");
        }

        // setup descriptors for both qeues // NO CHAINING IMPLEMENTED
        for (int i = 0; i < VirtqueueConstants.QUEUE_SIZE; i++) {
            DescriptorElement descr = transmitQueue.descriptors[i];
            descr.address = bufferAreaAddr + i * VirtqueueConstants.BUFFER_SIZE;
            descr.length = VirtqueueConstants.BUFFER_SIZE;
            descr.flags = 0;
            descr.next = 0;
        }
        for (int i = 0; i < VirtqueueConstants.QUEUE_SIZE; i++) {
            DescriptorElement descr = receiveQueue.descriptors[i];
            descr.address = bufferAreaAddr + i * VirtqueueConstants.BUFFER_SIZE;
            descr.length = VirtqueueConstants.BUFFER_SIZE;
            // allow device to write to all descriptors
            descr.flags = DescriptorElement.VIRTQ_DESC_F_WRITE;
            descr.next = 0;
        }

        // setup receive queue
        commonConfig.queue_select = (short)RECEIVE_QUEUE;
        if(Unsigned.isLessThan(commonConfig.queue_size, VirtqueueConstants.QUEUE_SIZE)){
            LowlevelLogging.debug("HVs R-Virtqueue size smaller than drivers!");
            Kernel.stop();
        }
        commonConfig.queue_size = (short) VirtqueueConstants.QUEUE_SIZE;
        commonConfig.queue_msix_vector = (short) CommonConfig.VIRTIO_MSI_NO_VECTOR;
        notifyConfig.setQueueNotifyOffset(RECEIVE_QUEUE, commonConfig.queue_notify_off);
        commonConfig.queue_desc = receiveQueueAddr + Virtqueue.DESCRIPTOR_OFFSET;
        commonConfig.queue_avail = receiveQueueAddr + Virtqueue.AVAILABLE_RING_OFFSET;
        commonConfig.queue_used = receiveQueueAddr + Virtqueue.USED_RING_OFFSET;
        commonConfig.queue_enable = 0;

        // setup transmit queue
        commonConfig.queue_select = (short)TRANSMIT_QUEUE;
        if((commonConfig.queue_size & 0xFFFF) < VirtqueueConstants.QUEUE_SIZE){
            LowlevelLogging.debug("HVs T-Virtqueue size smaller than drivers!");
            Kernel.stop();
        }
        commonConfig.queue_size = (short)VirtqueueConstants.QUEUE_SIZE;
        commonConfig.queue_msix_vector = (short) CommonConfig.VIRTIO_MSI_NO_VECTOR;
        notifyConfig.setQueueNotifyOffset(TRANSMIT_QUEUE, commonConfig.queue_notify_off);
        commonConfig.queue_desc = transmitQueueAddr + Virtqueue.DESCRIPTOR_OFFSET;
        commonConfig.queue_avail = transmitQueueAddr + Virtqueue.AVAILABLE_RING_OFFSET;
        commonConfig.queue_used = transmitQueueAddr + Virtqueue.USED_RING_OFFSET;
        commonConfig.queue_enable = 0;

        if(commonConfig.queue_desc != transmitQueueAddr + Virtqueue.DESCRIPTOR_OFFSET
                || commonConfig.queue_avail != transmitQueueAddr + Virtqueue.AVAILABLE_RING_OFFSET
                || commonConfig.queue_used != transmitQueueAddr + Virtqueue.USED_RING_OFFSET) {
            LowlevelLogging.debug("WRONG QUEUE ADDR ASSIGNMENT");
        }

        // now make device go live
        commonConfig.device_status |= CommonConfig.VIRTIO_CONF_STATUS_DRIVER_OK;

        // enable queues
        commonConfig.queue_select = (short) TRANSMIT_QUEUE;
        commonConfig.queue_enable = 1;

        commonConfig.queue_select = (short) RECEIVE_QUEUE;
        commonConfig.queue_enable = 1;

        // push buffers into receive available ring
        AvailableRing avail = receiveQueue.availableRing;
        avail.idx = 0;
        avail.flags = 0;
        avail.used_event = 0;
        for (int i = 0; i < VirtqueueConstants.QUEUE_SIZE; i++){
            avail.ring[i] = (short) i;
        }

        MAGIC.inline(0x0F,0xAE,0xF0); //mfence Memory Fence
        avail.idx += VirtqueueConstants.QUEUE_SIZE-5; // todo 5 testing

        // check if notifications are enabled
        if((receiveQueue.usedRing.flags & UsedRing.VIRTQ_USED_F_NO_NOTIFY)==0){
            // notify device see 4.1.4.4
            MAGIC.wMem16(notifyConfig.getQueueNotifyAddr(RECEIVE_QUEUE),(short) RECEIVE_QUEUE);
        }

    }

    public void send(byte[] data){
        if(transmitQueue.usedRing.idx != 0){
            LowlevelLogging.debug("WE MAY HAVE TRANSMITTED A PACKET");
        }

        if (data.length > VirtqueueConstants.BUFFER_SIZE - VirtioNetHeader.SIZE){
            LowlevelLogging.debug("Package to big!");
        }

        int nextIndex = (transmitQueue.availableRing.idx) % VirtqueueConstants.QUEUE_SIZE;
        DescriptorElement nextDescr = this.transmitQueue.descriptors[nextIndex];
        int nextBufAddr = (int) nextDescr.address;

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
