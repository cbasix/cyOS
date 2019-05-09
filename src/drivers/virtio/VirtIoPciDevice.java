package drivers.virtio;

import drivers.pci.PciBaseAddr;
import drivers.pci.PciDevice;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;

public abstract class VirtIoPciDevice extends PciDevice {

    public int confBase;
    protected int notify_off_multiplier;
    protected int notify_cap_offset;
    public int notifyBaseAddr;

    public VirtIoPciDevice(PciDevice pciDevice) {
        super(pciDevice.busNo, pciDevice.deviceNo);
        // load conf base and notifier offsets from pci-capabilities
        parseCapabilities();
        initalize();
    }

    /**
     * 3.1 Device Initialization
     * 3.1.1 Driver Requirements: Device Initialization
     *
     * The driver MUST follow this sequence to initialize a device:
     * 1. Reset the device.
     * 2 .Set the ACKNOWLEDGE status bit: the guest OS has notice the device.
     * 3. Set the DRIVER status bit: the guest OS knows how to drive the device.
     * 4. Read device feature bits, and writeConfigSpace the subset of feature bits understood by
     *    the OS and driver to the device. During this step the driver MAY read
     *    (but MUST NOT writeConfigSpace) the device-specific configurationfields to check that
     *    it can support the device before accepting it.5.Set the FEATURES_OK
     *    status bit. The driver MUST NOT accept new feature bits after this step.
     * 6. Re-readdevice status to ensure the FEATURES_OK bit is still set: otherwise,
     *    the device does notsupport our subset of features and the device is unusable.
     * 7. Perform device-specific setup, including discovery of virtqueues for the device,
     *    optional per-bus setup,reading and possibly writing the device’s virtio configuration
     *    space, and population of virtqueues.8.Set the DRIVER_OK status bit. At this point the
     *    device is “live”.
     *
     *
     * If any of these steps go irrecoverably wrong, the driver SHOULD set
     * the FAILED status bit to indicate that ithas given up on the device (it can reset the
     * device later to restart if desired). The driver MUST NOT continueinitialization in that
     * case.The driver MUST NOT notify the device before setting DRIVER_OK
     *
     * @return boolean true if initialisation was ok
     */
    public boolean initalize(){
        if (confBase < 0xFF && confBase > 0){
            // the device does not support our subset of features and the device is unusable.
            LowlevelLogging.debug(String.concat("Conf base seems wrong: ", String.hexFrom(confBase)));
        }
        CommonConfig conf = (CommonConfig) MAGIC.cast2Struct(confBase);

        // reset device by writing 0 to status reg
        conf.device_status = 0;
        // After writing 0 to device_status, the driver MUST wait for a read ofdevice_statusto return 0 before reinitial-izing the device.
        while (conf.device_status != 0){Kernel.sleep();}// see 4.1.4.3.2

        // Acknowledge device (let it know that its presence will will get honored soon :;
        conf.device_status = (byte)VirtIo.VIRTIO_CONF_STATUS_ACKNOWLEDGE;

        // set driver status bit (let it know we know how to drive it)
        conf.device_status = (byte)(VirtIo.VIRTIO_CONF_STATUS_ACKNOWLEDGE | VirtIo.VIRTIO_CONF_STATUS_DRIVER);

        // negotiate features (device specific)
        negotiateFeatures(conf);

        // set features ok
        conf.device_status = (byte)(VirtIo.VIRTIO_CONF_STATUS_ACKNOWLEDGE | VirtIo.VIRTIO_CONF_STATUS_DRIVER | VirtIo.VIRTIO_CONF_STATUS_FEATURES_OK);
        MAGIC.inline(0x0F, 0xAE, 0xF0); //mfence Memory Fence

        // check if features ok is still set
        if ((conf.device_status & VirtIo.VIRTIO_CONF_STATUS_FEATURES_OK) == 0){
            // the device does not support our subset of features and the device is unusable.
            LowlevelLogging.debug("Virtio device does not support selected features");
        }

        // perform device specific setup (virtqueues, etc. )
        setup(conf);


        // set driver go -> now the device is LIVE
        conf.device_status = (byte)(VirtIo.VIRTIO_CONF_STATUS_ACKNOWLEDGE | VirtIo.VIRTIO_CONF_STATUS_DRIVER |
                VirtIo.VIRTIO_CONF_STATUS_FEATURES_OK | VirtIo.VIRTIO_CONF_STATUS_DRIVER_OK);


        /*LowlevelOutput.printInt(conf.device_status,2 ,8, 1,1,  Color.RED);
        loadInfos();
        LowlevelOutput.printInt(status, 2, 16, 1, 2, Color.RED);
        Kernel.wait(10);*/

        return true;
    }

