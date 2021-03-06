package drivers.keyboard.layout;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardEvent;

public abstract class KeyboardLayout {
    char[] mappingCaps;
    char[] mappingNormal;
    char[] mappingAltGr;

    public KeyboardLayout(){
        mappingCaps = unescape(getCapsMapping(), true);
        mappingNormal = unescape(getNormalMapping(), true);
        mappingAltGr= unescape(getAltGrMapping(), true);
    }

    public abstract char[] getNormalMapping();
    public abstract char[] getCapsMapping();
    public abstract char[] getAltGrMapping();

    public void setCharOn(KeyboardEvent evt){
        //keyNo -= offset;

        if ((evt.modifiers & Keyboard.MODIFIER_CAPS) != 0){
            evt.setPrintChar(mappingCaps[evt.key]);
        }

        if ((evt.modifiers & Keyboard.MODIFIER_ALT_GR) != 0){
            evt.setPrintChar(mappingAltGr[evt.key]);
        }

        if (evt.modifiers == 0) {
            evt.setPrintChar(mappingNormal[evt.key]);
        }

    }

    public char[] unescape(char[] input, boolean ignoreNewlines){
        // a little to big, but who cares...
        char[] data = new char[input.length];

        boolean escaped = false;
        int j = 0;
        for (int i = 0; i < input.length; i++){
            char c = input[i];

            if (!escaped){
                if (c == '\\'){
                    escaped = true;
                } else if (c == '\n' && ignoreNewlines) {
                    // ignore the newline
                } else {
                    data[j++] = c;
                }

            } else {
                if(c == 't'){
                    data[j++] = '\t';

                } else if (c == '0'){
                    data[j++] = '\0';

                } else if (c == 'n'){
                    data[j++] = '\n';

                } else if (c == '\\'){
                    data[j++] = '\\';
                }

                escaped = false;
            }

        }

        return  data;
    }

}
