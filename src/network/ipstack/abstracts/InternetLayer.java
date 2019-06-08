package network.ipstack.abstracts;

import network.address.IPv4Address;
import network.PackageBuffer;

public abstract class InternetLayer {
    public abstract void send(IPv4Address targetIp, int protocol, PackageBuffer buffer);
    public abstract void receive(PackageBuffer buffer);
    public abstract PackageBuffer getBuffer(int payloadSize);
}