package network.ipstack;

import conversions.Endianess;
import kernel.Kernel;
import network.*;
import network.address.IPv4Address;
import network.address.MacAddress;
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

        for (int i = 0; i < stack.ipLayer.ownAddresses.size(); i++){
            IPv4Address curAddr = (IPv4Address) stack.ipLayer.ownAddresses._get(i);
            if ((curAddr.toInt() & 0xFF000000) != Ip.LOKAL_ADDR)
            sendRequest(curAddr, curAddr);
        }
    }

    @Override
    public MacAddress resolveIp(IPv4Address ip){
        if (ip.equals(IPv4Address.getGlobalBreadcastAddr()) ){  // todo network specific broadcasts
            return MacAddress.getBroadcastAddr();
        }

        NetworkStack stack = Kernel.networkManager.stack;
        MacAddress targetMac = cache.getMac(ip);

        // if not in cache target Mac is null here
        int tries = 2;
        while (targetMac == null && tries --> 0){

            sendRequest(stack.ipLayer.getMatchingOwnIpFor(ip), ip);
            for (int i = 0; i < 2; i++) {Kernel.sleep();} // short delay untill next system timer
            byte[] data = Kernel.networkManager.nic.receive();
            if (data != null){
                stack.ethernetLayer.receive(data);
            }
            targetMac = cache.getMac(ip);
        }

        return targetMac;
    }

    // todo check endianness

    public void sendRequest(IPv4Address fromIp, IPv4Address toIp){
        //LowlevelLogging.debug("requesting mac for ip: ");
        //LowlevelLogging.debug(toIp.toString());

        PackageBuffer buffer = Kernel.networkManager.stack.ethernetLayer.getBuffer(ArpMessage.SIZE);
        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        a.hardwareType = Endianess.convert(ArpMessage.HW_TYPE_ETHERNET);
        a.protocolType = Endianess.convert(ArpMessage.PROTOC_TYPE_IPv4);
        a.hwAddrLen = (byte)MacAddress.MAC_LEN;
        a.protocAddrLen = (byte)IPv4Address.IPV4_LEN;
        a.operation = Endianess.convert(ArpMessage.OP_REQUEST);

        byte[] b = Kernel.networkManager.nic.getMacAddress().toBytes();
        for (int i = 0; i < MacAddress.MAC_LEN; i++) {
            a.senderHwAddr[i] = b[i];
        }

        a.senderProtocAddr = Endianess.convert(fromIp.toInt());

        for (int i = 0; i < MacAddress.MAC_LEN; i++) {
            a.targetHwAddr[i] = 0;
        }

        a.targetProtocolAddr = Endianess.convert(toIp.toInt());

        Kernel.networkManager.stack.ethernetLayer.send(MacAddress.getBroadcastAddr(), Ethernet.TYPE_ARP, buffer);
    }

    public void sendReply(IPv4Address fromIp, IPv4Address toIp){
        PackageBuffer buffer = Kernel.networkManager.stack.ethernetLayer.getBuffer(ArpMessage.SIZE);
        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        a.hardwareType = Endianess.convert(ArpMessage.HW_TYPE_ETHERNET);
        a.protocolType = Endianess.convert(ArpMessage.PROTOC_TYPE_IPv4);
        a.hwAddrLen = (byte)MacAddress.MAC_LEN;
        a.protocAddrLen = (byte)IPv4Address.IPV4_LEN;
        a.operation = Endianess.convert(ArpMessage.OP_REPLY);

        byte[] b = Kernel.networkManager.nic.getMacAddress().toBytes();
        for (int i = 0; i < MacAddress.MAC_LEN; i++) {
            a.senderHwAddr[i] = b[i];
        }

        a.senderProtocAddr = Endianess.convert(fromIp.toInt());

        MacAddress sendToMac = resolveIp(toIp);
        b = sendToMac.toBytes();
        for (int i = 0; i < MacAddress.MAC_LEN; i++) {
            a.targetHwAddr[i] = b[i];
        }

        a.targetProtocolAddr = Endianess.convert(toIp.toInt());

        Kernel.networkManager.stack.ethernetLayer.send(
                sendToMac, Ethernet.TYPE_ARP, buffer);
    }

    @Override
    public void receive(PackageBuffer buffer){
        Ip ipLayer = Kernel.networkManager.stack.ipLayer;

        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        // information from both REPLY or REQUEST are saved in cache

        IPv4Address senderIp = new IPv4Address(Endianess.convert(a.senderProtocAddr));

        // update or create cache entry
        byte[] senderMac = new byte[MacAddress.MAC_LEN];
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            senderMac[i] = a.senderHwAddr[i];
        }
        cache.put(MacAddress.fromBytes(senderMac), senderIp);

        // reply to request
        int targetIp = Endianess.convert(a.targetProtocolAddr);
        short operation = Endianess.convert(a.operation);

        if (operation == ArpMessage.OP_REQUEST){
            for (int addrNo = 0; addrNo < ipLayer.ownAddresses.size(); addrNo++) {
                IPv4Address oneOfMyOwnIps = (IPv4Address) ipLayer.ownAddresses._get(addrNo);
                if(targetIp == oneOfMyOwnIps.toInt()){
                    //LowlevelLogging.debug("someone asked for my ip! replying");
                    //LowlevelLogging.debug(oneOfMyOwnIps.toString());
                    sendReply(oneOfMyOwnIps, senderIp);
                }
            }
        }
    }
}
