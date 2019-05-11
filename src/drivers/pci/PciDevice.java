package drivers.pci;

import datastructs.ArrayList;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;

public class PciDevice {

    public static final int STATUS_CAPABILITIES_LIST = 0x10;
    public static final int INTA = 0x01;
    public static final int INTB = 0x02;
    public static final int INTC = 0x03;
    public static final int INTD = 0x04;
    public static final int INT_NONE = 0x00;

    public int busNo;
    public int deviceNo;

    public int deviceId;
    public int vendorId;
    public int status;
    //public int command;
    public int baseClassCode;
    public int subClassCode;
    public int interf;
    public int revision;
    public int header;

    public int capabilitiesPointer;

    public ArrayList baseAddresses;

    public PciDevice(int busNo, int deviceNo) {
        this.busNo = busNo;
        this.deviceNo = deviceNo;

        baseAddresses = new ArrayList();

        loadInfos();
        loadBaseAddresses();
    }

    public void loadInfos(){
        int reg0 = PCI.readConfigSpace(busNo, deviceNo, 0, 0);
        deviceId = (reg0 >> 16) & 0xFFFF;
        vendorId = reg0 & 0xFFFF;

        int reg1 = PCI.readConfigSpace(busNo, deviceNo, 0, 1);
        status = (reg1 >> 16) & 0xFF;

        int reg2 = PCI.readConfigSpace(busNo, deviceNo, 0, 2);
        baseClassCode = (reg2 >> 24) & 0xFF;
        subClassCode = (reg2 >> 16) & 0xFF;
        interf = (reg2 >> 8) & 0xFF;
        revision = reg2 & 0xFF;

        int reg3 = PCI.readConfigSpace(busNo, deviceNo, 0, 3);
        header = (reg3 >> 16) & 0xFF;

        int reg0D = PCI.readConfigSpace(busNo, deviceNo, 0, 0xD);
        capabilitiesPointer = (reg0D & 0xFC) / 4; // bottom 2 bits are reserved
    }

    public void setInterruptLine(int line){
        int reg0F = readConfigSpace(0x0F);
        // take last 4 bits of line, see wiki.osdev.org/PCI
        LowlevelLogging.debug(String.concat("pci 0f reg: ", String.hexFrom(reg0F)));
        writeConfigSpace(0x0F, (reg0F & ~0xFF) | (line & 0xFF));
    }

    public int getInterruptLine() {
        return readConfigSpace(0x0F) & 0xF;
    }

    public void loadBaseAddresses(){
        // there are 6 base adresses = BAR for normal pci-devices
        for (int i = 4; i < 10; i++){
            int baseAddr = readConfigSpace(i);
            boolean isMemoryMapped = (baseAddr & 0x1) == 0;

            // Schreibzugriff mit -1 auf Register liefert Größe
            writeConfigSpace(i, -1);
            int size = ~(readConfigSpace(i) & ~0xf)+1; // osdev.org/PCI  Bar layout
            // reset old value
            writeConfigSpace(i, baseAddr);

            baseAddresses._add(new PciBaseAddr(baseAddr, size));
        }

    }


    public void writeConfigSpace(int reg, int value){
        writeConfigSpace(0, reg, value);
    }

    public void writeConfigSpace(int functionNo, int reg, int value){
        PCI.writeConfigSpace(busNo, deviceNo, functionNo, reg, value);
    }

    public int readConfigSpace(int reg){
        return readConfigSpace(0, reg);
    }

    public int readConfigSpace(int functionNo, int reg){
        return PCI.readConfigSpace(busNo, deviceNo, functionNo, reg);
    }

    /**
     * show dump of the pci confic space of the current device
     * @param start conf register
     */
    public void dump(int start){
        for (int i = 0; i < 24*4; i++){
            LowlevelOutput.printHex(i + start, 8, 1+20*(i/24), i%24, Color.RED);
            LowlevelOutput.printHex(readConfigSpace(i + start), 8, 10+20*(i/24), i%24, Color.RED);
        }
    }


}
