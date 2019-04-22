package tasks;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardEvent;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelOutput;
import kernel.Kernel;

// known bugs:
//  - the last char on the lower right corner can not be deleted.
//  - no navigation
//  - println uses spaces -> must delete each one by itself after using enter

/* simplistic editor that loses state on losing focus
* */
public class Blocking extends Task {
    GreenScreenOutput out = new GreenScreenOutput();

    public Blocking() {
        out.setColorState(Color.ERROR_COLOR);
    }

    public void onTick() {
        int i = 0;
        while (true){
            out.print(i);
            out.setCursor(37, 12);
            i++;
            Kernel.hlt();// energy saving is important even in blocking tasks ;)
            // the problem is, that my cpu fan is annoying me if one core is running with full speed...
            // and that happens when qemu is within normal while true;
        }
    }

    @Override
    public void onBackgroundTick() {}

    @Override
    public void onStart() {
        Kernel.taskManager.requestFocus(this);
    }

    @Override
    public void onStop() {}

    @Override
    public void onFocus() {
        LowlevelOutput.clearScreen(Color.ERROR_COLOR);
    }

}
