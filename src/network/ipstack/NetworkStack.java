package network.ipstack;

import network.ipstack.binding.BindingsManager;

public class NetworkStack {
    public BindingsManager bindingsManager = new BindingsManager();
    public Udp udpLayer = new Udp(bindingsManager);
    public Ip ipLayer = new Ip();
    public Arp arpLayer = new Arp();
    public Ethernet ethernetLayer = new Ethernet();


    public NetworkStack(){
        ipLayer.setArpLayer(arpLayer);
        ipLayer.setEthernetLayer(ethernetLayer);
        ipLayer.setUdpLayer(udpLayer);

        udpLayer.setIpLayer(ipLayer);
    }
}
