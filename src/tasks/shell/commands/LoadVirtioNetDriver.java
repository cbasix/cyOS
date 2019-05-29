package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import drivers.pci.PCI;
import drivers.pci.PciDevice;
import drivers.virtio.VirtIo;
import drivers.virtio.first_try.VirtioNet;
import drivers.virtio.net.VirtioNic;
import io.LowlevelLogging;
import kernel.Kernel;
import network.PackageBuffer;
import network.layers.Ethernet;
import tasks.LogEvent;


public class LoadVirtioNetDriver extends Command{

    @Override
    public String getCmd() {
        return "virtio";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        boolean found = false;
        ArrayList pciDevices = PCI.scan(0);
        for (int i = 0; i < pciDevices.size(); i++){
            PciDevice p = (PciDevice) pciDevices._get(i);

            if (p.vendorId == VirtIo.VIRTIO_VENDOR_ID && (
                    p.deviceId == VirtIo.VIRTIO_NETWORK_CARD_ID || p.deviceId == VirtIo.VIRTIO_NETWORK_CARD_ID_2)){

                if(found) {
                    shellMessageBuffer.push(new LogEvent("Multiple devices found"));
                }

                byte[] firstMessage = binimp.ByteData.discover;

                //VirtioNetLegacy deviceLegacy = new VirtioNetLegacy(p);
                //VirtioNet device = VirtioNet.from(p);

                VirtioNic device = new VirtioNic(p);

                Kernel.networkManager.nic = device;
                shellMessageBuffer.push(new LogEvent(device.getMacAddress().toString()));

                if(device.hasLink()){
                    shellMessageBuffer.push(new LogEvent("Link is UP"));
                } else {
                    shellMessageBuffer.push(new LogEvent("Link is DOWN"));
                }

                // send test message
                /*for (int j = 0; j < 512; j++) {
                    Ethernet eth = Kernel.networkManager.stack.ethernetLayer;
                    PackageBuffer b = eth.getBuffer(firstMessage.length);
                    int cnt = 0;
                    for (int k = b.start; k < b.start + b.usableSize; k++){
                        b.data[k] = firstMessage[cnt++];
                    }

                    eth.send(Kernel.networkManager.nic.getMacAddress(), Ethernet.TYPE_IP, b);
                }
                shellMessageBuffer.push(new LogEvent("Sending messages done"));*/
                found = true;
            }

        }
        if(!found){
            shellMessageBuffer.push(new LogEvent("No virtio network device found"));
        }
        //Kernel.wait(50);
    }
}
