package apps;

import io.Screen;
import kernel.Kernel;

public class WelcomeApp {
    public static void run(){
        rte.BIOS.regs.EAX=0x0013;
        rte.BIOS.rint(0x10);

        Screen screen = new Screen();
        screen.showWelcomePicture();
        //screen.erase();

        Kernel.wait(4);

        rte.BIOS.regs.EAX=0x0003;
        rte.BIOS.rint(0x10);
    }
}
