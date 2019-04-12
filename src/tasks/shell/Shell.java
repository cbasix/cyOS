package tasks.shell;

import drivers.keyboard.KeyboardEvent;
import io.Color;
import io.GreenScreenOutput;
import kernel.datastructs.RingBuffer;
import tasks.LogEvent;
import tasks.Task;
import tasks.shell.commands.Command;
import tasks.shell.commands.Echo;
import tasks.shell.commands.Interrupt;
import tasks.shell.commands.Welcome;

/**
 * Quick and dirty shell application
 */
public class Shell extends Task {
    public static final int OUTPUT_AREA_LINES = 22;
    RingBuffer outputBuffer = new RingBuffer(100);
    char[] currentCommand = new char[79];
    int currentPos = 1;
    Command[] registeredCommands;

    GreenScreenOutput outputArea = new GreenScreenOutput();
    GreenScreenOutput inputArea = new GreenScreenOutput();
    GreenScreenOutput statusArea = new GreenScreenOutput();

    public Shell(){
        // register default commands
        registeredCommands = new Command[3];
        registeredCommands[0] = new Echo();
        registeredCommands[1] = new Welcome();
        registeredCommands[2] = new Interrupt();


        inputArea.setColor(Color.BLACK, Color.GREY);
        outputArea.setColor(Color.GREY, Color.BLACK);
        statusArea.setCursor(Color.BLACK, Color.CYAN);
    }

    public void registerCommands(){

    }

    public void onStart(){
        // input symbol
        currentCommand[0] = '>';
        for (int i = 1; i < currentCommand.length; i++) {
            currentCommand[i] = ' ';
        }

        drawOutputArea();
        drawCommandArea();
        drawStatusArea();
    }

    public void onStop(){

    }

    public void onTick() {
        Object e = stdin.get();

        if (e instanceof KeyboardEvent) {
            KeyboardEvent k = (KeyboardEvent) e;

            if (k.pressed) {
                if (k.isPrintable()) {
                    currentCommand[currentPos] = k.getPrintChar();
                    if (currentPos < currentCommand.length) {
                        currentPos++;
                    }
                    drawCommandArea();
                }
                if (k.key == 28) {
                    execute(currentCommand);
                    currentPos = 1;
                    for (int i = 1; i < currentCommand.length; i++) {
                        currentCommand[i] = ' ';
                    }
                    drawCommandArea();
                }
                if (k.key == 14) {
                    if (currentPos > 1) {
                        currentPos--;
                        currentCommand[currentPos] = ' ';
                        drawCommandArea();
                    }
                }
            }

        } else if (e instanceof LogEvent) {
            LogEvent l = (LogEvent) e;
            outputBuffer.push(l.message);
            drawOutputArea();
        }
    }

    private void drawOutputArea() {
        outputArea.setCursor(0, 1);
        int lines = 0;
        int oldest_cmd = 0;
        // find out how many cmds fit into output area
        String cmd = null;
        do {
             cmd = (String) outputBuffer.peekPushed(oldest_cmd);
             if (cmd != null) {
                 lines += (cmd.length() / 80) + 1;
                 oldest_cmd++;
             }
        } while (lines < OUTPUT_AREA_LINES && cmd != null);

        for (int i = oldest_cmd; i >= 0; i--){
            outputArea.println((String) outputBuffer.peekPushed(i));
        }

        while (lines < OUTPUT_AREA_LINES){
            outputArea.println();
            lines++;
        }
        //outputArea.print((char []) outputBuffer.peekPushed(0));

    }

    private void drawCommandArea() {
        inputArea.setCursor(0, 24);
        inputArea.println(currentCommand);
    }

    private void drawStatusArea(){
        statusArea.setCursor(0,0);
        statusArea.println("                                   cySHELL 0.1");
    }

    private void execute(char[] command){
        String s = new String(command);
        // show command too
        outputBuffer.push(s);

        String[] cmd = s.substring(1).trim().split(' ');

        boolean handled = false;
        for(Command c: registeredCommands){
            if (cmd[0].equals(c.getCmd())){

                c.execute(this.stdin, cmd);
                handled = true;
                break;
            }
        }
        if (!handled){
            outputBuffer.push("Unknown command");
        }


        drawOutputArea();
        drawCommandArea();
        drawStatusArea();
    }

}
