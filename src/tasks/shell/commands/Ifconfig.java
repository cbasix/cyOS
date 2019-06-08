package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import kernel.Kernel;
import network.address.IPv4Address;
import tasks.LogEvent;

public class Ifconfig extends Command{

    @Override
    public String getCmd() {
        return "ifconfig";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        ArrayList addrList = Kernel.networkManager.stack.ipLayer.getAddresses();
        IPv4Address gateway = Kernel.networkManager.stack.ipLayer.getDefaultGateway();
        IPv4Address defaultIp = Kernel.networkManager.stack.ipLayer.getDefaultIp();
        IPv4Address dnsServer = Kernel.networkManager.stack.getDnsServer();

        shellOutput.push(new LogEvent(String.concat("Default Gateway: ", gateway != null ? gateway.toString() : "none")));
        shellOutput.push(new LogEvent(String.concat("Default Ip:      ", defaultIp != null ? defaultIp.toString() : "none")));
        shellOutput.push(new LogEvent(String.concat("DNS server:      ", dnsServer != null ? dnsServer.toString() : "none")));

        shellOutput.push(new LogEvent("\n All Ips: "));
        for(int i = 0; i < addrList.size(); i++){
            IPv4Address ip = (IPv4Address) addrList._get(i);
            shellOutput.push(new LogEvent(String.concat("   ", ip.toString())));
        }
    }
}
