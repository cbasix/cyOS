package drivers.virtio.net;

import io.GreenScreenOutput;
import kernel.Kernel;

public class NotifyConfig {
    private int notify_off_multiplier;
    private int notify_cap_offset_within_bar;
    private int notifyBarAddr;
    private int[] queueNotifyOffsets;

    public NotifyConfig(int notifyBarAddr, int notify_cap_offset_within_bar, int notify_off_multiplier, int queueCount) {
        this.notifyBarAddr = notifyBarAddr;
        this.notify_cap_offset_within_bar = notify_cap_offset_within_bar;
        this.notify_off_multiplier = notify_off_multiplier;
        queueNotifyOffsets = new int[queueCount];
    }

    public int getQueueNotifyAddr(int queueNo){
        GreenScreenOutput out = new GreenScreenOutput();
        out.setCursor(0, 0);

        out.print("bar addr ");out.println(String.hexFrom(notifyBarAddr));
        out.print("cap offset ");out.println(String.hexFrom(notify_cap_offset_within_bar));
        out.print("queue no ");out.println(String.hexFrom(queueNo));
        out.print("cap offset ");out.println(String.hexFrom(queueNotifyOffsets[queueNo]));
        out.print("notify off mult ");out.println(String.hexFrom(notify_off_multiplier));

        Kernel.wait(2);
        return notifyBarAddr + notify_cap_offset_within_bar + queueNotifyOffsets[queueNo] * notify_off_multiplier; // todo check
    }

    public void setQueueNotifyOffset(int queueNo, int notifyOffset){
        queueNotifyOffsets[queueNo] = notifyOffset;
    }
}
