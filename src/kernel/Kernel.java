package kernel;


import io.*;
import rte.DynamicRuntime;

public class Kernel {

  private static int vidMemCursor=0xB8000;

  public static final int FINER = 2;
  public static final int FINE = 3;
  public static final int INFO = 4;
  public static final int ERROR = 5;

  private static int dumpStartAddr = 1024;

  private static final int DEBUG_LEVEL = ERROR;

  public static void main() {
    DynamicRuntime.initializeMemoryPointers();
    MAGIC.doStaticInit();

    GreenScreenDirect.clearScreen(GreenScreenConst.DEFAULT_COLOR);

    run_output();
    wait(2);
    run_allocation();

    // remind myself that i forgot to uncomment one of the run methods above...
    while (true){
      debug("Please uncomment one of the run methods within the main method (or forgot loop?)", ERROR);
    }
  }

  public static void run_allocation() {
    GreenScreenDirect.clearScreen(GreenScreenConst.DEFAULT_COLOR);

    // set hexdump start to the address where our new objects will be created
    dumpStartAddr = DynamicRuntime.getNextFreeAddr();

    GreenScreenDirect.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printStr("Welcome to cyOS", 10, 4, GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printStr("Loading ", 12, 6, GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printChar('%', 22, 6, GreenScreenConst.DEFAULT_COLOR);


    int tick = 0;

    Test t2 = null;

    while(true){

      // update percent number
      GreenScreenDirect.printInt(tick, 10, 3, 19, 6, GreenScreenConst.DEFAULT_COLOR);
      //GreenScreenDirect.printInt(tick-50, 16, 3, 19, 7, GreenScreenConst.DEFAULT_COLOR);

      //update seconds
      GreenScreenDirect.printStr("Sec", 27, 0, GreenScreenConst.DEFAULT_COLOR);
      GreenScreenDirect.printInt(RTC.read(RTC.SECOND), 2, 8, 31, 0, GreenScreenConst.DEFAULT_COLOR);

      // update next free addr
      //GreenScreenDirect.printStr("Next Free",  0, 1, GreenScreenConst.DEFAULT_COLOR);
      //GreenScreenDirect.printInt(DynamicRuntime.getNextFreeAddr(), 10, 9, 10, 1, GreenScreenConst.DEFAULT_COLOR);

      debug("Kernel A", FINE);


      Test t1 = new Test();


      // after one tick start doing something usefull
      if (tick  == 1) {

        debug("Kernel X", FINE);
        wait(1);

        //t1 = ;
        t2 = new Test();


        // print status
        debug("Kernel Y", FINE);
      }

      if (tick >= 2){
        debug("Ticking T2", INFO);
        t2.setData(tick);
        debug("Testing T2", INFO);
        if (tick == t2.getData()){
            debug("Tick SUCCESS", INFO);
        } else {
            debug("Something is seriously wrong with this object instance ", ERROR);
        }

        GreenScreenDirect.printStr("T1 addr", 0, 13, Color.PINK);
        GreenScreenDirect.printInt(MAGIC.addr(t1), 10, 10, 15, 13, Color.PINK);
        GreenScreenDirect.printInt(MAGIC.rMem32(MAGIC.addr(t1)), 10, 10, 15, 13, Color.PINK);

        GreenScreenDirect.printStr("T2 addr", 0, 14, Color.PINK);
        GreenScreenDirect.printInt(MAGIC.addr(t2), 10, 10, 15, 14, Color.PINK);
        GreenScreenDirect.printInt(MAGIC.rMem32(MAGIC.addr(t2)), 10, 10, 15, 14, Color.PINK);
      }

      debug("Kernel E", FINE);
      printHexdump();

      if (tick == 4) {
        // stop here to inspect hexdump
        //debug("Stopped for inspection", INFO);
        //while (true){}
      }

      //while (true){};
      debug("Kernel F", FINE);
      wait(1);

      tick++;
    }

  }

  public static void run_output(){
      GreenScreenDirect.clearScreen(GreenScreenConst.DEFAULT_COLOR);

      GreenScreenOutput out = new GreenScreenOutput();
      out.setCursor(0, 3);
      out.setColor(Color.BLACK, Color.CYAN);

      GreenScreenOutput err = new GreenScreenOutput();
      err.setCursor(0, 10);
      err.setColor(Color.RED, Color.BLACK);

      // write some stuff
      out.print("Lorem ipsum dolor sit amet. slsdfasfeasf");
      //out.print("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis elit orci, porta commodo suscipit eget, aliquet quis est.");

      err.print(42); err.printHex(42); err.println();
      err.print(-42);

      for (int i = 0; i < 4; i++){
          // clock, something is wrong with how i expect the rtc data... but who cares. at least seconds change every second and minutes every minute.
          GreenScreenDirect.printInt(RTC.read(RTC.HOUR), 10, 3, 72, 24, GreenScreenConst.DEFAULT_COLOR);
          GreenScreenDirect.printInt(RTC.read(RTC.MINUTE), 10, 3, 74, 24, GreenScreenConst.DEFAULT_COLOR);
          GreenScreenDirect.printInt(RTC.read(RTC.SECOND), 10, 3, 77, 24, GreenScreenConst.DEFAULT_COLOR);
          GreenScreenDirect.printChar(':', 74, 24, GreenScreenConst.DEFAULT_COLOR);
          GreenScreenDirect.printChar(':', 77, 24, GreenScreenConst.DEFAULT_COLOR);
          wait(1);
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

    debug("Hexdmp A", FINER);
    for (int i = 0; i < GreenScreenConst.HEIGHT*4; i++){
      if (i % 4 == 0){
        debug("Hexdmp B", FINER);
        GreenScreenDirect.printInt(dumpStartAddr + i, 10, 8, 39, GreenScreenConst.HEIGHT-1-i/4, GreenScreenConst.ERROR_COLOR);
        debug("Hexdmp C", FINER);
        int v = MAGIC.rMem32(dumpStartAddr +i);
          debug("Hexdmp D", FINER);
        GreenScreenDirect.printInt(v, 10, 8, 70, GreenScreenConst.HEIGHT-1-i/4, GreenScreenConst.DEFAULT_COLOR);
      }
      debug("Hexdmp X", FINER);
      byte t = MAGIC.rMem8(dumpStartAddr + i);
      debug("Hexdmp Y", FINER);
      GreenScreenDirect.printHex(t, 2, 50+(i%4)*4, GreenScreenConst.HEIGHT-1-i/4, GreenScreenConst.DEFAULT_COLOR);
      //GreenScreenDirect.printInt(t,10, 3, 50, 0, GreenScreenConst.DEFAULT_COLOR);
    }
    /*DynamicRuntime.HexDump dump = (DynamicRuntime.HexDump) MAGIC.cast2Struct(dumpStartAddr);

    GreenScreenDirect.printStr("hexdump printing", 2, 23, Color.CYAN); Kernel.wait(3);

    for (int d = 0; d < DynamicRuntime.HexDump.size; d++){
      if (d % 8 == 0){
        GreenScreenDirect.printStr("print addr", 2, 23, Color.CYAN); Kernel.wait(3);
        // print addr
        GreenScreenDirect.printInt(dumpStartAddr + d, 10, 10, 39, d/8, GreenScreenConst.ERROR_COLOR);
      }
      GreenScreenDirect.printStr("print data", 2, 23, Color.CYAN); Kernel.wait(3);
      // print memory data as hex
      GreenScreenDirect.printInt(dump.expl[d],16, 2, 50+(d%8)*3, d/8, GreenScreenConst.DEFAULT_COLOR);
    }*/
  }
  public static void debug(String str, int lvl) {
    if (lvl >= DEBUG_LEVEL) {
      GreenScreenDirect.printStr(str, 0, 0, GreenScreenConst.DEFAULT_COLOR);
      wait(1);
    }
  }

}
