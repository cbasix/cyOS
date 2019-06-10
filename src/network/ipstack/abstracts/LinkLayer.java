package network.ipstack.abstracts;

import network.address.MacAddress;
import network.PackageBuffer;

public abstract class LinkLayer {
    public abstract PackageBuffer getBuffer(int payloadSize);
    public abstract void receive(int interfaceNo, byte[] data);
    public abstract void send(int interfaceNo, MacAddress targetMac, short type, PackageBuffer buffer);
}
