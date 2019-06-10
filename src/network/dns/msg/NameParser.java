package network.dns.msg;

import io.Color;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;


public class NameParser {
    public static final int PTR_MASK = 0xc0;

    private byte[] data;
    private int pos;
    private String name;

    public NameParser(byte[] data, int pos) {
        this.data = data;
        this.pos = pos;
    }

    public int getPos() {
        return pos;
    }

    public String getName() {
        return name;
    }

    public NameParser invoke() {
        name = "";

        byte labelLenOrPtr;
        int ptrDepth = 0;
        int currentLabelAddr = pos;
        /*
        12 5
        18 2
        26 192
        12 5
        18 2
        38 165

0000   26 d8 81 80 00 01 00 02 00 00 00 00 T> 05 62 6c 75
0010   62 62 02 64 65 00 00 01 00 01 L> c0 0c 00 01 00 01
0020   00 00 19 44 00 04 a5 a0 0f 14 L> c0 0c 00 01 00 01
0030   00 00 19 44 00 04 a5 a0 0d 14





         */
        labelLenOrPtr = data[currentLabelAddr];

        while (labelLenOrPtr != 0) {

            //LowlevelLogging.debug(String.concat("pos: ", String.from(currentLabelAddr), " val: ", String.from((data[currentLabelAddr] & 0xff))), "  ");
            //Kernel.wait(4);

            // test if len is a real len or a pointer to another label
            if ((labelLenOrPtr & PTR_MASK) == 0) {
                int len = labelLenOrPtr & 0xff;

                // is a label
                if (ptrDepth == 0) {
                    pos += labelLenOrPtr + 1;
                }

                char[] nameChars = new char[len];
                for (int i = 0; i < len; i++) {
                    nameChars[i] = (char) data[currentLabelAddr + 1 + i]; // only working for ascii
                }
                //name = name + "." + new String(nameChars);
                name = String.concat(name, name.equals("") ? "" : ".", new String(nameChars));

                currentLabelAddr += len + 1;

            } else {
                //it is a pointer to another label
                int ptr = labelLenOrPtr & 0xff & ~PTR_MASK;

                if (ptrDepth == 0) {
                    pos += 1; // pointers are 2 bytes (but the later +1)
                }


                // len is a pointer (that spans 2 bytes)
                currentLabelAddr = (ptr << 8) | (data[currentLabelAddr + 1] & 0xff);
               /* System.out.println("ptr: " + ptr);
                System.out.println("next: " + (data[currentLabelAddr+1] & 0xff));
                System.out.println("jumping to: " + currentLabelAddr);
                */
                ptrDepth++;
            }

            labelLenOrPtr = data[currentLabelAddr];

            /*try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }*/
        }

        // terminating null field
        pos += 1;

        return this;
    }
}