package network.dhcp.msg;

import network.address.IPv4Address;

public class IPv4AddrStruct extends STRUCT {
    @SJC(count = IPv4Address.IPV4_LEN)
    public byte[] ip;
}
