package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import kernel.Kernel;
import network.*;
import network.layers.Arp;
import network.layers.Ip;
import tasks.LogEvent;


public class Network extends Command{

    @Override
    public String getCmd() {
        return "net";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        NetworkStack stack = Kernel.networkManager.stack;
        Nic nic = Kernel.networkManager.nic;

        if(nic == null){
            shellMessageBuffer.push(new LogEvent("No nic found"));
            return;
        }

        if (args.length > 1) {
            if (args[1].equals("receive")) {
                doReceive(shellMessageBuffer, stack);

            } else if (args[1].equals("send")) {
                if(args.length < 3) {
                    shellMessageBuffer.push(new LogEvent("Please provide a message"));
                    return;
                }
                doSend(shellMessageBuffer, stack, args[2]);
            }
        } else {
            shellMessageBuffer.push(new LogEvent("Please select subcommand_ receive or send."));
        }
    }


    public void doSend(RingBuffer shellMessageBuffer, NetworkStack stack, String message) {
        IPv4Address sendToIp = null;
        ArrayList arpCacheEntrys = stack.arpLayer.cache.getList();
        for (int i = 0; i < arpCacheEntrys.size(); i++){
            ArpCache.Entry entry = (ArpCache.Entry) arpCacheEntrys._get(i);
            if (entry.mac != null){
                sendToIp = entry.ip;
            }
        }

        if (sendToIp == null){
            shellMessageBuffer.push(new LogEvent("No recipient available."));
            return;
        }


        char[] charData = message.toChars();
        PackageBuffer buffer = stack.ipLayer.getBuffer(charData.length * 2);
        for (int i = 0; i < charData.length; i++){
            buffer.data[buffer.start+i] = (byte) (charData[i] >> 8) ;
            buffer.data[buffer.start+i+1] = (byte) charData[i];
        }


        stack.ipLayer.send(sendToIp, Ip.PROTO_RAW_TEXT, buffer);
        byte[] data = Kernel.networkManager.nic.receive();
        if (data != null) {
            stack.ethernetLayer.receive(data);
        }
    }


    public void doReceive(RingBuffer shellMessageBuffer, NetworkStack stack) {
        byte[] data = null;
        do {
            data = Kernel.networkManager.nic.receive();
            if (data != null) {
                stack.ethernetLayer.receive(data);
            }
            //Kernel.wait(50);
        } while(data != null);
    }
}
