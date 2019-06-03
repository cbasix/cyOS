package network.ipstack.binding;

import network.PackageBuffer;
import network.address.IPv4Address;
import network.ipstack.abstracts.TransportLayer;

public abstract class PackageReceiver {
    public abstract void receive(TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data);
}
