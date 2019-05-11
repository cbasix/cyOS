package drivers.virtio.structs;

public class DescriptorElement extends STRUCT {
    public static final int SIZE = 8 + 4 + 2 + 2;

    /* This marks a buffer as continuing via the next field. */
    public static final short VIRTQ_DESC_F_NEXT = 1;
    /* This marks a buffer as device writeConfigSpace-only (otherwise device read-only). */
    public static final short VIRTQ_DESC_F_WRITE = 2;
    /* This means the buffer contains a list of buffer descriptors. */
    public static final short VIRTQ_DESC_F_INDIRECT = 4;

    /* Address (guest-physical). */
    public long address;

    /* Length. */
    public int length;

    /* The flags as indicated above. */
    public short flags;

    /* Next field if flags & NEXT */
    public short next;
}
