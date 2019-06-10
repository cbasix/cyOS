package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import kernel.Kernel;
import network.address.IPv4Address;
import network.dns.ARecord;
import network.ipstack.NetworkStack;
import tasks.LogEvent;

public class DnsServer extends Command{

    private network.dns.DnsServer server
            ;

    @Override
    public String getCmd() {
        return "dnss";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {


        if (args.length < 2){
            shellOutput.push(new LogEvent("use subcomand start or stop"));
            return;
        }

        if (args[1].equals("start")) {
            if (args.length < 3){
                shellOutput.push(new LogEvent("specify interface to start the server on"));
                return;
            }
            int interfaceNo = args[2].toInt();

            NetworkStack stack = Kernel.networkManager.stack;

            server = new network.dns.DnsServer(interfaceNo);
            // add some (wrong) local dummy entrys
            server.add(new ARecord("dhcpserver.cyos", IPv4Address.fromString("172.16.0.8")));
            server.add(new ARecord("dnsserver.cyos", IPv4Address.fromString("192.168.100.1")));
            server.add(new ARecord("first.cyos", IPv4Address.fromString("192.168.200.97")));
            server.add(new ARecord("97.cyos", IPv4Address.fromString("192.168.200.97")));
            server.add(new ARecord("second.cyos", IPv4Address.fromString("192.168.200.96")));
            server.add(new ARecord("96.cyos", IPv4Address.fromString("192.168.200.96")));

            stack.setDnsServer(Kernel.networkManager.getInterface(interfaceNo).getDefaultIp());

        } else if (args[1].equals("stop")) {
            if (server != null){
                server.stop();
            }

        }  else if (args[1].equals("cache")) {
            if (server != null){
                ArrayList records = server.getCacheRecords();
                for (int i = 0; i < records.size(); i++){
                    ARecord a = (ARecord) records._get(i);
                    shellOutput.push(new LogEvent(String.concat(a.getName(), " -> ", a.getIp().toString())));
                }
            }
        }
    }
}
