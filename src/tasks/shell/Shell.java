package tasks.shell;

import drivers.keyboard.Keyboard;
import drivers.keyboard.KeyboardEvent;
import drivers.virtio.VirtIo;
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
    private static final int OUTPUT_AREA_LINES = 22;
    private static final int COLOR_HIGHLIGHTED =  Color.BLACK << 4 | Color.CYAN;
    private static final int COLOR_NORMAL = Color.BLACK << 4 | Color.GREY;
    private static final int SCROLL_BUFFER_SIZE = 300;

    private RingBuffer outputBuffer = new RingBuffer(SCROLL_BUFFER_SIZE);
    private char[] currentCommand = new char[GreenScreenOutput.WIDTH-1];
    private int currentCommandPos = 1;
    private int scrollOffset = 0;
    private int backgroundTickCount = 0;
    private CommandArrayList registeredCommands;

    private GreenScreenOutput outputArea = new GreenScreenOutput();
    private GreenScreenOutput inputArea = new GreenScreenOutput();
    private GreenScreenOutput statusArea = new GreenScreenOutput();

    public Shell(){
        // register default commands
        registeredCommands = new CommandArrayList();
        registeredCommands.add(new Echo());
        registeredCommands.add(new Picture());
        registeredCommands.add(new Interrupt());
        registeredCommands.add(new CatFileTest());
        registeredCommands.add(new ExecuteTask());
        registeredCommands.add(new Smap());
        registeredCommands.add(new CharTest());
        registeredCommands.add(new Mem());
        registeredCommands.add(new GarbageCollection());
        registeredCommands.add(new Nullpointer());
        registeredCommands.add(new NullRead());
        registeredCommands.add(new NullWrite());
        registeredCommands.add(new TextualCall());
        registeredCommands.add(new GarbageCollectionInfo());
        registeredCommands.add(new PciScan());
        registeredCommands.add(new LoadVirtioNetDriver());
        registeredCommands.add(new Arp());
        registeredCommands.add(new Network());

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
        outputBuffer.push("");
        outputBuffer.push("        +-----------------------------------------------------------+");
        outputBuffer.push("        | Welcome. Type 'help' to get a list of available commands. |");
        outputBuffer.push("        +-----------------------------------------------------------+");

        Kernel.taskManager.requestFocus(this);
    }

    private void draw(){

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
        if (backgroundTickCount > 0){
            outputBuffer.push(String.concat("You spend x ticks outside of the Shell. x = ", String.from(backgroundTickCount)));
        }
        draw();
        // todo remove when virtio testing ready
        execute(">virtio".toChars());
        execute(">arp announce".toChars());
        execute(">net receive".toChars());
    }

    // todo bug last char not deletable
    // todo using space is not optimal
    public void onTick() {
        Object e = stdin.get();

        if (e instanceof KeyboardEvent) {
            KeyboardEvent k = (KeyboardEvent) e;

            if (k.pressed) {
                if (k.isPrintable()) {
                    currentCommand[currentCommandPos] = k.getPrintChar();
                    if (currentCommandPos < currentCommand.length - 1) {
                        currentCommandPos++;
                    }
                    drawCommandArea();
                }
                if (k.key == Keyboard.ENTER) {
                    execute(currentCommand);
                    currentCommandPos = 1;
                    for (int i = 1; i < currentCommand.length; i++) {
                        currentCommand[i] = ' ';
                    }
                    drawCommandArea();
                }
                if (k.key == Keyboard.BACKSP) {
                    if (currentCommandPos > 1) {
                        currentCommandPos--;
                        currentCommand[currentCommandPos] = ' ';
                        drawCommandArea();
                    }
                }
                if (k.key == Keyboard.UP) {
                    scrollOffset++;
                    if (scrollOffset > SCROLL_BUFFER_SIZE){
                        scrollOffset = SCROLL_BUFFER_SIZE;
                    }
                    drawOutputArea();
                }

                if (k.key == Keyboard.DOWN) {
                    scrollOffset--;
                    if (scrollOffset < 0){
                        scrollOffset = 0;
                    }
                    drawOutputArea();
                }
                if (k.key == Keyboard.PG_UP) {
                    scrollOffset+=10;
                    if (scrollOffset > SCROLL_BUFFER_SIZE){
                        scrollOffset = SCROLL_BUFFER_SIZE;
                    }
                    drawOutputArea();
                }

                if (k.key == Keyboard.PG_DOWN) {
                    scrollOffset-=10;
                    if (scrollOffset < 0){
                        scrollOffset = 0;
                    }
                    drawOutputArea();
                }
            }

        } else if (e instanceof LogEvent) {
            LogEvent l = (LogEvent) e;
            outputBuffer.push(l.message);
            drawOutputArea();
        }
        backgroundTickCount = 0;
    }

    @Override
    public void onBackgroundTick() {
        backgroundTickCount++;

    }

    private void drawOutputArea() {
        outputArea.setCursor(0, 1);
        int lines = 0;
        // todo bug if output buffer is empty and scrolling
        int oldest_cmd = scrollOffset;
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

        for (int i = oldest_cmd; i >= scrollOffset; i--){
            cmd = (String) outputBuffer.peekPushed(i);
            if (cmd != null) {

                if (cmd.length() > 0 && cmd.charAt(0) == '>') {
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
        inputArea.setCursor(currentCommandPos, 24);
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
        scrollOffset = 0;
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
