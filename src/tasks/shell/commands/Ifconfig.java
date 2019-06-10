package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import kernel.Kernel;
import network.Interface;
import network.address.IPv4Address;
import tasks.LogEvent;

public class Ifconfig extends Command{

    @Override
    public String getCmd() {
        return "ifconfig";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
        ArrayList interfaces = Kernel.networkManager.getInterfaces();

        IPv4Address gateway = Kernel.networkManager.getDefaultGateway();
        shellOutput.push(new LogEvent(String.concat("Default Gateway: ", gateway != null ? gateway.toString() : "none")));

        IPv4Address dnsServer = Kernel.networkManager.stack.getDnsServer();
        shellOutput.push(new LogEvent(String.concat("DNS server:      ", dnsServer != null ? dnsServer.toString() : "none")));

        for (int interfaceNo = 0; interfaceNo < interfaces.size(); interfaceNo++) {
            Interface intf = (Interface) interfaces._get(interfaceNo);
            shellOutput.push(new LogEvent(String.concat("\n Interface No:      ", String.from(interfaceNo))));


            ArrayList addrList = intf.getAddresses();
            IPv4Address defaultIp = intf.getDefaultIp();

            shellOutput.push(new LogEvent(String.concat("  Default Ip:      ", defaultIp != null ? defaultIp.toString() : "none")));

            shellOutput.push(new LogEvent("  All Ips: "));
            for (int i = 0; i < addrList.size(); i++) {
                IPv4Address ip = (IPv4Address) addrList._get(i);
                shellOutput.push(new LogEvent(String.concat("    ", ip.toString())));
            }
        }
    }
}
