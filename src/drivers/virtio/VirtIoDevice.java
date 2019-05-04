package drivers.virtio;

import drivers.pci.PciDevice;

public class VirtIoDevice extends PciDevice {

    public static VirtIoDevice from(PciDevice pciDevice){
        new VirtIoDevice(pciDevice)
    }

    private VirtIoDevice(PciDevice pciDevice) {
        super(pciDevice.busNo, pciDevice.deviceNo);
    }

    /**
     * 3 General Initialization And Device Operation
     * We start with an overview of device initialization, then expand on the details of the
     * device and how eachstep is preformed. This section is best read along with the bus-specific
     * section which describes how to communicate with the specific device.
     *
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
     * 6. Re-readdevice statusto ensure the FEATURES_OK bit is still set: otherwise,
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
        write();
        return true;
    }
}
