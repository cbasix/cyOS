package network.layers;

import conversions.Endianess;
import datastructs.ArrayList;
import io.LowlevelLogging;
import kernel.Kernel;
import network.IPv4Address;
import network.MacAddress;
import network.PackageBuffer;
import network.checksum.Crc32;
import network.structs.IpHeader;

public class Ip {
    public static final int LOKAL_ADDR = 0x0E000000;
    //public static final int PROTO_UDP = ;
    //public static final int PROTO_TCP = ;
    //public static final int PROTO_ICMP = ;

    public ArrayList myAddresses;

    public Ip(){
        myAddresses = new ArrayList();
        myAddresses._add(new IPv4Address(LOKAL_ADDR | 1)); // link local 127.0.0.1
    }

    public void addAddress(IPv4Address ip){
        myAddresses._add(ip);
    }

    public PackageBuffer getBuffer(int payloadSize) {
        PackageBuffer buffer = Kernel.networkManager.stack.ethernetLayer.getBuffer(payloadSize + IpHeader.SIZE);
        // upper layer protocols may only use the payload segment part-> for them the size is smaller
        buffer.start += IpHeader.SIZE;
        buffer.usableSize -= IpHeader.SIZE;
        return buffer;
    }

    public void send(IPv4Address targetIp, int protocol, PackageBuffer buffer){
        // remove upper layer protocols restrictions -> we may use our own layers header
        buffer.start -= IpHeader.SIZE;
        buffer.usableSize += IpHeader.SIZE;


        IpHeader header = (IpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        // todo check values
        header.versionIhl = 0x45;
        header.tos = 0;
        header.flagsFrag = Endianess.convert((short)0x4000);
        header.dstIP = Endianess.convert(targetIp.toInt());
        header.srcIP = Endianess.convert((myBestMatchingIpFor(targetIp).toInt())); // todo netmasks
        header.ttl = (byte)64; //standard ttl is 64 (see RFC 1700)
        header.prot = (byte)protocol;
        header.chksum = 0;
        header.len = Endianess.convert((short)(buffer.usableSize + IpHeader.SIZE));
        header.chksum = Endianess.convert((short) Crc32.calc(0, (int)MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));

        // handle link local
        for (int addrNo = 0; addrNo < myAddresses.size(); addrNo++){
            if (targetIp.equals((IPv4Address) myAddresses._get(addrNo))){
                this.receive(buffer);
                return;
            }
        }

        // wait for arp to resolve ip...
        MacAddress targetMac = Kernel.networkManager.stack.arpLayer.resolveIp(targetIp);

        if (targetMac == null){
            // simple timeout
            LowlevelLogging.debug("IP could not be resolved!");
        }


        Kernel.networkManager.stack.ethernetLayer.send(targetMac, Ethernet.TYPE_IP, buffer);
    }

    public void receive(PackageBuffer buffer) {
        // todo implement
    }

    // todo for now the last one added is always the best match -> may be replaced by net mask stuff
    public IPv4Address myBestMatchingIpFor(IPv4Address ip) {
        return (IPv4Address) myAddresses._get(myAddresses.size()-1);
        /*for (int i = 0; i < myAddresses.size(); i++){
            (IPv4Address) myAddresses._get(i)
        }*/
    }
}
