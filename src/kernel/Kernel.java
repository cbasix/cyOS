package kernel;


import io.*;
import rte.DynamicRuntime;

public class Kernel {

  private static int vidMemCursor=0xB8000;

  public static class Test{
    private int data;
    public void setData(int data){
      this.data = data;
    }
    public int getData(){
      return data;
    }
  }

  public static void main() {
    MAGIC.doStaticInit();
    DynamicRuntime.initializeMemoryPointers();

    GreenScreenDirect.printInt(MAGIC.rMem32(MAGIC.imageBase), 10, 10, 0, Color.RED);
    GreenScreenDirect.printInt(MAGIC.rMem32(MAGIC.imageBase+4), 10, 10, 80, Color.RED);

    wait(1);
    GreenScreenDirect.printInt(3333, 10, 10, 160, Color.RED);

    run();

    // remind myself that i forgot to uncomment the run method above...
    while (true){
      debug("YOU SHALL NEVER GET HERE");
    }
  }

  public static void run() {

    GreenScreenDirect.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printStr("Welcome to cyOS", 10, 4, GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printStr("Loading ", 12, 6, GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printChar('%', 22, 6, GreenScreenConst.DEFAULT_COLOR);


    int i = 0;
    while(true){

      // update percent number
      GreenScreenDirect.printInt(i, 10, 2, 20, 6, GreenScreenConst.DEFAULT_COLOR);
      GreenScreenDirect.printInt(i, 16, 2, 20, 7, GreenScreenConst.DEFAULT_COLOR);

      //update seconds
      GreenScreenDirect.printStr("Sec", 27, 0, GreenScreenConst.DEFAULT_COLOR);
      GreenScreenDirect.printInt(RTC.read(RTC.SECOND), 2, 8, 31, 0, GreenScreenConst.DEFAULT_COLOR);

      // update next free addr
      GreenScreenDirect.printStr("Next Free",  0, 1, GreenScreenConst.DEFAULT_COLOR);
      GreenScreenDirect.printInt(DynamicRuntime.getNextFreeAddr(), 10, 9, 10, 1, GreenScreenConst.DEFAULT_COLOR);

      debug("Kernel A");


      Test t1 = null;

      // after four ticks start doing something usefull
      if (i % 100 == 4) {

        /*GreenScreenOutput out = new GreenScreenOutput();
        out.setColor(Color.GREEN, Color.GREY);
        out.setCursor(0, 10);
        GreenScreenOutput err = new GreenScreenOutput();
        err.setColor(Color.RED, Color.BLACK);*/
        debug("Kernel X");
        wait(1);

        t1 = new Test();
        //Test t2 = new Test();


        // print status
        debug("Kernel Y");

        /*out.printHex(i);
        out.print("It is done. 42 Reached. ");
        out.print(t1.data);
        out.print(" ");
        out.print(t2.data);
        out.print(" ");
        out.println();
        out.print("Next line.");
        out.print("Second sentence.");
        err.println();
        err.print("Error message.");
        out.print("Normal Text");*/


      }
      if (i > 4){
        t1.setData(i);
      }



      debug("Kernel E");
      //continue;
      printHexdump();

      if (i % 100 == 5) {
        // stop here to inspect hexdump
        debug("Stopped for inspection");
        while (true){}
      }

      //while (true){};
      debug("Kernel F");
      wait(1);

      i++;
    }

  }

  // TODO this is not exact! up to 1000ms not exact.
  public static void wait(int delayInSeconds) {
    //GreenScreenDirect.printInt(88, 10, 3, 0, 19, Color.GREEN);
    int diffCount = 0;
    int lastSeen = RTC.read(RTC.SECOND);
    while (diffCount < delayInSeconds){
      int currentSec = RTC.read(RTC.SECOND);
      if (lastSeen != currentSec){
        lastSeen = currentSec;
        diffCount++;
      }
      //GreenScreenDirect.printInt(diffCount, 10, 3, 0, 20, Color.GREEN);
    };
  }

  private static void printHexdump() {
    //GreenScreenDirect.print("Starting hexdump", 2, 23, Color.CYAN); Kernel.wait(3);

    DynamicRuntime.ImageInfo image = (DynamicRuntime.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
    int startAddr = image.start + image.size + DynamicRuntime.SAFETY_DISTANCE;
    // allign to 4 byte, (last three address bits zero)
    startAddr = ((startAddr + 0x7) &~ 0x7) - 8; // show the last 8 bits before start too
    debug("Hexdmp A");
    for (int i = 0; i < GreenScreenConst.HEIGHT*4; i++){
      if (i % 4 == 0){
        debug("Hexdmp B");
        GreenScreenDirect.printInt(startAddr + i, 10, 8, 39, GreenScreenConst.HEIGHT-1-i/4, GreenScreenConst.ERROR_COLOR);
        debug("Hexdmp C");
        GreenScreenDirect.printInt((int)MAGIC.rMem32(startAddr+i), 10, 8, 70, GreenScreenConst.HEIGHT-1-i/4, GreenScreenConst.DEFAULT_COLOR);
      }
      debug("Hexdmp D");
      byte t = MAGIC.rMem8(startAddr + i);
      GreenScreenDirect.printInt(t,16, 2, 50+(i%4)*4, GreenScreenConst.HEIGHT-1-i/4, GreenScreenConst.DEFAULT_COLOR);
      //GreenScreenDirect.printInt(t,10, 3, 50, 0, GreenScreenConst.DEFAULT_COLOR);
    }
    /*DynamicRuntime.HexDump dump = (DynamicRuntime.HexDump) MAGIC.cast2Struct(startAddr);

    GreenScreenDirect.printStr("hexdump printing", 2, 23, Color.CYAN); Kernel.wait(3);

    for (int d = 0; d < DynamicRuntime.HexDump.size; d++){
      if (d % 8 == 0){
        GreenScreenDirect.printStr("print addr", 2, 23, Color.CYAN); Kernel.wait(3);
        // print addr
        GreenScreenDirect.printInt(startAddr + d, 10, 10, 39, d/8, GreenScreenConst.ERROR_COLOR);
      }
      GreenScreenDirect.printStr("print data", 2, 23, Color.CYAN); Kernel.wait(3);
      // print memory data as hex
      GreenScreenDirect.printInt(dump.expl[d],16, 2, 50+(d%8)*3, d/8, GreenScreenConst.DEFAULT_COLOR);
    }*/
  }
  public static void debug(String str) {
    GreenScreenDirect.printStr(str, 0, 0, GreenScreenConst.DEFAULT_COLOR);
    //wait(1);
  }

}
