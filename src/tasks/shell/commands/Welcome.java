package tasks.shell.commands;

import io.Color;
import io.Screen;
import kernel.Kernel;
import kernel.datastructs.RingBuffer;

public class Welcome extends Command {

    @Override
    public String getCmd() {
        return "welcome";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {
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
