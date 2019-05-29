package network.layers;

import conversions.Endianess;
import datastructs.ArrayList;
import io.LowlevelLogging;
import kernel.Kernel;
import network.IPv4Address;
import network.MacAddress;
import network.NetworkStack;
import network.PackageBuffer;
import network.structs.ArpMessage;

public class Arp {
    public ArrayList cache;

    public Arp(){
        cache = new ArrayList();
    }

    public void anounce() {
        NetworkStack stack = Kernel.networkManager.stack;

        for (int i = 0; i < stack.ipLayer.myAddresses.size(); i++){
            IPv4Address curAddr = (IPv4Address) stack.ipLayer.myAddresses._get(i);
            if ((curAddr.toInt() & 0xFF000000) != Ip.LOKAL_ADDR)
            sendRequest(curAddr, curAddr);
        }
    }

    public static class ArpCacheEntry{
        public IPv4Address ip;
        public MacAddress mac;
        public int pendingRequests;
    }

    private MacAddress loadFromCache(IPv4Address ip){
        for (int entryNo = 0; entryNo < cache.size(); entryNo++){
            ArpCacheEntry entry = (ArpCacheEntry) cache._get(entryNo);
            if (entry.ip.toInt() == ip.toInt()){
                // may return null if no arp request completed yet
                return entry.mac;
            }
        }
        return null;
    }

    public MacAddress resolveIp(IPv4Address ip){
        NetworkStack stack = Kernel.networkManager.stack;

        MacAddress targetMac = null;
        int tries = 5;
        while (targetMac == null && tries-- > 0){
            sendRequest(stack.ipLayer.myBestMatchingIpFor(ip), ip);
            for (int i = 0; i < 10; i++) {Kernel.sleep();} // short delay untill next system timer
            byte[] data = Kernel.networkManager.nic.receive();
            if (data != null){
                stack.ethernetLayer.receive(data);
            }
            targetMac = loadFromCache(ip);
        }

        return targetMac;
    }

    // todo check endianness

    public void sendRequest(IPv4Address senderIp, IPv4Address receiverIp){

        ArpCacheEntry entry = new ArpCacheEntry();
        entry.ip = receiverIp;
        entry.mac = null;
        if(loadFromCache(receiverIp) == null) {
            cache._add(entry);
        }

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

        a.senderProtocAddr = Endianess.convert(senderIp.toInt());

        for (int i = 0; i < MacAddress.MAC_LEN; i++) {
            a.targetHwAddr[i] = 0;
        }

        a.targetProtocolAddr = Endianess.convert(receiverIp.toInt());

        Kernel.networkManager.stack.ethernetLayer.send(MacAddress.getBroadcastAddr(), Ethernet.TYPE_ARP, buffer);
    }

    public void sendReply(IPv4Address senderIp, IPv4Address receiverIp){
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

        a.senderProtocAddr = Endianess.convert(senderIp.toInt());

        b = resolveIp(receiverIp).toBytes();
        for (int i = 0; i < MacAddress.MAC_LEN; i++) {
            a.targetHwAddr[i] = b[i];
        }

        a.targetProtocolAddr = Endianess.convert(receiverIp.toInt());

        Kernel.networkManager.stack.ethernetLayer.send(
                MacAddress.getBroadcastAddr(), Ethernet.TYPE_ARP, buffer);
    }

    public void receive(PackageBuffer buffer){
        Ip ipLayer = Kernel.networkManager.stack.ipLayer;


        LowlevelLogging.debug("got arp package");
        ArpMessage a = (ArpMessage) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        // information from both REPLY or REQUEST are saved in cache

        for (int entryNo = 0; entryNo < cache.size(); entryNo++){
            ArpCacheEntry entry = (ArpCacheEntry) cache._get(entryNo);
            if (entry.ip.toInt() == a.senderProtocAddr){
                byte[] mac = new byte[MacAddress.MAC_LEN];
                for (int i = 0; i < MacAddress.MAC_LEN; i++){
                    mac[i] = a.senderHwAddr[i];
                }

                entry.mac = MacAddress.fromBytes(mac);
                return;
            }
        }

        // if no existing entry found
        ArpCacheEntry entry = new ArpCacheEntry();
        entry.ip = new IPv4Address(Endianess.convert(a.senderProtocAddr));

        byte[] mac = new byte[MacAddress.MAC_LEN];
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            mac[i] = a.senderHwAddr[i];
        }

        entry.mac = MacAddress.fromBytes(mac);
        cache._add(entry);

        // todo is sending to my own mac a problem ?

        // reply to request
        int targetIp = Endianess.convert(a.targetProtocolAddr);
        short operation = Endianess.convert(a.operation);

        if (operation == ArpMessage.OP_REQUEST){
            for (int addrNo = 0; addrNo < ipLayer.myAddresses.size(); addrNo++) {
                IPv4Address curIp = (IPv4Address) ipLayer.myAddresses._get(addrNo);
                if(targetIp == curIp.toInt()){
                    sendReply(curIp, entry.ip);
                }
            }
        }
    }
}
