package network;

import network.layers.Ethernet;
import network.layers.Ip;
import network.layers.Udp;

public class NetworkStack {
    public Udp udpLayer = new Udp();
    public Ip ipLayer = new Ip();
    public Ethernet ethernetLayer = new Ethernet();
}
