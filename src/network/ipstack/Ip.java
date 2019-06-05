package network.ipstack;

import conversions.Endianess;
import datastructs.ArrayList;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.PackageBuffer;
import network.checksum.MyCrc32;
import network.checksum.OnesComplement;
import network.ipstack.abstracts.InternetLayer;
import network.ipstack.abstracts.LinkLayer;
import network.ipstack.abstracts.ResolutionLayer;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.structs.IpHeader;

public class Ip extends InternetLayer {
    public static final int LOKAL_ADDR = 0x7F000000;
    public static final int ZERO_ADDR = 0x00000000;
    private static final byte V4 = 0x40;

    public static final byte PROTO_UDP = 0x11;
    public static final byte PROTO_TCP = 0x06;
    public static final byte PROTO_ICMP = 0x01;
    public static final byte PROTO_RAW_TEXT = (byte)0xF2;

    private LinkLayer ethernetLayer = null;
    private ResolutionLayer arpLayer = null;
    private TransportLayer udpLayer = null;

    public ArrayList ownAddresses;
    private IPv4Address defaultGateway;

    public Ip(){
        ownAddresses = new ArrayList();
        //ownAddresses._add(new IPv4Address(ZERO_ADDR)); // placeholder 0.0.0.0 is used as sender addr in broadcast
        ownAddresses._add(new IPv4Address(LOKAL_ADDR | 1).setNetmaskCidr(8)); // link local 127.0.0.1

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
        if (ip.netmask == 0){
            LowlevelLogging.debug("trying to add own ip without netmask");
            return;
        }
        ownAddresses._add(ip);
    }

    public void setDefaultGateway(IPv4Address ip){
        defaultGateway = ip;
    }

    public ArrayList getAddresses(){
        return ownAddresses;
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

        boolean forwardToGateway = false;

        IPv4Address myIp = getMatchingOwnIpFor(targetIp);
        //LowlevelLogging.debug(String.concat("myIP: ", myIp == null ? "null" : myIp.toString()));

        // if target network is not directly reachable -> send to default gateway
        if (myIp == null){
            forwardToGateway = true;
            myIp = getMatchingOwnIpFor(defaultGateway);

            // default gateway not reachable directly
            if (myIp == null){
                LowlevelLogging.debug("Default gateway not in local net! ");
                return;
            }
        }


        IpHeader header = (IpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        // todo check values
        header.versionIhl = 0x45;
        header.tos = 0;
        header.flagsFrag = Endianess.convert((short)0x4000);
        header.dstIP = Endianess.convert(targetIp.toInt());
        header.srcIP = Endianess.convert(myIp.toInt());
        header.ttl = (byte)64; //standard ttl is 64 (see RFC 1700)
        header.prot = (byte)protocol;
        header.len = Endianess.convert((short)(buffer.usableSize));
        header.chksum = Endianess.convert((short) OnesComplement.calc(0, MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));

        // handle link local
        for (int addrNo = 0; addrNo < ownAddresses.size(); addrNo++){
            if (targetIp.equals((IPv4Address) ownAddresses._get(addrNo))){
                this.receive(buffer);
                return;
            }
        }

        // wait for arp to resolve ip...
        MacAddress targetMac = arpLayer.resolveIp(forwardToGateway ? defaultGateway : targetIp);

        if (targetMac == null){
            // simple timeout
            LowlevelOutput.printStr("IP could not be resolved!", 0,0, Color.PINK);
            return;
        }


        ethernetLayer.send(targetMac, Ethernet.TYPE_IP, buffer);
    }

    @Override
    public void receive(PackageBuffer buffer) {
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

        if (!this.hasOwnAddress(targetIp) && targetIp.toInt() != 0xFFFFFFFF){
            LowlevelLogging.debug("got package with for other ip");
            return;
        }
        
        if (version != Ip.V4){
            return;
        }

        if (len > buffer.usableSize) {
            LowlevelLogging.debug("Dropped invalid package: ip len > package len");
            return;
        }

        // upper layer protocol usable area restrictions
        buffer.start += IpHeader.SIZE;
        buffer.usableSize -= IpHeader.SIZE;

        // simple net send style "popup message" for debugging
        if(protocol == PROTO_RAW_TEXT){

            char[] messageArr = new char[len/2];
            for (int i = 0; i < messageArr.length; i++){
                messageArr[i] = (char)((buffer.data[buffer.start + 2*i] << 8) | buffer.data[buffer.start + 2*i + 1]);
            }

            GreenScreenOutput out = Kernel.out;
            out.setCursor(5, 21);
            out.setColor(Color.GREY, Color.PINK);

            out.print("Got message: ");
            out.print(new String(messageArr));
            out.print(" length: ");
            out.print(messageArr.length);
            out.print(" from: ");
            out.print(sourceIp.toString());

        } else if (protocol == PROTO_UDP){
            udpLayer.receive(sourceIp, buffer);
        }
    }

    // todo for now the last one added is always the best match -> can be replaced by net mask stuff
    public IPv4Address getMatchingOwnIpFor(IPv4Address ip) {

        //return getDefaultIp();
        for (int i = 0; i < ownAddresses.size(); i++){
            IPv4Address myAddr = (IPv4Address) ownAddresses._get(i);
            if (myAddr.isInSameNetwork(ip)){
                return myAddr;
            }
        }

        // no ip found and target is broadcast -> send with "undefined" ip as sender
        if (IPv4Address.getGlobalBreadcastAddr().equals(ip)){
            return new IPv4Address(0x00000000);
        }

        return null;
    }

    /** last one added is default */
    public IPv4Address getDefaultIp(){
        return (IPv4Address) ownAddresses._get(ownAddresses.size()-1);
    }

    public boolean hasOwnAddress(IPv4Address ip){
        for (int i = 0; i < ownAddresses.size(); i++){
            if (ip.toInt() == ((IPv4Address) ownAddresses._get(i)).toInt()){
                return true;
            }
        }
        return  false;
    }

    public void removeAddress(IPv4Address ip) {
        // we need the object from the ownAdd List for the == comparation in "remove"
        IPv4Address o = getMatchingOwnIpFor(ip);
        this.ownAddresses.remove(o);

    }
}
