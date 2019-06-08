package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import network.address.IPv4Address;
import network.dns.ARecord;
import network.dns.DnsClient;
import tasks.LogEvent;

public class Dig extends Command{

    private network.dns.DnsServer server;

    @Override
    public String getCmd() {
        return "dig";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {


        if (args.length < 2){
            shellOutput.push(new LogEvent("please specify hostname"));
            return;
        }

        DnsClient client = new DnsClient();
        IPv4Address addr = client.resolve(args[1]);

        shellOutput.push(new LogEvent(String.concat(args[1], " -> ", addr != null ? addr.toString() : "no ip found")));
    }
}
