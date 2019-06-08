package network.ipstack;

import conversions.Endianess;
import drivers.virtio.structs.Buffer;
import io.LowlevelLogging;
import network.PackageBuffer;
import network.address.IPv4Address;
import network.ipstack.abstracts.InternetLayer;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.BindingsManager;
import network.ipstack.binding.PackageReceiver;
import network.ipstack.structs.UdpHeader;

public class Udp extends TransportLayer {
    private InternetLayer ipLayer;
    private BindingsManager bindingsManager;

    public Udp(BindingsManager bindingsManager){
        this.bindingsManager = bindingsManager;
    }

    public BindingsManager getBindingsManager() {
        return bindingsManager;
    }

    public void setIpLayer(InternetLayer ipLayer) {
        this.ipLayer = ipLayer;
    }

    public PackageBuffer getBuffer(int payloadSize) {
        PackageBuffer buffer = ipLayer.getBuffer(payloadSize + UdpHeader.SIZE);


        if (buffer.usableSize != payloadSize + UdpHeader.SIZE){
            LowlevelLogging.debug("IP layer betrayed us, wrong size");
        }
        // upper layer protocols may only use the payload segment part-> for them the size is smaller
        buffer.start += UdpHeader.SIZE;
        buffer.usableSize -= UdpHeader.SIZE;
        return buffer;
    }

    @Override
    public void send(IPv4Address targetIp, int srcPort, int dstPort, byte[] data) {
        PackageBuffer buffer = getBuffer(data.length);

        // remove upper layer protocols restrictions
        buffer.start -= UdpHeader.SIZE;
        buffer.usableSize += UdpHeader.SIZE;

        UdpHeader header = (UdpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        // todo check values
        header.dstPort = Endianess.convert((short) dstPort);
        header.srcPort = Endianess.convert((short) srcPort);
        header.len = Endianess.convert((short)(data.length + UdpHeader.SIZE));
        header.chk = 0;

        for (int i = 0; i < data.length; i++){
            buffer.data[buffer.start + UdpHeader.SIZE + i] = data[i];
        }

        ipLayer.send(targetIp, Ip.PROTO_UDP, buffer);

    }

    @Override
    public void receive(IPv4Address senderIp, PackageBuffer buffer) {
        UdpHeader header = (UdpHeader) MAGIC.cast2Struct(MAGIC.addr(buffer.data[buffer.start]));

        int senderPort = Endianess.convert(header.srcPort);
        int receiverPort = Endianess.convert(header.dstPort);

        // todo check checksum

        // upper layer protocol usable area restrictions
        buffer.start += UdpHeader.SIZE;
        buffer.usableSize -= UdpHeader.SIZE;

        byte[] data = new byte[buffer.usableSize];
        for(int i = 0; i < buffer.usableSize; i++){
            data[i] = buffer.data[buffer.start + i];
        }

        bindingsManager.receive(this, senderIp, senderPort, receiverPort, data);
    }
}