    /** should be overwritten by drivers for concrete devices */
    public abstract void setup(CommonConfig conf);

    /** should be overwritten by drivers for concrete devices
     *  simple version :
     *
     * //read device feature bits
     * int features = conf.device_feature;
     *
     * // no features supported
     * conf.driver_feature = 0x0;
     *
     * */
    public abstract void negotiateFeatures(CommonConfig conf);


    /* see 4.1.4 */
    private void parseCapabilities(){
        // todo prettify pci capabilities stuff
        int virtioPciCapAddr = capabilitiesPointer;

        /*while(true){
            dump(60);
            Kernel.wait(3);
            dump(0);
            Kernel.wait(3);
        }
        Kernel.stop();*/

        int i = 0;

        //GreenScreenOutput out = new GreenScreenOutput();
        //out.setColorState(Color.RED);
        //out.setCursor(1, 3);

        boolean cfgFound = false;
        boolean notifyFound = false;
        do {
            int tmp = readConfigSpace(virtioPciCapAddr);
            //LowlevelOutput.printStr(String.hexFrom(tmp), 1, 4, Color.RED);

            char cap_vndr = (char)(tmp & 0xFF);    /* Generic PCI field: PCI_CAP_ID_VNDR */
            char cap_next = (char)((tmp >> 8) & 0xFF);    /* Generic PCI field: next ptr. */
            char cap_len = (char)((tmp >> 16) & 0xFF);     /* Generic PCI field: capability length */
            char cfg_type = (char)((tmp >> 24) & 0xFF);    /* Identifies the structure. */

            tmp = readConfigSpace(virtioPciCapAddr+1);
            char bar = (char)(tmp & 0xFF);

            // get conf base
            // 0x9 -> vendor specific capability

            if (!cfgFound && cfg_type == VirtIo.VIRTIO_PCI_CAP_COMMON_CFG && cap_vndr == 0x09){

                                              /* Where to find it. */
                int offset = readConfigSpace(virtioPciCapAddr+2);      /* Offset within bar. */
                int length = readConfigSpace(virtioPciCapAddr+3);      /* Length of the structure, in bytes. */

                /*LowlevelLogging.debug(String.concat("conf bar: ", String.from(bar)));
                LowlevelLogging.debug(String.concat("conf bar length: ", String.from(length)));
                LowlevelLogging.debug(String.concat("conf bar offset: ", String.from(offset)));*/

                PciBaseAddr pba = (PciBaseAddr) baseAddresses._get(bar);
                confBase = (pba.address & ~0xF) + offset;

                if ((pba.address & 1) == 1){
                    LowlevelLogging.debug("notify bar is i/o space, not taking it");
                } else {
                    cfgFound = true;
                }

            }

            // get notify offset multiplier
            if (!notifyFound && cfg_type == VirtIo.VIRTIO_PCI_CAP_NOTIFY_CFG && cap_vndr == 0x09){

                notify_off_multiplier = readConfigSpace(virtioPciCapAddr+4);
                //LowlevelLogging.debug(String.concat(String.from(notify_off_multiplier), "  notify off multiplier   "));
                notify_cap_offset = readConfigSpace(virtioPciCapAddr+2);
                //LowlevelLogging.debug(String.concat("  notify cap offset  ", String.from(notify_cap_offset)));

                PciBaseAddr pba = (PciBaseAddr) baseAddresses._get(bar);
                notifyBaseAddr = pba.address & ~0x3;

                if ((pba.address & 1) == 1){
                    LowlevelLogging.debug("notify bar is i/o space, not taking it");
                } else {
                    notifyFound = true;
                }
            }



            /*out.print("vndr: ");
            out.print(String.from(cap_vndr));
            out.print(" cfg_type: ");
            out.print(String.from(cfg_type));
            out.print(" bar: ");
            out.print(String.from(bar));
            out.println();*/

            virtioPciCapAddr = cap_next / 4;
            i++;
        } while (virtioPciCapAddr != 0);

        if (!cfgFound){
            LowlevelLogging.debug("No config capability found");
            Kernel.stop();
        }

        if (!notifyFound){
            LowlevelLogging.debug("No notify capability found");
            Kernel.stop();
        }
    }



}
