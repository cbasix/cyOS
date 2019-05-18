package network.structs;

import network.PacketNetwork;

public class UdpHeader extends STRUCT{
    public static final int SIZE = 8;

    public short srcPort, dstPort;
    public short len, chk;
}
