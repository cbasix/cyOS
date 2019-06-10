package network.ipstack;

import conversions.Endianess;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.PackageBuffer;
import network.checksum.OnesComplement;
import network.ipstack.abstracts.InternetLayer;
import network.ipstack.abstracts.LinkLayer;
import network.ipstack.abstracts.ResolutionLayer;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.BindingsManager;
import network.ipstack.structs.IpHeader;

public class Ip extends InternetLayer {
    public static final int LOKAL_ADDR_FIRST_BYTE = 0x7F;
    private static final byte V4 = 0x40;

    public static final byte PROTO_UDP = 0x11;
    public static final byte PROTO_TCP = 0x06;
    public static final byte PROTO_ICMP = 0x01;
    public static final byte PROTO_RAW_TEXT = (byte)0xF2;

    private LinkLayer ethernetLayer = null;
    private ResolutionLayer arpLayer = null;
    private TransportLayer udpLayer = null;

    public Ip(){}

    public void setArpLayer(ResolutionLayer arpLayer) {
        this.arpLayer = arpLayer;
    }

    public void setEthernetLayer(LinkLayer ethernetLayer) {
        this.ethernetLayer = ethernetLayer;
    }

    public void setUdpLayer(TransportLayer udpLayer) {
        this.udpLayer = udpLayer;
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
    public void send(int interfaceNo, IPv4Address targetIp, int protocol, PackageBuffer buffer){
        // remove upper layer protocols restrictions -> we may use our own layers header
        buffer.start -= IpHeader.SIZE;
        buffer.usableSize += IpHeader.SIZE;

        boolean forwardToGateway = false;

        // if no interface set -> get it from the ip destination
        if (interfaceNo == BindingsManager.UNSET_INTERFACE) {
            interfaceNo = Kernel.networkManager.getInterfaceNoForTarget(targetIp);
        }

        IPv4Address myIp;
        // if target network is not directly reachable -> send to default gateway
        if (interfaceNo == BindingsManager.UNSET_INTERFACE){
            forwardToGateway = true;
            IPv4Address gateway = Kernel.networkManager.getDefaultGateway();
            myIp = Kernel.networkManager.getInterfaceForTarget(gateway).getLocalIpForTarget(gateway);
            interfaceNo = Kernel.networkManager.getInterfaceNoForTarget(gateway);

            // default gateway not reachable directly
            if (myIp == null){
                LowlevelLogging.debug("Default gateway not in local net! ");
                return;
            }
        } else {
            // direct reachable

            myIp = Kernel.networkManager.getInterface(interfaceNo).getLocalIpForTarget(targetIp);
            //LowlevelLogging.debug(String.concat("myIP: ", myIp == null ? "null" : myIp.toString()));
        }

        IpHeader header = (IpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        // todo check values
        header.versionIhl = 0x45;
        header.tos = 0;
        header.flagsFrag = Endianess.convert((short)0x4000);
        targetIp.writeTo(header.dstIP);
        myIp.writeTo(header.srcIP);
        header.ttl = (byte)64; //standard ttl is 64 (see RFC 1700)
        header.prot = (byte)protocol;
        header.len = Endianess.convert((short)(buffer.usableSize));
        header.chksum = Endianess.convert((short) OnesComplement.calc(0, MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));

        // handle link local
        if(Kernel.networkManager.hasLocalIp(targetIp)){
            this.receive(interfaceNo, buffer);
            //LowlevelLogging.debug(String.concat("Linklocal for addr: ", targetIp.toString()));
            return;
        }

        // wait for arp to resolve ip...
        MacAddress targetMac = arpLayer.resolveIp(forwardToGateway ? Kernel.networkManager.getDefaultGateway() : targetIp);

        if (targetMac == null){
            // simple timeout
            LowlevelOutput.printStr("IP could not be resolved!", 0,0, Color.PINK);
            return;
        }

        ethernetLayer.send(interfaceNo, targetMac, Ethernet.TYPE_IP, buffer);
    }

    @Override
    public void receive(int interfaceNo, PackageBuffer buffer) {
        IpHeader header = (IpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        byte version = (byte) (header.versionIhl & 0xF0);
        header.tos = 0;
        IPv4Address targetIp = IPv4Address.fromStruct(header.dstIP);
        IPv4Address sourceIp = IPv4Address.fromStruct(header.srcIP);
        byte protocol = header.prot;
        short chksum = Endianess.convert(header.chksum);
        int len = Endianess.convert(header.len); // todo IPv4 has variable header length!

        // todo check checksum
        //  Endianess.convert((short) Crc32.calc(0, (int)MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));


        if (version != Ip.V4){
            return;
        }

        // handle missing ethernet checksum in some virtual networks... ethernet layer cut it off... add it again
        if (len > buffer.usableSize && len - buffer.usableSize <= 4) {
            buffer.usableSize += len - buffer.usableSize;
        }

        if (len > buffer.usableSize) {
            LowlevelLogging.debug("Dropped invalid package: ip len > package len");
            return;
        }

        // upper layer protocol usable area restrictions
        buffer.start += IpHeader.SIZE;
        buffer.usableSize -= IpHeader.SIZE;

        if (!Kernel.networkManager.hasLocalIp(targetIp) && !targetIp.equals(IPv4Address.getGlobalBreadcastAddr())){
            LowlevelLogging.debug(String.concat("got package with for other ip", targetIp.toString()));

            // IP FORWARDING
            //this.send(Kernel.networkManager.getInterfaceNoForTarget(targetIp), targetIp, protocol, buffer);

            return;
        }

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
            udpLayer.receive(interfaceNo, sourceIp, buffer);
        }
    }

    // todo for now the last one added is always the best match -> can be replaced by net mask stuff
}
