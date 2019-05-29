package tasks.shell.commands;

import datastructs.RingBuffer;
import kernel.Kernel;
import network.IPv4Address;
import network.MacAddress;
import network.NetworkStack;
import network.Nic;
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

        for(int c = 0; c < 20; c++) {
            for (int i = 0; i < 10; i++) {
                Kernel.sleep();
            } // short delay untill next system timer
            byte[] data = Kernel.networkManager.nic.receive();
            if (data != null) {
                stack.ethernetLayer.receive(data);
            }
            //Kernel.wait(50);
        }
    }
}
