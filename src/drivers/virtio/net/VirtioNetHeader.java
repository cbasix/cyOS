package drivers.virtio.net;

public class VirtioNetHeader extends STRUCT {
    public static final int SIZE = 1 * 2 + 2 * 5;

    public static final int VIRTIO_NET_HDR_F_NEEDS_CSUM = 1;
    public static final int VIRTIO_NET_HDR_GSO_NONE = 0;
    public static final int VIRTIO_NET_HDR_GSO_TCPV4 = 1;
    public static final int VIRTIO_NET_HDR_GSO_UDP = 3;
    public static final int VIRTIO_NET_HDR_GSO_TCPV6 = 4;
    public static final int VIRTIO_NET_HDR_GSO_ECN = 0x80;

    public byte flags;
    public byte gso_type;

    public short hdr_len;
    public short gso_size;
    public short csum_start;
    public short csum_off;
    public short num_buffers;
}
