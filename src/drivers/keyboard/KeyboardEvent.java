package drivers.keyboard;

public class KeyboardEvent {
    public final static short MD_NONE        = (short)0x0000;
    public final static short MD_LEFT_CTRL   = (short)0x0001;
    public final static short MD_RIGHT_CTRL  = (short)0x0002;
    public final static short MD_LEFT_ALT    = (short)0x0004;
    public final static short MD_RIGHT_ALT   = (short)0x0008;
    public final static short MD_LEFT_SHIFT  = (short)0x0010;
    public final static short MD_RIGHT_SHIFT = (short)0x0020;
    public final static short MD_CAPS_LOCK   = (short)0x0040;
    public final static short MD_NUM_LOCK    = (short)0x0080;
    public final static short MD_SCROLL_LOCK = (short)0x0100;
    public final static short MD_LEFT_WIN    = (short)0x0200;
    public final static short MD_RIGHT_WIN   = (short)0x0400;
    public final static short MD_CONTEXT     = (short)0x0800;
    //some useful combinations
    public final static short MD_ANY_CTRL    = (short)0x0003;
    public final static short MD_ANY_ALT     = (short)0x000C;
    public final static short MD_ANY_SHIFT   = (short)0x0030;
    public final static short MD_ANY_WIN     = (short)0x0600;

    //constants for control-keys, mapped in 1-31 of ASCII
    public final static char KC_BACKSPACE = (char)0x0008;
    public final static char KC_TAB       = (char)0x0009;
    public final static char KC_ENTER     = (char)0x000A;
    public final static char KC_ESC       = (char)0x001B;
    //constants for control-keys without mapping
    public final static char KC_UNKNOWN   = (char)0xFF00;
    public final static char KC_F1        = (char)0xFF01;
    public final static char KC_F2        = (char)0xFF02;
    public final static char KC_F3        = (char)0xFF03;
    public final static char KC_F4        = (char)0xFF04;
    public final static char KC_F5        = (char)0xFF05;
    public final static char KC_F6        = (char)0xFF06;
    public final static char KC_F7        = (char)0xFF07;
    public final static char KC_F8        = (char)0xFF08;
    public final static char KC_F9        = (char)0xFF09;
    public final static char KC_F10       = (char)0xFF0A;
    public final static char KC_F11       = (char)0xFF0B;
    public final static char KC_F12       = (char)0xFF0C;
    public final static char KC_PRINT     = (char)0xFF0D;
    public final static char KC_SCROLL    = (char)0xFF0E;
    public final static char KC_PAUSE     = (char)0xFF0F;
    public final static char KC_CURSLEFT  = (char)0xFF10;
    public final static char KC_CURSRIGHT = (char)0xFF11;
    public final static char KC_CURSUP    = (char)0xFF12;
    public final static char KC_CURSDOWN  = (char)0xFF13;
    public final static char KC_HOME      = (char)0xFF14;
    public final static char KC_END       = (char)0xFF15;
    public final static char KC_PAGEUP    = (char)0xFF16;
    public final static char KC_PAGEDOWN  = (char)0xFF17;
    public final static char KC_INS       = (char)0xFF18;
    public final static char KC_DEL       = (char)0xFF19;
    public final static char KC_CENTER    = (char)0xFF1A; //used for '5' with NUM off
    public final static char KC_NUM       = (char)0xFF1B;
    public final static char KC_SYSRQ     = (char)0xFF1C;
    public final static char KC_BREAK     = (char)0xFF1D;
    public final static char KC_LCTRL     = (char)0xFF1E;
    public final static char KC_RCTRL     = (char)0xFF1F;
    public final static char KC_LALT      = (char)0xFF20;
    public final static char KC_RALT      = (char)0xFF21;
    public final static char KC_LSHIFT    = (char)0xFF22;
    public final static char KC_RSHIFT    = (char)0xFF23;
    public final static char KC_CAPSLOCK  = (char)0xFF24;
    public final static char KC_LWIN      = (char)0xFF25;
    public final static char KC_RWIN      = (char)0xFF26;
    public final static char KC_CONTEXT   = (char)0xFF27;


    public int key;
    public int modifiers;
    public char printChar;
    public boolean pressed;
    private boolean printable;

    KeyboardEvent(int key, boolean pressed){
        this.key = key;
        this.pressed = pressed;
    }

    KeyboardEvent(int key, boolean pressed, int modifiers){
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
