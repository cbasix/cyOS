package drivers.virtio;

public class VirtIo {
    public static final int VIRTIO_VENDOR_ID = 0x1AF4;
    public static final int VIRTIO_NETWORK_CARD_ID = 0x1000;
    public static final int VIRTIO_NETWORK_CARD_ID_2 = 0x1041;

    /* PCI CAPABILITIES */
    /* Common configuration */
    public static final int VIRTIO_PCI_CAP_COMMON_CFG = 1;
    /* Notifications */
    public static final int VIRTIO_PCI_CAP_NOTIFY_CFG = 2;
    /* ISR Status */
    public static final int VIRTIO_PCI_CAP_ISR_CFG = 3;
    /* Device specific configuration */
    public static final int VIRTIO_PCI_CAP_DEVICE_CFG = 4;
    /* PCI configuration access */
    public static final int VIRTIO_PCI_CAP_PCI_CFG = 5;

    /* common conf status flags */

    /* Indicates that the guest OS has found the device and recognized it as a valid virtiodevice. */
    public static final int VIRTIO_CONF_STATUS_ACKNOWLEDGE = 1;
    /* Indicates that the guest OS knows how to drive the device.*/
    public static final int VIRTIO_CONF_STATUS_DRIVER = 2;
    /* Indicates that the driver has acknowledged all the features it understands, and featurenegotiation is complete. */
    public static final int VIRTIO_CONF_STATUS_FEATURES_OK = 8;
    /* Indicates that the driver is set up and ready to drive the device. */
    public static final int VIRTIO_CONF_STATUS_DRIVER_OK = 4;
    /* Indicates that the device has experienced an error from which it can’t re-cover */
    public static final int VIRTIO_CONF_STATUS_DEVICE_NEEDS_RESET = 64;
    /* Indicates that something went wrong in the guest, and it has given up on the device. Thiscould be an internal error, or the driver didn’t like the device for some reason, or even a fatal errorduring device operation */
    public static final int VIRTIO_CONF_STATUS_FAILED = 128;

}
