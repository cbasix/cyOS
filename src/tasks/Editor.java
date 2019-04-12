package tasks;

import drivers.keyboard.KeyboardEvent;
import io.GreenScreenOutput;

public class Editor extends Task{
    GreenScreenOutput out = new GreenScreenOutput();

    public void onTick() {
        KeyboardEvent k = (KeyboardEvent) stdin.get();
        if (k != null && k.pressed){
            if (k.isPrintable()) {
                /*out.print(" <Key: ");
                out.printHex(k.key);
                out.print(" Pressed: ");
                out.print(k.pressed);
                out.print(" PrintStr: ");*/
                out.print(k.getPrintChar());
                /*out.print(" Modifier: ");
                out.print(k.modifiers);
                out.println(">");*/
            }
            if (k.key == 14){
                out.setCursor(out.getCursor()-1);
                out.print(' ');
                out.setCursor(out.getCursor()-1);
            }
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

}
