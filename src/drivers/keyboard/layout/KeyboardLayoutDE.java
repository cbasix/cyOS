package drivers.keyboard.layout;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardEvent;

public class KeyboardLayoutDE extends KeyboardLayout{
    public char[] getNormalMapping(){
        return MAGIC.getNamedString("../../blobs/LayoutDeNormal.txt").toChars();
    }

    @Override
    public char[] getCapsMapping() {
        return MAGIC.getNamedString("../../blobs/LayoutDeCaps.txt").toChars();
    }
}
