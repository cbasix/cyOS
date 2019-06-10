package network.ipstack.structs;

import network.dhcp.msg.IPv4AddrStruct;

// only works for ethernet & ip ;)
public class ArpMessage extends STRUCT {
    public static final short HW_TYPE_ETHERNET = 1;
    public static final short PROTOC_TYPE_IPv4 = 0x0800;
    public static final short OP_REQUEST = 1;
    public static final short OP_REPLY = 2;

    public static final int SIZE = 28;

    public short hardwareType;
    public short protocolType;
    public byte hwAddrLen, protocAddrLen;
    public short operation;
    public MacAddrStruct senderHwAddr;
    public IPv4AddrStruct senderProtocAddr;
    public MacAddrStruct targetHwAddr;
    public IPv4AddrStruct targetProtocolAddr;
}
