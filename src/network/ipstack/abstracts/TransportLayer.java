package network.ipstack.abstracts;

import network.PackageBuffer;
import network.address.IPv4Address;

public abstract class TransportLayer {
    public abstract void send(IPv4Address targetIp, int srcPort, int dstPort, byte[] buffer);
    public abstract void receive(int interfaceNo, IPv4Address senderIp, PackageBuffer buffer);
}
