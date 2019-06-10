package network.ipstack;

import conversions.Endianess;
import datastructs.ArrayList;
import io.LowlevelLogging;
import kernel.Kernel;
import network.*;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.dhcp.msg.IPv4AddrStruct;
import network.ipstack.abstracts.ResolutionLayer;
import network.ipstack.structs.ArpMessage;

public class Arp extends ResolutionLayer {
    public ArpCache cache;

    public Arp(){
        cache = new ArpCache();
    }

    @Override
    public void anounce() {
        NetworkStack stack = Kernel.networkManager.stack;
        ArrayList interfaces = Kernel.networkManager.getInterfaces();

        for(int interfaceNo = 0; interfaceNo < interfaces.size(); interfaceNo++) {
            Interface intf = (Interface) interfaces._get(interfaceNo);

            for (int i = 0; i < intf.ownAddresses.size(); i++) {
                IPv4Address curAddr = (IPv4Address) intf.ownAddresses._get(i);
                if (curAddr.toBytes()[0] != Ip.LOKAL_ADDR_FIRST_BYTE) {
                    sendReply(interfaceNo, curAddr, curAddr, true);
                }
            }
        }
    }

    @Override
    public MacAddress resolveIp(IPv4Address ip){
        if (ip.equals(IPv4Address.getGlobalBreadcastAddr()) ){  // todo network specific broadcasts
            return MacAddress.getBroadcastAddr();
        }

        NetworkStack stack = Kernel.networkManager.stack;
        int interfaceNo = Kernel.networkManager.getInterfaceNoForTarget(ip);
        if (interfaceNo == -1){
            LowlevelLogging.debug("arp no route to: ", ip.toString());
            return null;
        }
        MacAddress targetMac = cache.getMac(ip);

        // if not in cache target Mac is null here
        int tries = 2;
        while (targetMac == null && tries --> 0){

            sendRequest(interfaceNo, Kernel.networkManager.getInterface(interfaceNo).getLocalIpForTarget(ip), ip);
            for (int i = 0; i < 2; i++) {Kernel.sleep();} // short delay untill next system timer
            Kernel.networkManager.receive();
            targetMac = cache.getMac(ip);
        }

        return targetMac;
    }

    // todo check endianness

    public void sendRequest(int interfaceNo, IPv4Address fromIp, IPv4Address toIp){
        //LowlevelLogging.debug("requesting mac for ip: ");
        //LowlevelLogging.debug(toIp.toString());

        PackageBuffer buffer = Kernel.networkManager.stack.ethernetLayer.getBuffer(ArpMessage.SIZE);
        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        a.hardwareType = Endianess.convert(ArpMessage.HW_TYPE_ETHERNET);
        a.protocolType = Endianess.convert(ArpMessage.PROTOC_TYPE_IPv4);
        a.hwAddrLen = (byte)MacAddress.MAC_LEN;
        a.protocAddrLen = (byte)IPv4Address.IPV4_LEN;
        a.operation = Endianess.convert(ArpMessage.OP_REQUEST);

        MacAddress senderMac = Kernel.networkManager.getInterface(interfaceNo).nic.getMacAddress();
        senderMac.writeTo(a.senderHwAddr);

        fromIp.writeTo(a.senderProtocAddr);

        MacAddress targetMac = MacAddress.zeroAddr();
        targetMac.writeTo(a.targetHwAddr);

        toIp.writeTo(a.targetProtocolAddr);

        Kernel.networkManager.stack.ethernetLayer.send(interfaceNo, MacAddress.getBroadcastAddr(), Ethernet.TYPE_ARP, buffer);
    }

    public void sendReply(int interfaceNo, IPv4Address fromIp, IPv4Address toIp, boolean gratuitous){
        PackageBuffer buffer = Kernel.networkManager.stack.ethernetLayer.getBuffer(ArpMessage.SIZE);
        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        a.hardwareType = Endianess.convert(ArpMessage.HW_TYPE_ETHERNET);
        a.protocolType = Endianess.convert(ArpMessage.PROTOC_TYPE_IPv4);
        a.hwAddrLen = (byte)MacAddress.MAC_LEN;
        a.protocAddrLen = (byte)IPv4Address.IPV4_LEN;
        a.operation = Endianess.convert(ArpMessage.OP_REPLY);

        MacAddress senderMac = Kernel.networkManager.getInterface(interfaceNo).nic.getMacAddress();
        senderMac.writeTo(a.senderHwAddr);

        fromIp.writeTo(a.senderProtocAddr);

        MacAddress sendToMac = null;
        if (gratuitous){
            sendToMac = MacAddress.getBroadcastAddr();
        } else {
            sendToMac = resolveIp(toIp);
        }

        sendToMac.writeTo(a.targetHwAddr);


        toIp.writeTo(a.targetProtocolAddr);

        Kernel.networkManager.stack.ethernetLayer.send(interfaceNo,
                sendToMac, Ethernet.TYPE_ARP, buffer);
    }

    @Override
    public void receive(int interfaceNo, PackageBuffer buffer){
        Ip ipLayer = Kernel.networkManager.stack.ipLayer;

        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        // information from both REPLY or REQUEST are saved in cache

        IPv4Address senderIp = IPv4Address.fromStruct(a.senderProtocAddr);

        // update or create cache entry
        MacAddress senderMac = MacAddress.fromStruct(a.senderHwAddr);
        cache.put(senderMac, senderIp);

        // reply to request
        IPv4Address targetIp = IPv4Address.fromStruct(a.targetProtocolAddr);
        short operation = Endianess.convert(a.operation);

        if (operation == ArpMessage.OP_REQUEST){
            Interface intf = Kernel.networkManager.getInterface(interfaceNo);
            if (intf.hasIp(targetIp)){
                sendReply(interfaceNo, intf.getLocalIpForTarget(targetIp), senderIp, false);
            }
        }
    }
}
