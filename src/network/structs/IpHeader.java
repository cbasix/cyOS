package network.structs;

public class IpHeader extends STRUCT {
    public static final int SIZE = 20;

    public byte versionIhl, tos;
    public short len;
    public short id, flagsFrag;
    public byte ttl, prot;
    public short chksum;
    public int srcIP, dstIP;

}
