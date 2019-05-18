package drivers.virtio.structs;

import network.MacAddress;

public class NetConfig extends STRUCT {
    public static final int VIRTIO_NET_S_LINK_UP = 1;
    public static final int VIRTIO_NET_S_ANNOUNCE = 2;

    @SJC(count = MacAddress.MAC_LEN)
    public byte[] mac;
    public short status; // only valid if VIRTIO_NET_F_STATUS feature active
    public short max_virtqueue_pairs; // only valid if VIRTIO_NET_F_MQ feature active

}
