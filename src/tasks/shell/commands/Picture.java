package tasks.shell.commands;

import io.Screen;
import kernel.Kernel;
import datastructs.RingBuffer;

public class Picture extends Command {

    @Override
    public String getCmd() {
        return "picture";
    }

    @Override
    public void execute(RingBuffer shellOutput, String[] args) {



        Screen screen = new Screen();
        screen.switchToGraphics();

        if (args.length > 1){
            if (args[1].equals("grey")) {
                screen.showGreyscaleSquirrelPicture();
            } else {
                screen.showColorPicture();
            }
        } else {
            screen.showSquirrelPicture();
        }
        //screen.erase();


        Kernel.wait(8);
        screen.switchToTextMode();



    }
}
