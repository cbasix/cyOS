package tasks;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardEvent;
import io.Color;
import io.GreenScreenOutput;
import io.LowlevelLogging;
import io.LowlevelOutput;
import kernel.Kernel;
import tasks.Task;

/* simplistic editor that loses state on losing focus
* */
public class Editor extends Task {
    GreenScreenOutput out = new GreenScreenOutput();

    public void onTick() {
        KeyboardEvent k = (KeyboardEvent) stdin.get();
        if (k != null && k.pressed){
            if (k.isPrintable()) {
                out.print(k.getPrintChar());
            }
            if (k.key == Keyboard.BACKSP){
                out.setCursor(out.getCursor()-1);
                out.print(' ');
                out.setCursor(out.getCursor()-1);

            } else if (k.key == Keyboard.ESC){
                Kernel.taskManager.requestStop(this);

            } else if (k.key == Keyboard.ENTER){
                out.println();
            }
        }
    }

    @Override
    public void onBackgroundTick() {

    }

    @Override
    public void onStart() {
        Kernel.taskManager.requestFocus(this);
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onFocus() {
        LowlevelOutput.clearScreen(Color.RED);
        out.setCursor(0);
    }

}
