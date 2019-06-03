package network;

import network.address.MacAddress;

public abstract class Nic {
    public abstract void send(byte[] data);
    public abstract byte[] receive();

    public abstract MacAddress getMacAddress();

    public abstract boolean hasLink();
}
