package network.dhcp;

import conversions.Endianess;
import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;


public class OptionsReader {
    private int startAddr;
    private int len;

    public OptionsReader(int startAddr, int optionsLen){
        this.startAddr = startAddr;
        this.len = optionsLen;
    }

    public byte[] getOptionValue(int option){
        int curAddr = startAddr;
        byte curOpt = MAGIC.rMem8(curAddr);

        while (curOpt != DhcpOption.OPT_END) {

            if (curOpt == DhcpOption.OPT_PADDING){
                // todo check for read after end of msg
                curAddr++;
                continue;
            }

            DhcpOption opt = (DhcpOption) MAGIC.cast2Struct(curAddr);
            int curLen = (int)opt.len;

            //LowlevelOutput.printStr(String.hexFrom(curLen), 8, 3, Color.RED);

            if(curLen < 1){
                LowlevelLogging.debug(String.concat(String.from(curOpt), "DHCP options read: invalid len (must >=1): ", String.from(curLen)));
                return null;
            }

            if (curOpt == (byte) option){
                byte[] value = new byte[curLen];

                // overflow check
                if (curAddr + DhcpOption.FIXED_SIZE + curLen > startAddr + this.len){
                    LowlevelLogging.debug("DHCP options read overflow");
                    return null;
                }

                for (int i = 0; i < curLen; i++){
                    value[i] = opt.valueBytes[i];
                }

                return value;
            }


            curAddr += DhcpOption.FIXED_SIZE + curLen;
            curOpt = MAGIC.rMem8(curAddr);
        }

        return null;
    }
}
