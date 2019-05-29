package network.structs;

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
    @SJC(count = 6)
    public byte[] senderHwAddr;
    public int senderProtocAddr;
    @SJC(count = 6)
    public byte[] targetHwAddr;
    public int targetProtocolAddr;
}
