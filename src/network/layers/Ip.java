package network.layers;

import conversions.Endianess;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.receivers.TimerCounter;
import network.IPv4Address;
import network.MacAddress;
import network.PackageBuffer;
import network.checksum.OnesComplement;
import network.structs.EthernetFooter;
import network.structs.EthernetHeader;
import network.structs.IpHeader;

public class Ip {
    //public static final int PROTO_UDP = ;
    //public static final int PROTO_TCP = ;
    //public static final int PROTO_ICMP = ;

    public static int myIp;

    public Ip(){
        myIp = 0xC0A8C800 | (TimerCounter.getCurrent()%125)+1; // random last part of ip between 1 and 126
    }

    public PackageBuffer getBuffer(int payloadSize) {
        long targetMac = 0L;
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
        header.srcIP = Endianess.convert(myIp);
        header.ttl = 64; //standard ttl is 64 (see RFC 1700)
        header.prot = (byte)protocol;
        header.chksum = 0;
        header.len = Endianess.convert((short)(buffer.usableSize + IpHeader.SIZE));
        header.chksum = Endianess.convert((short)OnesComplement.calc(0, (int)MAGIC.addr(header.versionIhl), IpHeader.SIZE, true));

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
}
