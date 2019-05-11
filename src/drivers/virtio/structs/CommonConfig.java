package drivers.virtio.structs;

/* impl of the virtio_pci_common_cfg struct 4.1.4.2 */
public class CommonConfig extends STRUCT {
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

    public static final int VIRTIO_MSI_NO_VECTOR = 0xffff;


    /* About the whole device. */
    public int device_feature_select;     /* read-write 0*/
    public int device_feature;            /* read-only for driver 4*/
    public int driver_feature_select;     /* read-write 8*/
    public int driver_feature;            /* read-write 12*/
    public short msix_config;             /* read-write 16*/
    public short num_queues;              /* read-only for driver 18*/
    public byte device_status;            /* read-write 20*/
    public byte config_generation;        /* read-only for driver 21*/

    /* About a specific virtqueue. */
    public short queue_select;              /* read-write 22*/
    public short queue_size;                /* read-write, power of 2, or 0.   24*/
    public short queue_msix_vector;         /* read-write 26*/
    public short queue_enable;              /* read-write 28*/
    public short queue_notify_off;          /* read-only for driver 30*/
    //public int queue_desc_higher;                 /* read-write */
    public long queue_desc;                 /* read-write 32*/
    public long queue_avail;                /* read-write 40*/
    public long queue_used;                 /* read-write 48*/
}
