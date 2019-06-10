package network.ipstack.abstracts;

import network.address.IPv4Address;
import network.address.MacAddress;
import network.PackageBuffer;

public abstract class ResolutionLayer {
    public abstract void anounce();

    public abstract MacAddress resolveIp(IPv4Address ip);

    public abstract void receive(int interfaceNo, PackageBuffer buffer);
}
