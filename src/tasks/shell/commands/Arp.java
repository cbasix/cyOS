package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import kernel.Kernel;
import network.*;
import network.ipstack.ArpCache;
import tasks.LogEvent;


public class Arp extends Command{

    @Override
    public String getCmd() {
        return "arp";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        network.ipstack.Arp arpLayer = Kernel.networkManager.stack.arpLayer;
        int interfaceCnt = Kernel.networkManager.getInterfaces().size();

        if (args.length > 1){
            if (args[1].equals("cache")){
                ArrayList items = arpLayer.cache.getList();
                for (int i = 0; i < items.size(); i++){
                    ArpCache.Entry entry = (ArpCache.Entry) items._get(i);
                    shellMessageBuffer.push(new LogEvent(String.concat(String.concat(entry.ip.toString(), " "), entry.mac != null ? entry.mac.toString() : "noMac")));
                }

            } else if (args[1].equals("announce")){
                if(interfaceCnt == 0){
                    shellMessageBuffer.push(new LogEvent("No nic found"));
                    return;
                }

                arpLayer.anounce();
                shellMessageBuffer.push(new LogEvent("Anouncements send"));
                //shellMessageBuffer.push(new LogEvent(String.concat("my own mac via arp: ", mac != null ? mac.toString() : "Resolve Error")));

            }


        } else {
            shellMessageBuffer.push(new LogEvent("Please specify subcommand: cache or announce"));
        }





        //Kernel.wait(50);
    }
}
