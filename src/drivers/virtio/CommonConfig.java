package drivers.virtio;

/* impl of the virtio_pci_common_cfg struct 4.1.4.2 */
public class CommonConfig extends STRUCT {
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
    public short queue_size;                /* read-write, power of 2, or 0. 24*/
    public short queue_msix_vector;         /* read-write 26*/
    public short queue_enable;              /* read-write 28*/
    public short queue_notify_off;          /* read-only for driver 30*/
    //public int queue_desc_higher;                 /* read-write */
    public long queue_desc;                 /* read-write 32*/
    public long queue_avail;                /* read-write 40*/
    public long queue_used;                 /* read-write 48*/
}
