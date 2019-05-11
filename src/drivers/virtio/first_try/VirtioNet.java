package drivers.virtio.first_try;

import drivers.pci.PciDevice;
import drivers.virtio.structs.CommonConfig;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.core.InterruptReceiver;


public class VirtioNet extends VirtIoPciDevice {
    public static final int VIRTIO_F_VERSION_1 = 32;
    public static final int VIRTIO_F_RING_EVENT_IDX = 29;

    private static final short ENABLED = 1;
    private static final short DISABLED = 0;


    public static final int INTERRUPT_NO = 43;
    private final InterruptHandler interruptHandler;

    public class InterruptHandler extends InterruptReceiver {
        @Override
        public boolean handleInterrupt(int interruptNo, int param) {
            if (interruptNo == INTERRUPT_NO){
                //todo read isr register an check if virtio was the source then
                return true;
            }
            return false;
        }
    }

    TransmitQueue transmitQueue;
    ReceiveQueue receiveQueue;

    public static VirtioNet from(PciDevice pciDevice){
        return new VirtioNet(pciDevice);
    }

    public VirtioNet(PciDevice pciDevice) {
        super(pciDevice);
        this.interruptHandler = new InterruptHandler();
    }

    @Override
    public void negotiateFeatures(CommonConfig conf) {
        conf.device_feature_select = 0;
        //read device feature bits
        int features1 = conf.device_feature;
        conf.device_feature_select = 1;
        //MAGIC.inline(0x0F,0xAE,0xF0); //mfence Memory Fence
        int features2 = conf.device_feature;

        if (conf.device_feature_select != 1){
            LowlevelLogging.debug("CRAZY SHIT IS GOING ON");
        }

        if (features1 == features2){
            LowlevelLogging.debug("are you really talking to a device?");
        }

        if ((features2 & 1) != 0) {
            //LowlevelLogging.debug("VERSION 1 SUPPORTED", LowlevelLogging.DEBUG_LEVEL);
        } else {
            LowlevelOutput.printHex(features2, 8, 15, 2, Color.PINK);
            LowlevelLogging.debug("VERSION 1 NOT! SUPPORTED" , LowlevelLogging.DEBUG_LEVEL);
        }

        // no features supported
        setFeatureFlag(conf, VIRTIO_F_VERSION_1);
        setFeatureFlag(conf, VIRTIO_F_RING_EVENT_IDX);
    }

    public void setFeatureFlag(CommonConfig conf, int featureId){
        int featureRegister = featureId / 32;
        int offsetInRegister = featureId % 32;
        conf.driver_feature_select = featureRegister;
        conf.driver_feature = conf.driver_feature | (1 << offsetInRegister);
    }

    public static final int QUEUE_CNT = 2; // todo read from from device maybe

    public static final int RECEIVE_QUEUE = 0;
    public static final int TRANSMIT_QUEUE = 1;

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

