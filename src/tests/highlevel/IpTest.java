package tests.highlevel;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import network.address.IPv4Address;
import network.address.MacAddress;
import network.PackageBuffer;
import network.ipstack.Ip;
import network.ipstack.abstracts.LinkLayer;
import network.ipstack.abstracts.ResolutionLayer;

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


        IPv4Address a = new IPv4Address(0xFF552287);
        byte[] aByte = a.toBytes();
        if (aByte[0] != (byte)0xFF){return 2600;}
        if (aByte[1] != (byte)0x55){return 2601;}
        if (aByte[2] != (byte)0x22){return 2602;}
        if (aByte[3] != (byte)0x87){return 2603;}

        IPv4Address b = new IPv4Address();
        if (a.toInt() != b.toInt()){
            LowlevelOutput.printStr(a.toString(), 15, 2, Color.GREEN);
            LowlevelOutput.printStr(b.toString(), 15, 3, Color.RED);
            return 2604;
        }

        return 0;
    }
}
