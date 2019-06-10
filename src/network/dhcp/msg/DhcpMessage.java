package network.dhcp.msg;

import conversions.Endianess;
import io.LowlevelLogging;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.dhcp.DhcpServer;


public class DhcpMessage {

    public static final byte TYPE_DISCOVER = DhcpOption.MSG_TYPE_DISCOVER;
    public static final byte TYPE_REQUEST = DhcpOption.MSG_TYPE_REQUEST;
    public static final byte TYPE_OFFER = DhcpOption.MSG_TYPE_OFFER;
    public static final byte TYPE_ACK = DhcpOption.MSG_TYPE_ACK;
    public static final byte TYPE_NO_ACK = DhcpOption.MSG_TYPE_NOT_ACK;

    private int transactionId;
    private boolean broadcast;
    private IPv4Address clientIp = IPv4Address.fromString("0.0.0.0");
    private IPv4Address yourIp  = IPv4Address.fromString("0.0.0.0");
    private IPv4Address serverIp = IPv4Address.fromString("0.0.0.0");
    private MacAddress clientHwAddr;
    private String serverName = "cyosdhcp";

    private IPv4Address requestedIp = null;
    private byte type = TYPE_DISCOVER;
    private IPv4Address dnsserver = null;
    private IPv4Address gateway = null;

    public DhcpMessage(){}

    public static DhcpMessage fromBytes(byte[] data){
        DhcpMessage msg = new DhcpMessage();

        if (data.length < DhcpHeader.SIZE){
            LowlevelLogging.debug("Invalid Dhcp Package: too short");
            return null;
        }

        DhcpHeader dhcp = (DhcpHeader) MAGIC.cast2Struct(MAGIC.addr(data[0]));
        OptionsReader options = new OptionsReader(MAGIC.addr(dhcp.options[0]), data.length - DhcpHeader.SIZE);

        msg.transactionId = Endianess.convert(dhcp.transactionId);
        msg.broadcast = (dhcp.flags & (1 << DhcpHeader.BROADCAST_BIT)) != 0;

        msg.clientIp = IPv4Address.fromStruct(dhcp.clientIp);
        msg.yourIp = IPv4Address.fromStruct(dhcp.yourIp);
        msg.serverIp = IPv4Address.fromStruct(dhcp.serverIp);

        byte[] clientMacArr = new byte[MacAddress.MAC_LEN];
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            clientMacArr[i] = dhcp.clientHwAddr[i];
        }
        msg.clientHwAddr = MacAddress.fromBytes(clientMacArr);

        byte[] typeArr = options.getOptionValue(DhcpOption.OPT_MSG_TYPE);
        if (typeArr == null || typeArr.length != 1){
            LowlevelLogging.debug(String.concat(String.from(typeArr), " not a dhcp packet"));
            return null;
        }
        msg.type = typeArr[0];

        // todo check for null before assign
        msg.requestedIp = IPv4Address.fromBytes(options.getOptionValue(DhcpOption.OPT_REQUESTED_IP));
        msg.gateway = IPv4Address.fromBytes(options.getOptionValue(DhcpOption.OPT_ROUTER));
        msg.dnsserver = IPv4Address.fromBytes(options.getOptionValue(DhcpOption.OPT_DNS_SERVERS));

        byte[] netmask = options.getOptionValue(DhcpOption.OPT_SUBNET_MASK);
        if(netmask != null) {
            msg.yourIp.setNetmask(netmask);
        }

        return msg;
    }

    public byte[] toBytes(){
       // LowlevelLogging.debug("writing out your ip: ", yourIp.toString());

        OptionsWriter options = new OptionsWriter();
        options.write(DhcpOption.OPT_MSG_TYPE, (byte)type);
        // NETMASK IS contained in yourip!
        if(yourIp != null && yourIp.netmask != null) { options.write(DhcpOption.OPT_SUBNET_MASK, yourIp.netmask); }
        options.write(DhcpOption.OPT_LEASE_TIME, 86400); // = 1d
        if(serverIp != null) { options.write(DhcpOption.OPT_DHCP_SERVER, serverIp.addr); }
        if(dnsserver != null) { options.write(DhcpOption.OPT_DNS_SERVERS, dnsserver.addr); }
        if(gateway != null) { options.write(DhcpOption.OPT_ROUTER, gateway.addr); }
        if (requestedIp != null){ options.write(DhcpOption.OPT_REQUESTED_IP, requestedIp.addr); }
        options.write(DhcpOption.OPT_END);

        byte[] offer = new byte[DhcpHeader.SIZE + options.getSize()];

        DhcpHeader dhcp = (DhcpHeader) MAGIC.cast2Struct(MAGIC.addr(offer[0]));

        dhcp.operation = (byte) (type == DhcpOption.MSG_TYPE_DISCOVER || type == DhcpOption.MSG_TYPE_REQUEST ? DhcpHeader.BOOT_REQUEST : DhcpHeader.BOOT_REPLY);
        dhcp.hwType = DhcpServer.TYPE_ETHERNET;
        dhcp.hwAddrLen = (byte) MacAddress.MAC_LEN;
        dhcp.hops = 0;
        dhcp.transactionId = Endianess.convert(transactionId);
        dhcp.seconds = 0;
        dhcp.flags = (short)((broadcast ? 1 : 0) << DhcpHeader.BROADCAST_BIT);
        clientIp.writeTo(dhcp.clientIp);
        yourIp.writeTo(dhcp.yourIp);
        serverIp.writeTo(dhcp.serverIp);

        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            dhcp.clientHwAddr[i] = clientHwAddr.toBytes()[i];
        }

        dhcp.magicCookie = Endianess.convert(0x63825363); // = fixed value (DHCP)

        //Kernel.out.print((int)dhcp.operation);
        //Kernel.wait(4);

        options.writeTo(MAGIC.addr(dhcp.options[0]));

        return offer;
    }
    public int getTransactionId() {
        return transactionId;
    }

    public DhcpMessage setTransactionId(int transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public DhcpMessage setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
        return this;
    }

    public IPv4Address getClientIp() {
        return clientIp;
    }

    public DhcpMessage setClientIp(IPv4Address clientIp) {
        this.clientIp = clientIp;
        return this;
    }

    public IPv4Address getYourIp() {
        return yourIp;
    }

    public DhcpMessage setYourIp(IPv4Address yourIp) {
        this.yourIp = yourIp;
        return this;
    }

    public IPv4Address getServerIp() {
        return serverIp;
    }

    public DhcpMessage setServerIp(IPv4Address serverIp) {
        this.serverIp = serverIp;
        return this;
    }

    public MacAddress getClientHwAddr() {
        return clientHwAddr;
    }

    public DhcpMessage setClientHwAddr(MacAddress clientHwAddr) {
        this.clientHwAddr = clientHwAddr;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public DhcpMessage setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public IPv4Address getRequestedIp() {
        return requestedIp;
    }

    public DhcpMessage setRequestedIp(IPv4Address requestedIp) {
        this.requestedIp = requestedIp;
        return this;
    }

    public byte getType() {
        return type;
    }

    public DhcpMessage setType(byte type) {
        this.type = type;
        return this;
    }

    public IPv4Address getDnsserver() {
        return dnsserver;
    }

    public DhcpMessage setDnsserver(IPv4Address dnsserver) {
        this.dnsserver = dnsserver;
        return this;
    }

    public IPv4Address getGateway() {
        return gateway;
    }

    public DhcpMessage setGateway(IPv4Address gateway) {
        this.gateway = gateway;
        return this;
    }
}
