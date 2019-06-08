package network.dns;

import datastructs.ArrayList;
import io.LowlevelLogging;
import kernel.Kernel;
import network.address.IPv4Address;
import network.dns.msg.DnsMessage;
import network.dns.msg.Question;
import network.ipstack.NetworkStack;
import network.ipstack.abstracts.TransportLayer;
import network.ipstack.binding.PackageReceiver;

public class DnsServer extends PackageReceiver {
    public static final int DNS_SERVER_PORT = 53;
    public static final int DEFAULT_TTL = 60*60*24; // 24h
    private final NetworkStack stack;
    private DnsCache cache;

    public DnsServer () {
        stack = Kernel.networkManager.stack;
        cache = new DnsCache();
        stack.bindingsManager.bind(stack.udpLayer, DNS_SERVER_PORT, this);
    }

    public void add(ARecord a){
        cache.add(a);
    }

    public ArrayList getCacheRecords(){
        return cache.cache;
    }

    @Override
    public void receive(TransportLayer transport, IPv4Address senderIp, int senderPort, int receiverPort, byte[] data) {
        DnsMessage msg = DnsMessage.fromBytes(data);


        for (int i = 0; i < msg.getQuestionCount(); i++){
            Question q = (Question) msg.getQuestions()._get(i);

            if((q.getqClass() == DnsMessage.CLASS_IN || q.getqClass() == DnsMessage.QCLASS_ALL)
                    && q.getqType() == DnsMessage.TYPE_A || q.getqType() == DnsMessage.QTYPE_ALL){

                ARecord r = cache.get(q.getName());
                if (r != null) {
                    LowlevelLogging.debug(r.ip.toString());
                    msg.addAnswer(r.name, DEFAULT_TTL, r.ip.toBytes());
                }
            }
        }

        msg.setType(DnsMessage.TYPE_RESPONSE)
                .setTruncated(false)
                .setRecursionAvailable(false)
                .setResponseCode(DnsMessage.RCODE_NO_ERROR);


        stack.udpLayer.send(senderIp, DNS_SERVER_PORT, senderPort, msg.toBytes());
    }

    public void stop() {
        stack.bindingsManager.unbind(stack.udpLayer, DNS_SERVER_PORT, this);
    }
}
