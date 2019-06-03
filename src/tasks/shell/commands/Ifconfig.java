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

        for(int i = 0; i < addrList.size(); i++){
            IPv4Address ip = (IPv4Address) addrList._get(i);
            shellOutput.push(new LogEvent(ip.toString()));
        }
    }
}
