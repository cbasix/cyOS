package drivers.keyboard;

public class KeyboardEvent {
    public int key;
    public int modifiers;
    public char printChar;
    public boolean pressed;
    private boolean printable;

    public KeyboardEvent(){}

    KeyboardEvent(int key, boolean pressed){
        reinit(key, pressed);
    }

    KeyboardEvent(int key, boolean pressed, int modifiers){
        reinit(key, pressed, modifiers);
    }

    public void reinit(int key, boolean pressed){
        reinit(key, pressed, 0);
    }

    public void reinit(int key, boolean pressed, int modifiers){
        this.key = key;
        this.pressed = pressed;
        this.modifiers = modifiers;
    }

    public void setPrintChar(char s) {
        printChar = s;
        if (s != '\0'){
            printable = true;
        }
    }

    public char getPrintChar(){
        return printChar;
    }

    public boolean isPrintable(){
        return printable;
    }
}
