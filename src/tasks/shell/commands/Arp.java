package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import drivers.pci.PCI;
import drivers.pci.PciDevice;
import drivers.virtio.VirtIo;
import drivers.virtio.net.VirtioNic;
import kernel.Kernel;
import network.IPv4Address;
import network.MacAddress;
import network.Nic;
import network.PackageBuffer;
import network.layers.Ethernet;
import network.layers.Ip;
import tasks.LogEvent;


public class Arp extends Command{

    @Override
    public String getCmd() {
        return "arp";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        network.layers.Arp arpLayer = Kernel.networkManager.stack.arpLayer;
        Nic nic = Kernel.networkManager.nic;

        if (args.length > 1){
            if (args[1].equals("cache")){
                for (int i = 0; i < arpLayer.cache.size(); i++){
                    network.layers.Arp.ArpCacheEntry entry = (network.layers.Arp.ArpCacheEntry) arpLayer.cache._get(i);
                    shellMessageBuffer.push(new LogEvent(String.concat(entry.ip.toString(), entry.mac != null ? entry.mac.toString() : "noMac")));
                }

            } else if (args[1].equals("send")){
                if(nic == null){
                    shellMessageBuffer.push(new LogEvent("No nic found"));
                    return;
                }

                MacAddress mac = arpLayer.resolveIp(new IPv4Address(Ip.myIp));
                shellMessageBuffer.push(new LogEvent(String.concat("my own mac via arp: ", mac != null ? mac.toString() : "Resolve Error")));

            }


        } else {
            shellMessageBuffer.push(new LogEvent("Please specify subcommand: cache or send"));
        }





        //Kernel.wait(50);
    }
}
