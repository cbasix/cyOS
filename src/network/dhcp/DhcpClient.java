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


public class DhcpClient extends PackageReceiver {

    private final NetworkStack stack;

    public DhcpClient(){
        stack = Kernel.networkManager.stack;
        stack.bindingsManager.bind(stack.udpLayer, DhcpServer.DHCP_CLIENT_PORT, this);
        sendDiscovery();
    }

    @Override
    public void receive(TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data) {
        if (data.length < DhcpHeader.SIZE){
            LowlevelLogging.debug("Invalid Dhcp Package: too short");
        }

        DhcpHeader dhcp = (DhcpHeader) MAGIC.cast2Struct(MAGIC.addr(data[0]));
        OptionsReader options = new OptionsReader(MAGIC.addr(dhcp.options[0]), data.length - DhcpHeader.SIZE);

        byte[] typeArr = options.getOptionValue(DhcpOption.OPT_MSG_TYPE);
        if (typeArr == null || typeArr.length != 1){
            LowlevelLogging.debug(String.concat(String.from(typeArr), "not a dhcp packet"));
            return;
        }
        byte type = typeArr[0];
        byte operation = dhcp.operation;


        if (type == DhcpOption.MSG_TYPE_OFFER) {
            sendRequest(dhcp.transactionId, new IPv4Address(dhcp.yourIp));
            LowlevelLogging.debug("send offer");

        } else if (type == DhcpOption.MSG_TYPE_ACK) {
            byte[] netmask = options.getOptionValue(DhcpOption.OPT_SUBNET_MASK);
            if (netmask == null){
                LowlevelLogging.debug("no netmask! ");
                return;
            }

            byte[] gateway = options.getOptionValue(DhcpOption.OPT_ROUTER);
            if (gateway != null){
                IPv4Address gw = new IPv4Address(gateway);
                LowlevelLogging.debug("Got gateway: ", gw.toString());
                stack.ipLayer.setDefaultGateway(gw);
            }

            byte[] dnsserver = options.getOptionValue(DhcpOption.OPT_DNS_SERVERS);
            if (dnsserver != null){
                IPv4Address dns = new IPv4Address(dnsserver);
                LowlevelLogging.debug("Got dnsserver: ", dns.toString());
                stack.setDnsServer(dns);
            }

            // add the new got ip !
            IPv4Address myNewIp = new IPv4Address(Endianess.convert(dhcp.yourIp)).setNetmask(netmask);
            stack.ipLayer.addAddress(myNewIp);

            LowlevelLogging.debug(String.concat("We got an ip! ", myNewIp.toString()));

            stop();
            // todo maybe some kind of timeout?
            // todo implement save dns server
        } else {
            LowlevelLogging.debug("Got dhcp msg of type: ", String.from(type));
        }
    }

    public void stop(){
        stack.bindingsManager.unbind(stack.udpLayer, DhcpServer.DHCP_CLIENT_PORT, this);
    }

    private void sendDiscovery(){
        int transactionId = PseudoRandom.getRandInt();
        MacAddress myMac = Kernel.networkManager.nic.getMacAddress();
        byte[] msg = DhcpServer.buildMessage(
                transactionId, new IPv4Address(0), new IPv4Address(0), new IPv4Address(0), new IPv4Address(0), myMac, DhcpOption.MSG_TYPE_DISCOVER);
        stack.udpLayer.send(IPv4Address.getGlobalBreadcastAddr(), DhcpServer.DHCP_CLIENT_PORT, DhcpServer.DHCP_SERVER_PORT, msg);

    }

    public void sendRequest(int transactionId, IPv4Address myIp){
        //LowlevelLogging.debug("sendRequest     ");
        MacAddress myMac = Kernel.networkManager.nic.getMacAddress();
        byte[] msg = DhcpServer.buildMessage(
                transactionId, new IPv4Address(0), new IPv4Address(0), new IPv4Address(0), myIp, myMac, DhcpOption.MSG_TYPE_REQUEST);
        stack.udpLayer.send(IPv4Address.getGlobalBreadcastAddr(), DhcpServer.DHCP_CLIENT_PORT, DhcpServer.DHCP_SERVER_PORT, msg);
        //LowlevelLogging.debug(" done sendRequest");
    }
}
