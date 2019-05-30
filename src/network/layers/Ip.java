package network.layers;

import conversions.Endianess;
import datastructs.ArrayList;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import kernel.Kernel;
import network.IPv4Address;
import network.MacAddress;
import network.PackageBuffer;
import network.checksum.Crc32;
import network.layers.abstracts.LinkLayer;
import network.layers.abstracts.ResolutionLayer;
import network.layers.abstracts.TransportLayer;
import network.structs.IpHeader;

public class Ip extends InternetLayer {
    public static final int LOKAL_ADDR = 0x0E000000;
    private static final byte V4 = 0x40;

    private static final byte PROTO_UDP = 0x11;
    public static final byte PROTO_TCP = 0x06;
    public static final byte PROTO_ICMP = 0x01;
    public static final byte PROTO_RAW_TEXT = (byte)0xF2;

    private LinkLayer ethernetLayer = null;
    private ResolutionLayer arpLayer = null;
    private TransportLayer udpLayer = null;

    public ArrayList ownAddresses;

    public Ip(){
        ownAddresses = new ArrayList();
        ownAddresses._add(new IPv4Address(LOKAL_ADDR | 1)); // link local 127.0.0.1
    }

    public void setArpLayer(ResolutionLayer arpLayer) {
        this.arpLayer = arpLayer;
    }

    public void setEthernetLayer(LinkLayer ethernetLayer) {
        this.ethernetLayer = ethernetLayer;
    }

    public void setUdpLayer(TransportLayer udpLayer) {
        this.udpLayer = udpLayer;
    }

    public void addAddress(IPv4Address ip){
        ownAddresses._add(ip);
    }

    public PackageBuffer getBuffer(int payloadSize) {
        PackageBuffer buffer = ethernetLayer.getBuffer(payloadSize + IpHeader.SIZE);

        if (buffer.usableSize != payloadSize + IpHeader.SIZE){
            LowlevelLogging.debug("Ethernet layer betrayed us, wrong size");
        }
        // upper layer protocols may only use the payload segment part-> for them the size is smaller
        buffer.start += IpHeader.SIZE;
        buffer.usableSize -= IpHeader.SIZE;
        return buffer;
    }

    @Override
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
        header.len = Endianess.convert((short)(buffer.usableSize));
        header.chksum = Endianess.convert((short) Crc32.calc(0, (int)MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));

        // handle link local
        for (int addrNo = 0; addrNo < ownAddresses.size(); addrNo++){
            if (targetIp.equals((IPv4Address) ownAddresses._get(addrNo))){
                this.receive(buffer);
                return;
            }
        }

        // wait for arp to resolve ip...
        MacAddress targetMac = arpLayer.resolveIp(targetIp);

        if (targetMac == null){
            // simple timeout
            LowlevelLogging.debug("IP could not be resolved!");
            return;
        }


        ethernetLayer.send(targetMac, Ethernet.TYPE_IP, buffer);
    }

    @Override
    public void receive(PackageBuffer buffer) {
        // todo implement
        GreenScreenOutput out = Kernel.out;
        out.setCursor(30, 15);
        out.setColor(Color.GREY, Color.PINK);

        IpHeader header = (IpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        byte version = (byte) (header.versionIhl & 0xF0);
        header.tos = 0;
        IPv4Address targetIp = new IPv4Address(Endianess.convert(header.dstIP));
        IPv4Address sourceIp = new IPv4Address(Endianess.convert(header.srcIP));
        byte protocol = header.prot;
        short chksum = Endianess.convert(header.chksum);
        int len = Endianess.convert(header.len) - IpHeader.SIZE; // todo IPv4 has variable header length!

        // todo check checksum
        //  Endianess.convert((short) Crc32.calc(0, (int)MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));

        if (!this.hasOwnAddress(targetIp)){
            return;
        }
        
        if (version != Ip.V4){
            return;
        }

        // upper layer protocol usable area restrictions
        buffer.start += IpHeader.SIZE;
        buffer.usableSize -= IpHeader.SIZE;
        
        if(protocol == PROTO_RAW_TEXT){

            // todo condinue here
            if (len != buffer.usableSize) {
                out.print("DIFFERING length:\n buffer usable size: ");
                out.print(String.from(buffer.usableSize));
                out.print(" ip len: ");
                out.println(len);
            }

            Kernel.wait(10);

            char[] messageArr = new char[buffer.usableSize/2];
            for (int i = 0; i < messageArr.length; i++){
                messageArr[i] = (char)((buffer.data[buffer.start + i] << 8) | buffer.data[buffer.start + i + 1]);
            }

            out.print("Got message: ");
            out.print(new String(messageArr));
            out.print(" length: ");
            out.print(messageArr.length);
            out.print(" from: ");
            out.print(sourceIp.toString());
            Kernel.wait(10);
        }
    }

    // todo for now the last one added is always the best match -> can be replaced by net mask stuff
    public IPv4Address myBestMatchingIpFor(IPv4Address ip) {
        return (IPv4Address) ownAddresses._get(ownAddresses.size()-1);
        /*for (int i = 0; i < myAddresses.size(); i++){
            (IPv4Address) myAddresses._get(i)
        }*/
    }

    public boolean hasOwnAddress(IPv4Address ip){
        for (int i = 0; i < ownAddresses.size(); i++){
            if (ip.toInt() == ((IPv4Address) ownAddresses._get(i)).toInt()){
                return true;
            }
        }
        return  false;
    }
}
