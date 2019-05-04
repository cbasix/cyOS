package drivers.pci;

import datastructs.ArrayList;

public class PciDevice {
    public int busNo;
    public int deviceNo;

    public int deviceId;
    public int manufacturerId;
    public int status;
    //public int command;
    public int baseClassCode;
    public int subClassCode;
    public int interf;
    public int revision;
    public int header;

    public ArrayList baseAddresses;
    public ArrayList baseAddressSizes;

    public PciDevice(int busNo, int deviceNo) {
        this.busNo = busNo;
        this.deviceNo = deviceNo;

        baseAddresses = new ArrayList();
        baseAddressSizes = new ArrayList();

        loadInfos();
    }

    public void loadInfos(){
        int reg0 = PCI.readConfigSpace(busNo, deviceNo, 0, 0);
        deviceId = (reg0 >> 16) & 0xFFFF;
        manufacturerId = reg0 & 0xFFFF;

        int reg2 = PCI.readConfigSpace(busNo, deviceNo, 0, 2);
        baseClassCode = (reg2 >> 24) & 0xFF;
        subClassCode = (reg2 >> 16) & 0xFF;
        interf = (reg2 >> 8) & 0xFF;
        revision = reg2 & 0xFF;

        int reg3 = PCI.readConfigSpace(busNo, deviceNo, 0, 3);
        header = (reg3 >> 16) & 0xFF;;
    }

    public void loadBaseAddresses(){
        for (int i = 4; i < 10; i++){
            int baseAddr = readConfigSpace(i);
            if (baseAddr)
            boolean isMemoryMapped = (baseAddr & 0x1) == 0;

            // Schreibzugriff mit -1 auf Register liefert Größe
            writeConfigSpace(i, -1);
            int size = readConfigSpace(i);
            // reset old value
            writeConfigSpace(i, baseAddr);



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

}
