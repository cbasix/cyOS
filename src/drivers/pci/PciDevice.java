package drivers.pci;

public class PciDevice {
    private int busNo;
    private int deviceNo;

    public int deviceId;
    public int manufacturerId;
    public int status;
    //public int command;
    //public int baseClassCode;
    //public int subClassCode;
    //public int interfaceId;
    //public int revision;
    public int header;

    public PciDevice(int busNo, int deviceNo, int deviceId, int manufacturerId) {
        this.busNo = busNo;
        this.deviceNo = deviceNo;
        this.deviceId = deviceId;
        this.manufacturerId = manufacturerId;
    }
}
