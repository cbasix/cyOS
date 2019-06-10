package network.dhcp.msg;

import conversions.Endianess;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import network.address.MacAddress;

public class DhcpOption extends STRUCT{

    public static final byte OPT_PADDING = 0;
    public static final byte OPT_MSG_TYPE = 53;

    public static final byte MSG_TYPE_DISCOVER = 1;
    public static final byte MSG_TYPE_OFFER = 2;
    public static final byte MSG_TYPE_REQUEST = 3;
    public static final byte MSG_TYPE_ACK = 5;
    public static final byte MSG_TYPE_NOT_ACK = 6;

    public static final byte OPT_SUBNET_MASK = 1;
    public static final byte OPT_ROUTER = 3;
    public static final byte OPT_HOSTNAME = 12;
    public static final byte OPT_REQUESTED_IP = 50;
    public static final byte OPT_LEASE_TIME = 51;
    public static final byte OPT_DHCP_SERVER = 54;
    public static final byte OPT_DNS_SERVERS = 6;

    public static final byte OPT_END = (byte)255;

    public static final int FIXED_SIZE = 2;


    byte option;
    byte len;
    @SJC(count = 0, offset = 2)
    byte[] valueBytes;
    @SJC(count = 0, offset = 2)
    int[] valueInts;

}