package network.layers.abstracts;

import network.IPv4Address;
import network.MacAddress;
import network.PackageBuffer;

public abstract class ResolutionLayer {
    public abstract void anounce();

    public abstract MacAddress resolveIp(IPv4Address ip);

    public abstract void receive(PackageBuffer buffer);
}
