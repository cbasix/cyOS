package tasks.shell;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardEvent;
import io.*;
import kernel.Kernel;
import datastructs.RingBuffer;
import datastructs.subtypes.CommandArrayList;
import tasks.LogEvent;
import tasks.Task;
import tasks.shell.commands.*;


/**
 * Quick and dirty shell application
 */
public class Shell extends Task {
    public static final int OUTPUT_AREA_LINES = 22;
    public static final int COLOR_HIGHLIGHTED =  Color.BLACK << 4 | Color.CYAN;
    public static final int COLOR_NORMAL = Color.BLACK << 4 | Color.GREY;

    RingBuffer outputBuffer = new RingBuffer(100);
    char[] currentCommand = new char[GreenScreenOutput.WIDTH-1];
    int currentPos = 1;
    CommandArrayList registeredCommands;

    GreenScreenOutput outputArea = new GreenScreenOutput();
    GreenScreenOutput inputArea = new GreenScreenOutput();
    GreenScreenOutput statusArea = new GreenScreenOutput();

    public Shell(){
        // register default commands
        int i = 0;
        registeredCommands = new CommandArrayList();
        registeredCommands.add(new Echo());
        registeredCommands.add(new Picture());
        registeredCommands.add(new Interrupt());
        registeredCommands.add(new CatFileTest());
        registeredCommands.add(new ExecuteTask());
        registeredCommands.add(new Smap());
        registeredCommands.add(new CharTest());
        registeredCommands.add(new Mem());

        inputArea.setColor(Color.BLACK, Color.GREY);
        outputArea.setColorState(COLOR_NORMAL);
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
        // todo BUG: out of bounds on empty string
        outputBuffer.push("\0");
        outputBuffer.push("          +--------------------------------------------------------+");
        outputBuffer.push("          | Welcome. Type 'help' to get a list available commands. |");
        outputBuffer.push("          +--------------------------------------------------------+");

        Kernel.taskManager.requestFocus(this);
    }

    public void draw(){

        LowlevelOutput.disableCursor();
        LowlevelOutput.clearScreen(COLOR_NORMAL);
        drawOutputArea();
        drawStatusArea();
        drawCommandArea();

    }

    public void onStop(){

    }

    @Override
    public void onFocus() {
        draw();
    }

    // todo bug last char not deletable
    // todo using space is not optimal
    public void onTick() {
        Object e = stdin.get();

        if (e instanceof KeyboardEvent) {
            KeyboardEvent k = (KeyboardEvent) e;

            if (k.pressed) {
                if (k.isPrintable()) {
                    currentCommand[currentPos] = k.getPrintChar();
                    if (currentPos < currentCommand.length - 1) {
                        currentPos++;
                    }
                    drawCommandArea();
                }
                if (k.key == Keyboard.ENTER) {
                    execute(currentCommand);
                    currentPos = 1;
                    for (int i = 1; i < currentCommand.length; i++) {
                        currentCommand[i] = ' ';
                    }
                    drawCommandArea();
                }
                if (k.key == Keyboard.BACKSP) {
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

    @Override
    public void onBackgroundTick() {

    }

    private void drawOutputArea() {
        outputArea.setCursor(0, 1);
        int lines = 0;
        int oldest_cmd = 0;
        // find out how many cmds fit into output area
        // todo fix overflow if one single command has to much lines
        String cmd = null;
        do {
             cmd = (String) outputBuffer.peekPushed(oldest_cmd);
             if (cmd != null) {
                 lines += (cmd.length() / 80) + 1;
                 lines += cmd.countOccurences('\n');
                 oldest_cmd++;
             }
        } while (lines < OUTPUT_AREA_LINES && cmd != null);
        for (int i = oldest_cmd; i >= 0; i--){
            cmd = (String) outputBuffer.peekPushed(i);
            if (cmd != null) {

                if (cmd.charAt(0) == '>') {
                    outputArea.setColorState(COLOR_HIGHLIGHTED);
                } else {
                    outputArea.setColorState(COLOR_NORMAL);
                    cmd = String.concat(" ", cmd);
                }
                outputArea.println(cmd);
            }
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

        // blinking cursor
        inputArea.setCursor(currentPos, 24);
        inputArea.setColorState(inputArea.getColorState() | Color.MOD_BLINK);
        inputArea.print(' ');
        inputArea.setColorState(inputArea.getColorState() & ~Color.MOD_BLINK);
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

        if(cmd[0].equals("help")){
            printHelp();
            handled = true;
        }

        for(int i = 0; i < registeredCommands.size(); i++){
            Command c = registeredCommands.get(i);
            if (cmd[0].equals(c.getCmd())){

                c.execute(this.stdin, cmd);
                handled = true;
                break;
            }
        }
        if (!handled){
            outputBuffer.push("Unknown command");
        }

        draw();
    }

    private void printHelp() {
        outputBuffer.push("Available commands:");
        for(int i = 0; i < registeredCommands.size(); i++){
            Command c = registeredCommands.get(i);
            outputBuffer.push(String.concat("- ", c.getCmd()));
        }
    }

}
