package tasks.shell.commands;

import datastructs.ArrayList;
import datastructs.RingBuffer;
import drivers.pci.PCI;
import drivers.pci.PciDevice;
import drivers.virtio.VirtIo;
import drivers.virtio.first_try.VirtioNet;
import drivers.virtio.net.VirtioNic;
import io.LowlevelLogging;
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
                    LowlevelLogging.debug("Multiple devices found");
                }

                byte[] firstMessage = binimp.ByteData.discover;

                //VirtioNetLegacy deviceLegacy = new VirtioNetLegacy(p);
                //VirtioNet device = VirtioNet.from(p);
                VirtioNic device = new VirtioNic(p);
                for (int j = 0; j < 512; j++) {
                    device.send(firstMessage);
                }

                found = true;
            }

        }
        if(!found){
            shellMessageBuffer.push(new LogEvent("No virtio network device found"));
        }
        //Kernel.wait(50);
    }
}
