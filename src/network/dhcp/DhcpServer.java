package network.dhcp;

import conversions.Endianess;
import io.LowlevelLogging;
import kernel.Kernel;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.dhcp.structs.DhcpHeader;
import network.ipstack.NetworkStack;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.PackageReceiver;
import random.PseudoRandom;


public class DhcpServer extends PackageReceiver {
    public static final int DHCP_SERVER_PORT = 67;
    public static final int DHCP_CLIENT_PORT = 68;
    private static final byte TYPE_ETHERNET = 1;

    private final NetworkStack stack;
    private DhcpCache cache;

    public DhcpServer () {
        stack = Kernel.networkManager.stack;
        cache = new DhcpCache();
        stack.bindingsManager.bind(stack.udpLayer, DHCP_SERVER_PORT, this);
    }

    @Override
    public void receive(TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data) {
        //LowlevelLogging.debug("Dhcp Server got a package!");

        if (data.length < DhcpHeader.SIZE){
            LowlevelLogging.debug("Invalid Dhcp Package: too short");
        }

        DhcpHeader dhcp = (DhcpHeader) MAGIC.cast2Struct(MAGIC.addr(data[0]));
        OptionsReader options = new OptionsReader(MAGIC.addr(dhcp.options[0]), data.length - DhcpHeader.SIZE);

        /*Kernel.out.print("Transaction id: ");
        Kernel.out.println(String.hexFrom(Endianess.convert(dhcp.transactionId)));
        Kernel.out.print("DHCP msg type: ");
        byte[] val = options.getOptionValue(DhcpOption.OPT_MSG_TYPE);
        if (val == null){
            Kernel.out.println("Option MSG_TYPE not found");
        } else {
            Kernel.out.println((int)val[0]);
        }
        Kernel.out.print("DHCP hostname: ");
        val = options.getOptionValue(DhcpOption.OPT_HOSTNAME);
        if (val == null){
            Kernel.out.println("Option Hostname not found");
        } else {
            Kernel.out.println(String.from(val));
        }*/

        int transactionId = Endianess.convert(dhcp.transactionId);
        byte[] clientMacArr = new byte[MacAddress.MAC_LEN];
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            clientMacArr[i] = dhcp.clientHwAddr[i];
        }
        MacAddress clientMac = MacAddress.fromBytes(clientMacArr);
        byte[] requestedIpBytes = options.getOptionValue(DhcpOption.OPT_REQUESTED_IP);
        IPv4Address requestedIp = null;
        if (requestedIpBytes == null){
            requestedIp = null;
        } else {
            requestedIp = new IPv4Address(requestedIpBytes);
        }
        byte[] typeArr = options.getOptionValue(DhcpOption.OPT_MSG_TYPE);
        if (typeArr == null || typeArr.length != 1){
            LowlevelLogging.debug(String.concat(String.from(typeArr), "not a dhcp packet"));
            return;
        }
        byte type = typeArr[0];
        byte operation = dhcp.operation;

        if (type == DhcpOption.MSG_TYPE_DISCOVER) {
            handleDiscovery(transactionId, clientMac, requestedIp);

        } else if (type == DhcpOption.MSG_TYPE_REQUEST){
            handleRequest(transactionId, clientMac, requestedIp);
        }

