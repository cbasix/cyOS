package tests.highlevel;

import network.IPv4Address;
import network.MacAddress;
import network.PackageBuffer;
import network.layers.Ip;
import network.layers.abstracts.LinkLayer;
import network.layers.abstracts.ResolutionLayer;

public class IpTest {
    private static byte[] ethernetReceived;
    private static PackageBuffer sent;
    private static short type;
    private static MacAddress targetMac;
    private static PackageBuffer arpReceive;

    private static class MockEthernet extends LinkLayer {

        @Override
        public PackageBuffer getBuffer(int payloadSize) {
            byte[] data = new byte[payloadSize];
            return new PackageBuffer(data, 0, payloadSize);
        }

        @Override
        public void receive(byte[] data) {
            ethernetReceived = data;
        }

        @Override
        public void send(MacAddress targetMac_, short type_, PackageBuffer buffer) {
            sent = buffer;
            type = type_;
            targetMac = targetMac_;
        }
    }

    private static class MockArpLayer extends ResolutionLayer {

        @Override
        public void anounce() {

        }

        @Override
        public MacAddress resolveIp(IPv4Address ip) {
            byte[] mac = new byte[6];
            for (int i = 0; i < 6; i++) {
                mac[i] = (byte)i;
            }
            return MacAddress.fromBytes(mac);
        }

        @Override
        public void receive(PackageBuffer buffer) {
            arpReceive = buffer;
        }
    }

    public static int test(){
        Ip ipLayer = new Ip();
        ipLayer.setEthernetLayer(new MockEthernet());
        ipLayer.setArpLayer(new MockArpLayer());


        PackageBuffer buffer = ipLayer.getBuffer(10);
        byte[] data = new byte[10];
        for (int i = 0; i < 10; i ++){
            buffer.data[buffer.start + i] = (byte)i;
        }
        ipLayer.send(new IPv4Address(1), Ip.PROTO_RAW_TEXT, buffer);


        return 0;
    }
}