    /**
     * Virtqueue Configuration
     *
     * 1.Write the virtqueue index (first queue is 0) toqueue_select.
     * 2.Read the virtqueue size from queue_size. This controls how big the virtqueue is (see2.4Virtqueues).If this field is 0, the virtqueue does not exist.
     * 3.Optionally, select a smaller virtqueue size and write it toqueue_size.
     * 4.Allocate and zero Descriptor Table, Available and Used rings for the virtqueue in contiguous physicalmemory.
     * 5.Optionally, if MSI-X capability is present and enabled on the device, select a vector to use to requestinterrupts triggered by virtqueue events. Write the MSI-X Table entry number corresponding to thisvector intoqueue_msix_vector. Readqueue_msix_vector: on success, previously written value isreturned; on failure, NO_VECTOR value is returned.
     *
     * @param conf
     */
    @Override
    public void setup(CommonConfig conf) {
        //Kernel.interruptHub.addObserver(this.interrupHandler);
        setInterruptLine(11); // todo changing doesnt work


        GreenScreenOutput out = new GreenScreenOutput();


        conf.queue_select = 0;
        if (conf.queue_size == 0){
            LowlevelLogging.debug("Queue unavailable");
        } else {
            //printConf(conf, out);
        }



        //conf.msix_config = 0x1;
        //if(conf.msix_config != 0x1){LowlevelLogging.debug("msix_config not ok");};
        //conf.queue_select = 0x1;
        //if(conf.queue_select != 0x1){LowlevelLogging.debug("queue_select not ok");};


        //Kernel.wait(5);

        //printConf(conf, out);

        //LowlevelLogging.printHexdump(confBase);
        //Kernel.stop();


        out.setCursor(0,0);

        // setup queues
        for (short i = 0; i < QUEUE_CNT; i++) {
            conf.queue_select = i;

            // queue size = min(hypervisor_max, QUEUE_CNT)
            // todo reactivate. for now only fixed width is supported
            /*int queueSize = conf.queue_size;
            if (QUEUE_SIZE < queueSize || queueSize == -1){
                queueSize = QUEUE_SIZE;
            }*/

            int notifyAddr = this.notifyBaseAddr + notify_cap_offset + conf.queue_notify_off * notify_off_multiplier;

            LowlevelLogging.debug(String.hexFrom(notifyAddr));
            Kernel.wait(15);

            // todo only one transmit and receive queue supported  no config no second transmit queue ...
            VirtQueue currentlyAddedQueue;
            if (i == RECEIVE_QUEUE) {
                receiveQueue = new ReceiveQueue(i, notifyAddr);
                currentlyAddedQueue = receiveQueue;
            } else {
                transmitQueue = new TransmitQueue(i, notifyAddr);
                currentlyAddedQueue = transmitQueue;
            }

            if (conf.queue_size == 0){
                LowlevelLogging.debug("Queue unavailable");
            }

            conf.queue_desc = currentlyAddedQueue.descriptorTableAddr;
            conf.queue_used = currentlyAddedQueue.usedRingAddr;
            conf.queue_avail = currentlyAddedQueue.availableRingAddr;

            /*MAGIC.wMem32(confBase + 30, 0x4444444);
            MAGIC.wMem32(confBase + 34, 0x5555555);
            MAGIC.wMem32(confBase + 38, 0x6666666);
            MAGIC.wMem32(confBase + 34, 0x7777777);*/
            /*for (short j = 0; j < 50; j += 1){
                MAGIC.wMem32(confBase+j, (short)((j<<4) |j));
            }*/

            if (conf.queue_desc != currentlyAddedQueue.descriptorTableAddr){
                LowlevelOutput.printHex(conf.queue_desc, 16, 1, 1, Color.RED);
                LowlevelOutput.printHex(currentlyAddedQueue.descriptorTableAddr, 16, 1, 2, Color.GREEN);

                LowlevelLogging.debug("DescrQueue assignment wrong");
                Kernel.stop();
            }
            if (conf.queue_used != currentlyAddedQueue.usedRingAddr){
                LowlevelLogging.debug("UsedRing assignment wrong");
                Kernel.stop();
            }
            if (conf.queue_avail != currentlyAddedQueue.availableRingAddr){
                LowlevelLogging.debug("AvailRing assignment wrong");
                Kernel.stop();
            }

            // todo remove test
            /*int l = 0;
            while (true) {
                l += 4096;
                out.setCursor(0, 0);

                conf.queue_desc = l;

                out.print("queue desc conf: ");
                out.print(String.hexFrom((int)(conf.queue_desc >> 32)));
                out.print(String.hexFrom((int)conf.queue_desc));

                out.print(" queue: ");
                out.print(String.hexFrom((int)(l >> 32)));
                out.print(String.hexFrom((int)l));

                out.println();

                conf.queue_used = l;
                out.print("queue used conf: ");
                out.print(String.hexFrom((int)(conf.queue_used >> 32)));
                out.print(String.hexFrom((int)conf.queue_used));
                out.print(" queue: ");
                out.print(String.hexFrom((int)(l >> 32)));
                out.print(String.hexFrom((int)l));
                //out.print(String.hexFrom(currentlyAddedQueue.usedRingAddr));
                out.println();

                Kernel.wait(1);
            }*/


            // todo check long value (sign extension???)
            /*LowlevelLogging.debug("hexdmp queuedescr ");
            LowlevelLogging.printHexdump(currentlyAddedQueue.descriptorTableAddr);
            Kernel.wait(30);

            LowlevelLogging.debug("hexdmp usedring ");
            LowlevelLogging.printHexdump((int)conf.queue_used);
            Kernel.wait(30);

            LowlevelLogging.debug("hexdmp availring ");
            LowlevelLogging.printHexdump((int)conf.queue_avail);
            Kernel.wait(30);*/

            conf.queue_size = (short) VirtQueue.QUEUE_SIZE;
            // The driver MUST configure the other virtqueue fields before enabling the virtqueue withqueue_enable.
            conf.queue_enable = ENABLED;

            printConf(conf, out);
            //LowlevelLogging.printHexdump(confBase);
            Kernel.wait(2);

            // todo msi-x capabilities? NO
        }

        /*int desc = (int) (conf.queue_avail >> 32);
        LowlevelOutput.printHex(desc, 8, 1,1, Color.RED);
        Kernel.stop();*/
    }

    public void send(byte[] data){
        transmitQueue.transmit(data);
    }


    public byte[] receive() {
        return receiveQueue.receiveOne();
    }

}