        // todo release not implemented
    }

    public void handleDiscovery(int transactionId, MacAddress clientMac, IPv4Address requestedIp) {
        /*if (requestedIp != null){
            // renew or proposed ip given
            if (DhcpCache.AVAILABLE == cache.getStatus(requestedIp)){
                sendMessage(transactionId, requestedIp, clientMac, DhcpOption.MSG_TYPE_OFFER);
                return;
                // todo check if ip is in range of current network
            }
        }*/
        //LowlevelLogging.debug("On Discovery");
        // generate random ip
        IPv4Address myIp = stack.ipLayer.getDefaultIp();

        IPv4Address clientIP = null;
        do {
            clientIP = new IPv4Address((myIp.toInt() & ~0xFF) | PseudoRandom.getRandInt() & 0xFE);
        } while (cache.getStatus(clientIP) != DhcpCache.AVAILABLE);

        byte[] msg = buildMessage(transactionId, myIp, stack.getDnsServer(), stack.ipLayer.getDefaultGateway(), clientIP, clientMac, DhcpOption.MSG_TYPE_OFFER);
        stack.udpLayer.send(IPv4Address.getGlobalBreadcastAddr(), DHCP_SERVER_PORT, DHCP_CLIENT_PORT, msg);
        //LowlevelLogging.debug("Discovery answered");
    }

    private void handleRequest(int transactionId, MacAddress clientMac, IPv4Address requestedIp) {
        //LowlevelLogging.debug("On Request");
        // todo checks...
        IPv4Address serverIp = stack.ipLayer.getDefaultIp();

        if(requestedIp == null || !requestedIp.isInSameNetwork(serverIp)){
            do {
                requestedIp = new IPv4Address((serverIp.toInt() & ~0xFF) | PseudoRandom.getRandInt() & 0xFE);
            } while (cache.getStatus(requestedIp) != DhcpCache.AVAILABLE);

        }

        byte[] msg = buildMessage(transactionId, serverIp, stack.getDnsServer(), stack.ipLayer.getDefaultGateway(), requestedIp, clientMac, DhcpOption.MSG_TYPE_ACK);
        stack.udpLayer.send(IPv4Address.getGlobalBreadcastAddr(), DHCP_SERVER_PORT, DHCP_CLIENT_PORT, msg);

        //LowlevelLogging.debug("Request answered");
        //LowlevelLogging.debug(requestedIp.toString());
    }


    public static byte[] buildMessage(int transactionId, IPv4Address serverIp, IPv4Address dnsserver, IPv4Address gateway, IPv4Address clientIp, MacAddress clientMac, byte msgType){
        OptionsWriter options = new OptionsWriter();
        options.write(DhcpOption.OPT_MSG_TYPE, msgType);
        options.write(DhcpOption.OPT_SUBNET_MASK, 0xFFFFFF00);
        options.write(DhcpOption.OPT_LEASE_TIME, 86400); // = 1d
        options.writeIp(DhcpOption.OPT_DHCP_SERVER, serverIp.toInt());
        options.writeIp(DhcpOption.OPT_DNS_SERVERS, dnsserver.toInt());
        options.writeIp(DhcpOption.OPT_ROUTER, gateway.toInt());
        options.writeIp(DhcpOption.OPT_REQUESTED_IP, clientIp.toInt());
        options.write(DhcpOption.OPT_END);

        byte[] offer = new byte[DhcpHeader.SIZE + options.getSize()];

        DhcpHeader dhcp = (DhcpHeader) MAGIC.cast2Struct(MAGIC.addr(offer[0]));

        dhcp.operation = (byte) (msgType == DhcpOption.MSG_TYPE_DISCOVER || msgType == DhcpOption.MSG_TYPE_REQUEST ? DhcpHeader.BOOT_REQUEST : DhcpHeader.BOOT_REPLY);
        dhcp.hwType = TYPE_ETHERNET;
        dhcp.hwAddrLen = (byte) MacAddress.MAC_LEN;
        dhcp.hops = 0;
        dhcp.transactionId = Endianess.convert(transactionId);
        dhcp.seconds = 0;
        dhcp.flags = 0;
        dhcp.clientIp = 0;
        // todo check endianness on all ip stuff!
        dhcp.yourIp = clientIp.toInt();
        dhcp.serverIp = serverIp.toInt();
        dhcp.gatewayIp = 0;
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            dhcp.clientHwAddr[i] = clientMac.toBytes()[i];
        }
        dhcp.magicCookie = Endianess.convert(0x63825363); // = fixed value (DHCP)

        //Kernel.out.print((int)dhcp.operation);
        //Kernel.wait(4);

        options.writeTo(MAGIC.addr(dhcp.options[0]));

        return offer;
    }

    public void stop() {
        stack.bindingsManager.unbind(stack.udpLayer, DHCP_SERVER_PORT, this);
    }
}
