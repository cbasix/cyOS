package network.ipstack.abstracts;

import network.address.MacAddress;
import network.PackageBuffer;

public abstract class LinkLayer {
    public abstract PackageBuffer getBuffer(int payloadSize);
    public abstract void receive(byte[] data);
    public abstract void send(MacAddress targetMac, short type, PackageBuffer buffer);
}
