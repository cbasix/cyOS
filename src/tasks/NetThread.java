package tasks;

import io.Color;
import io.GreenScreenOutput;
import io.LowlevelOutput;
import kernel.Kernel;
import tasks.shell.commands.Network;

// known bugs:
//  - the last char on the lower right corner can not be deleted.
//  - no navigation
//  - println uses spaces -> must delete each one by itself after using enter

/* simplistic editor that loses state on losing focus
* */
public class NetThread extends Task {

    public NetThread() {}

    public void onTick() {}

    @Override
    public void onBackgroundTick() {
        Network.doReceive(Kernel.networkManager.stack);
    }

    @Override
    public void onStart() {}

    @Override
    public void onStop() {}

    @Override
    public void onFocus() {}

}
