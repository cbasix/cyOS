package network.dhcp.msg;

public class DhcpHeader extends STRUCT {
    public static final int SIZE = 4*1 + 4 + 2*2 + 4*4 + 16 + 64 + 128 + 4;

    public static final int BOOT_REQUEST = 1;
    public static final int BOOT_REPLY = 2;
    public static final int BROADCAST_BIT = 15;

    public byte operation;
    public byte hwType;
    public byte hwAddrLen;
    public byte hops;
    public int transactionId;
    public short seconds;
    public short flags;
    public IPv4AddrStruct clientIp;
    public IPv4AddrStruct yourIp;
    public IPv4AddrStruct serverIp;
    public IPv4AddrStruct gatewayIp;
    @SJC(count = 16)
    public byte[] clientHwAddr;
    @SJC(count = 64)
    public byte[] serverName;
    @SJC(count = 128)
    public byte[] bootFilename;

    public int magicCookie;

    @SJC(count = 0)
    public byte[] options;
}
