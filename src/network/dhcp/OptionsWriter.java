package network.dhcp;

import conversions.Endianess;

public class OptionsWriter {
    public static final int MAX_SIZE = 512;

    private final byte[] buffer;
    private int bufPos;

    public OptionsWriter(){
        this.buffer = new byte[MAX_SIZE];
    }

    public void write(byte option, byte value) {
        DhcpOption o = (DhcpOption) MAGIC.cast2Struct(MAGIC.addr(buffer[bufPos]));

        o.option = option;
        o.len = 1;
        o.valueBytes[0] = value;

        bufPos += DhcpOption.FIXED_SIZE + o.len;
    }

    public void write(byte option, int value) {
        DhcpOption o = (DhcpOption) MAGIC.cast2Struct(MAGIC.addr(buffer[bufPos]));

        o.option = option;
        o.len = 4;
        o.valueInts[0] = Endianess.convert(value);

        bufPos += DhcpOption.FIXED_SIZE + o.len;
        // todo overruns
    }

    public void writeTo(int addr) {
        for (int i = 0; i < bufPos; i++){
            MAGIC.wMem8(addr + i, buffer[i]);
        }
    }

    public int getSize() {
        return bufPos;
    }

    public void write(byte singleOpt) {
        MAGIC.wMem8(MAGIC.addr(buffer[bufPos]), singleOpt);
        bufPos += 1;
    }
}
