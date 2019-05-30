package network;

import network.layers.Arp;
import network.layers.Ethernet;
import network.layers.Ip;
import network.layers.Udp;

public class NetworkStack {
    public Udp udpLayer = new Udp();
    public Ip ipLayer = new Ip();
    public Arp arpLayer = new Arp();
    public Ethernet ethernetLayer = new Ethernet();

    public NetworkStack(){
        ipLayer.setArpLayer(arpLayer);
        ipLayer.setEthernetLayer(ethernetLayer);
        ipLayer.setUdpLayer(udpLayer);
    }
}
