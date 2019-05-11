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


}
