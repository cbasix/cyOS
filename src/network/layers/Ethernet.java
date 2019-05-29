package network.layers;


import conversions.Endianess;
import io.LowlevelLogging;
import kernel.Kernel;
import network.MacAddress;
import network.PackageBuffer;
import network.checksum.Crc32;
import network.structs.EthernetHeader;
import network.structs.EthernetFooter;

public class Ethernet {
    public final static short TYPE_ARP = 0x0806;
    public final static short TYPE_IP = 0x0800;

    public PackageBuffer getBuffer(int payloadSize) {
        byte[] ethernetFrame = new byte[EthernetHeader.SIZE + payloadSize + EthernetFooter.SIZE];

        return new PackageBuffer(ethernetFrame, EthernetHeader.SIZE, payloadSize);
    }

    private PackageBuffer bufferFromBytes(byte[] data){
        return new PackageBuffer(data, 0, data.length);
    }

    public void send(MacAddress targetMac, short type, PackageBuffer buffer){
        buffer.start -= EthernetHeader.SIZE; // should equal 0
        buffer.usableSize += EthernetHeader.SIZE + EthernetFooter.SIZE; // should equal buffer.length

        MacAddress myMac = Kernel.networkManager.nic.getMacAddress(); // todo check endianness of mac addr

        EthernetHeader header = (EthernetHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        EthernetFooter tail = (EthernetFooter) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.data.length - EthernetFooter.SIZE]));

        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            header.destMac[i] = targetMac.addr[i];
        }
        for (int i = 0; i < MacAddress.MAC_LEN; i++){
            header.srcMac[i] = myMac.addr[i];
        }
        header.type = Endianess.convert(type);

        // calc checksum over ethernet header
        tail.checksum = Endianess.convert(Crc32.calc(0, MAGIC.addr(buffer.data[buffer.start]), EthernetHeader.SIZE, false)); // todo check

        Kernel.networkManager.nic.send(buffer.data);
    }

    public void receive(byte[] data){
        PackageBuffer buffer = bufferFromBytes(data);

        EthernetHeader header = (EthernetHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));
        EthernetFooter tail = (EthernetFooter) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.data.length - 1 - EthernetFooter.SIZE]));

        LowlevelLogging.debug(String.concat("ETHERNET ",String.concat(String.hexFrom(Endianess.convert(header.type)),"                       ")));

        // define payload area for upper proto
        buffer.start += EthernetHeader.SIZE;
        buffer.usableSize -= EthernetHeader.SIZE + EthernetFooter.SIZE;

        int type = Endianess.convert(header.type);
        if (type == TYPE_ARP) {
            Kernel.networkManager.stack.arpLayer.receive(buffer);

        } else if (type == TYPE_IP) {
            Kernel.networkManager.stack.ipLayer.receive(buffer);
        }

    }
}
