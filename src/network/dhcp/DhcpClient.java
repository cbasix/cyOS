package network.dhcp;

import conversions.Endianess;
import io.LowlevelLogging;
import kernel.Kernel;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.dhcp.msg.DhcpMessage;
import network.dhcp.msg.DhcpOption;
import network.dhcp.msg.OptionsReader;
import network.dhcp.msg.DhcpHeader;
import network.ipstack.NetworkStack;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.BindingsManager;
import network.ipstack.binding.PackageReceiver;
import random.PseudoRandom;


public class DhcpClient extends PackageReceiver {

    private final NetworkStack stack;
    private final int interfaceNo;
    private int transactionId;

    public DhcpClient(int interfaceNo){
        this.interfaceNo = interfaceNo;
        stack = Kernel.networkManager.stack;
        stack.bindingsManager.bind(interfaceNo, stack.udpLayer, DhcpServer.DHCP_CLIENT_PORT, this);
        sendDiscovery();
    }

    @Override
    public void receive(int interfaceNo, TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data) {
        DhcpMessage msg = DhcpMessage.fromBytes(data);

        if (msg == null){
            LowlevelLogging.debug("ignoring invalid package");
            return;
        }

        if (msg.getType() == DhcpMessage.TYPE_OFFER) {
            sendRequest(msg);

        } else if (msg.getType() == DhcpMessage.TYPE_ACK) {
            if (msg.getYourIp() == null || msg.getYourIp().equals(IPv4Address.fromString("0.0.0.0"))){
                LowlevelLogging.debug("no ip assigned! ");
                return;
            }
            if (msg.getYourIp().getNetmaskCidr() == 0){
                LowlevelLogging.debug("no netmask assigned! ");
                return;
            }

            if (msg.getGateway() == null || msg.getGateway().equals(IPv4Address.fromString("0.0.0.0"))){
                LowlevelLogging.debug("no gateway assigned! ");
            } else {
                Kernel.networkManager.setDefaultGateway(msg.getGateway());
            }

            if (msg.getDnsserver() == null || msg.getDnsserver().equals(IPv4Address.fromString("0.0.0.0"))){
                LowlevelLogging.debug("no dnsserver assigned! ");
            } else {
                stack.setDnsServer(msg.getDnsserver());
            }

            // add the new got ip !
            Kernel.networkManager.getInterface(interfaceNo).addAddress(msg.getYourIp());

            LowlevelLogging.debug(String.concat("We got an ip! ", msg.getYourIp().toString()));

            stop();
            // todo maybe some kind of timeout?
            // todo implement save dns server
        } else {
            LowlevelLogging.debug("Got dhcp msg of type: ", String.from(msg.getType()));
        }
    }

    public void stop(){
        stack.bindingsManager.unbind(BindingsManager.ALL_INTERFACES, stack.udpLayer, DhcpServer.DHCP_CLIENT_PORT, this);
    }

    private void sendDiscovery(){
        transactionId = PseudoRandom.getRandInt();
        MacAddress myMac = Kernel.networkManager.getInterface(interfaceNo).nic.getMacAddress();
        //byte[] msg = DhcpServer.buildMessage(
        //        transactionId, new IPv4Address(0), new IPv4Address(0), new IPv4Address(0), new IPv4Address(0), myMac, DhcpOption.MSG_TYPE_DISCOVER);

        DhcpMessage msg = new DhcpMessage()
                .setType(DhcpMessage.TYPE_DISCOVER)
                .setTransactionId(transactionId)
                .setClientHwAddr(myMac);

        stack.udpLayer.send(interfaceNo, IPv4Address.getGlobalBreadcastAddr(), DhcpServer.DHCP_CLIENT_PORT, DhcpServer.DHCP_SERVER_PORT, msg.toBytes());

    }

    public void sendRequest(DhcpMessage msg){
        //LowlevelLogging.debug("sendRequest     ");
        MacAddress myMac = Kernel.networkManager.getInterface(interfaceNo).nic.getMacAddress();
        //byte[] msg = DhcpServer.buildMessage(
        //        transactionId, new IPv4Address(0), new IPv4Address(0), new IPv4Address(0), myIp, myMac, DhcpOption.MSG_TYPE_REQUEST);
        if (msg.getTransactionId() == transactionId) {

            msg.setType(DhcpMessage.TYPE_REQUEST)
                    .setTransactionId(transactionId);

            stack.udpLayer.send(interfaceNo, IPv4Address.getGlobalBreadcastAddr(), DhcpServer.DHCP_CLIENT_PORT, DhcpServer.DHCP_SERVER_PORT, msg.toBytes());
            //LowlevelLogging.debug(" done sendRequest");
        }
    }
}
