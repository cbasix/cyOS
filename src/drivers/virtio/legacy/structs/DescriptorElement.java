package drivers.virtio.legacy.structs;

public class DescriptorElement extends STRUCT {
    /* Address (guest-physical). */
    public long address;

    /* Length. */
    public int length;

    /* The flags as indicated above. */
    public short flags;

    /* Next field if flags & NEXT */
    public short next;
}
