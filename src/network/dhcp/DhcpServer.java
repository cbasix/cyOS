package network.dhcp;

import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.dhcp.msg.DhcpMessage;
import network.dhcp.msg.DhcpOption;
import network.ipstack.NetworkStack;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.BindingsManager;
import network.ipstack.binding.PackageReceiver;
import random.PseudoRandom;

public class DhcpServer extends PackageReceiver {
    public static final int DHCP_SERVER_PORT = 67;
    public static final int DHCP_CLIENT_PORT = 68;
    public static final byte TYPE_ETHERNET = 1;

    private final NetworkStack stack;
    private DhcpCache cache;

    public DhcpServer () {
        stack = Kernel.networkManager.stack;
        cache = new DhcpCache();

    }

    public void startListenOn(IPv4Address ip){
        int interfaceNo = Kernel.networkManager.getInterfaceNoForTarget(ip);
        if (interfaceNo == BindingsManager.UNSET_INTERFACE){
            LowlevelLogging.debug("No interface for given ip: ", ip.toString(), " starting on interface 0");
            interfaceNo = 0;
        }

        stack.bindingsManager.bind(interfaceNo, stack.udpLayer, DHCP_SERVER_PORT, this);
    }

    @Override
    public void receive(int interfaceNo, TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data) {
        //LowlevelLogging.debug("Dhcp Server got a package!");

        DhcpMessage msg = DhcpMessage.fromBytes(data);

        if (msg == null){
            LowlevelLogging.debug("ignoring invalid package");
            return;
        }

        if (msg.getType() == DhcpMessage.TYPE_DISCOVER) {
            handleDiscovery(interfaceNo, msg);

        } else if (msg.getType() == DhcpOption.MSG_TYPE_REQUEST){
            handleRequest(interfaceNo, msg);
        }

        // todo release not implemented
    }

    public void handleDiscovery(int interfaceNo, DhcpMessage msg) {

        IPv4Address myIp = Kernel.networkManager.getInterface(0).getDefaultIp();
        IPv4Address clientIP = findClientIp(msg, myIp).setNetmaskCidr(myIp.getNetmaskCidr());

        //byte[] msg = null; //buildMessage(transactionId, myIp, stack.getDnsServer(), Kernel.networkManager.getDefaultGateway(), clientIP, clientMac, DhcpOption.MSG_TYPE_OFFER);
        msg.setType(DhcpMessage.TYPE_OFFER);
        msg.setYourIp(clientIP);
        msg.setServerIp(myIp);
        msg.setDnsserver(null);
        msg.setGateway(null);

        stack.udpLayer.send(IPv4Address.getGlobalBreadcastAddr(), DHCP_SERVER_PORT, DHCP_CLIENT_PORT, msg.toBytes());
        //LowlevelLogging.debug("Discovery answered");
    }

    // generate random ip based on my own default ip, if no or non valid ip requested
    public IPv4Address findClientIp(DhcpMessage msg, IPv4Address myIp) {
        IPv4Address clientIP = msg.getRequestedIp();

        if (clientIP == null){
            clientIP = IPv4Address.fromString("0.0.0.0");
        }

        while (cache.getStatus(clientIP) != DhcpCache.AVAILABLE || !clientIP.isInSameNetwork(myIp)) {
           byte[] addr = clientIP.toBytes();
           byte[] myAddr = myIp.toBytes();

           for (int i = 0; i < IPv4Address.IPV4_LEN - 1; i++){
               addr[i] = myAddr[i];
           }
           addr[IPv4Address.IPV4_LEN - 1] = (byte)(PseudoRandom.getRandInt() & 0xFE);
           clientIP = new IPv4Address(addr);
        }

        //LowlevelLogging.debug("found ip: ", clientIP.toString());

        return clientIP;
    }

    private void handleRequest(int interfaceNo, DhcpMessage msg) {
        // todo checks...
        IPv4Address requestedIp = msg.getYourIp();
        IPv4Address myIp = Kernel.networkManager.getInterface(interfaceNo).getDefaultIp();

        if(requestedIp == null){
            LowlevelLogging.debug("req ip was null");
        }

        if(!requestedIp.isInSameNetwork(myIp)){
            LowlevelLogging.debug("is not in same net as my ip ", requestedIp.toString(), myIp.toString());
        }


        if(requestedIp == null || !requestedIp.isInSameNetwork(myIp) || cache.getStatus(requestedIp) != DhcpCache.AVAILABLE){
            msg.setType(DhcpMessage.TYPE_NO_ACK);
        } else {
            msg.setType(DhcpMessage.TYPE_ACK);
        }

        msg.setYourIp(requestedIp);
        msg.setServerIp(myIp);
        //msg.setDnsserver(stack.getDnsServer());
        msg.setDnsserver(myIp);
        //msg.setGateway(Kernel.networkManager.getDefaultGateway());
        msg.setGateway(myIp);

        cache.setStatus(requestedIp, DhcpCache.IN_USE);

        //byte[] msg = null; //buildMessage(transactionId, serverIp, stack.getDnsServer(), Kernel.networkManager.getDefaultGateway(), requestedIp, clientMac, DhcpOption.MSG_TYPE_ACK);
        stack.udpLayer.send(IPv4Address.getGlobalBreadcastAddr(), DHCP_SERVER_PORT, DHCP_CLIENT_PORT, msg.toBytes());

        //LowlevelLogging.debug("Request answered");
        //LowlevelLogging.debug(requestedIp.toString());
    }



    public void stop() {
        stack.bindingsManager.unbind(BindingsManager.ALL_INTERFACES, stack.udpLayer, DHCP_SERVER_PORT, this);
    }
}
