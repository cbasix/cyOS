package apps;

import io.*;
import kernel.Kernel;

/**
 * This app just uses the GreenScreenOutput classes to have some fun and show their capabilities.
 */
public class OutputApp {
    public static void run() {
        LowlevelOutput.clearScreen(GreenScreenConst.DEFAULT_COLOR);

        GreenScreenOutput out = new GreenScreenOutput();
        out.setCursor(0, 3);
        out.setColor(Color.BLACK, Color.CYAN);

        GreenScreenOutput err = new GreenScreenOutput();
        err.setCursor(0, 23);
        err.setColor(Color.RED, Color.BLACK);

        // write some displays full of useless ascii jokes
        for (byte c : binimp.ByteData.lorem_ipsum) {
            out.print((char) c);
            if (out.getCursor() >= GreenScreenConst.WIDTH * GreenScreenConst.HEIGHT) {
                break;
            }
        }
        //MAGIC.getNamedString("lorem_ipsum") <-- TODO ask how this works

        out.setCursor(0, 0);
        out.println("Output App");
        out.println("-----------------------------------");
        out.println();


        err.print("Blubber Blubb. The solution is "); err.print(42); err.print(" Hello "); err.printHex(42); err.print(' ');err.printHex(0x24242424242424L); err.println();
        err.print("~"); err.print("NegInt: "); err.print(-42, 8); err.print(" Long: "); err.print(42424242424242424L); err.println('*');

        while (Kernel.mode == Kernel.OUTPUT_APP){
            // clock, something is wrong with how i expect the rtc data... but who cares. at least seconds change every second and minutes every minute.
            err.setCursor(70, 24);
            err.print(RTC.read(RTC.HOUR), 2);
            err.print(':');
            err.print(RTC.read(RTC.MINUTE), 2);
            err.print(':');
            err.print(RTC.read(RTC.SECOND), 2);

            Kernel.wait(1);
        }


    }
}
