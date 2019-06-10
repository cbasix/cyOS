package network.ipstack.structs;

import network.dhcp.msg.IPv4AddrStruct;

public class IpHeader extends STRUCT {
    public static final int SIZE = 20;

    public byte versionIhl, tos;
    public short len;
    public short id, flagsFrag;
    public byte ttl, prot;
    public short chksum;
    public IPv4AddrStruct srcIP, dstIP;

}
