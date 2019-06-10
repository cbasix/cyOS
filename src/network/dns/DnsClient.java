package network.dns;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import kernel.interrupts.receivers.TimerCounter;
import network.address.IPv4Address;
import network.dns.msg.DnsMessage;
import network.dns.msg.ResourceRecord;
import network.ipstack.NetworkStack;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.BindingsManager;
import network.ipstack.binding.PackageReceiver;
import random.PseudoRandom;

public class DnsClient extends PackageReceiver {

    private static final int WAIT_TICKS = 200;
    private final NetworkStack stack;
    private int port;
    private short sessionId;
    private boolean gotAnswer = false;
    private IPv4Address result;
    private IPv4Address dnsserver;

    public DnsClient(){
        this.stack = Kernel.networkManager.stack;
        dnsserver = stack.getDnsServer();
    }

    public void setDnsserver(IPv4Address dnsserver){
        this.dnsserver = dnsserver;
    }

    @Override
    public void receive(int interfaceNo, TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data) {
        DnsMessage m = DnsMessage.fromBytes(data);
        if(m.getId() == sessionId && m.getAnswers().size() >= 1){
            ResourceRecord a = (ResourceRecord) m.getAnswers()._get(0); // just return first answer
            //LowlevelLogging.debug("rdata len: ", String.from(a.rdata.length));
            result = new IPv4Address(a.rdata);
        } else {
            LowlevelOutput.printStr("Got non correct dns answer", 0, 0, Color.RED);
            LowlevelOutput.printStr(String.hexFrom(m.getAnswers().size()), 0, 1, Color.RED);
            Kernel.wait(4);
        }

        gotAnswer = true;
        stack.bindingsManager.unbind(BindingsManager.ALL_INTERFACES, stack.udpLayer, port, this);
    }

    public IPv4Address resolve(String hostname) {
        gotAnswer = false;
        result = null;

        sessionId = (short) PseudoRandom.getRandInt();
        port = stack.bindingsManager.getUnusedPort(BindingsManager.ALL_INTERFACES, stack.udpLayer);

        stack.bindingsManager.bind(BindingsManager.ALL_INTERFACES, stack.udpLayer, port, this);

        DnsMessage m = new DnsMessage()
                .setId(sessionId)
                .setRecursionDesired(true)
                .addQuestion(hostname);

        stack.udpLayer.send(dnsserver, port, DnsServer.DNS_SERVER_PORT, m.toBytes());

        int startTime = TimerCounter.getCurrent();
        while (!gotAnswer && TimerCounter.getCurrent() - startTime < WAIT_TICKS) {
            Kernel.networkManager.receive();
        }

        /*if (gotAnswer) {
            LowlevelLogging.debug("Got ansewer");
        } else {
            LowlevelLogging.debug("got no answer");
        }*/

        return result;
    }
}
