package kernel;


import io.Color;
import io.GreenScreenConst;
import io.GreenScreenDirect;
import io.GreenScreenOutput;
import rte.DynamicRuntime;

public class Kernel {

  private static int vidMemCursor=0xB8000;

  public static class Test{
    public int data;
  }

  public static void main() {
    DynamicRuntime.initializeMemoryPointers();
    MAGIC.doStaticInit();

    GreenScreenDirect.clearScreen(GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printStr("Welcome to cyOS", 10, 4, GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printStr("Loading ", 12, 6, GreenScreenConst.DEFAULT_COLOR);
    GreenScreenDirect.printChar('%', 22, 6, GreenScreenConst.DEFAULT_COLOR);

    int i = 0;
    while(true){

      // update percent number
      GreenScreenDirect.printInt(i, 10, 2, 20, 6, GreenScreenConst.DEFAULT_COLOR);
      GreenScreenDirect.printInt(i, 16, 2, 20, 7, GreenScreenConst.DEFAULT_COLOR);
      Test t1 = null;
      // after four ticks start doing something usefull
      if (i % 100 == 4) {

        /*GreenScreenOutput out = new GreenScreenOutput();
        out.setColor(Color.GREEN, Color.GREY);
        out.setCursor(0, 10);
        GreenScreenOutput err = new GreenScreenOutput();
        err.setColor(Color.RED, Color.BLACK);*/

        t1 = new Test();
        //Test t2 = new Test();


        // print status
        GreenScreenDirect.printStr("Kernel Main here commander!", 2, 23, Color.CYAN);
        //GreenScreenDirect.printInt(MAGIC.addr(out._r_next), 10, 10, 2, 24, Color.CYAN);
        Kernel.wait(1);

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
        t1.data = i;
      }

      printHexdump();
      wait(1);
      i++;
    }

  }

  public static void wait(int sec) {
    //slow it down
    int i, j;
    for(i = 0; i < 10000*sec; i++){
      for(j = 0; j < 10000; j++) {
        //put nop here
      }
    }
  }

  private static void printHexdump() {
    //GreenScreenDirect.print("Starting hexdump", 2, 23, Color.CYAN); Kernel.wait(3);

    DynamicRuntime.ImageInfo image = (DynamicRuntime.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
    int startAddr = image.start + image.size;
    // allign to 4 byte, (last three address bits zero)
    startAddr = ((startAddr + 0x7) &~ 0x7) - 8; // show the last 8 bits before start too

    for (int i = 0; i < GreenScreenConst.HEIGHT*4; i++){
      if (i % 4 == 0){
        GreenScreenDirect.printInt(startAddr + i, 10, 8, 39, GreenScreenConst.HEIGHT-i/4, GreenScreenConst.ERROR_COLOR);
        GreenScreenDirect.printInt(MAGIC.rMem32(startAddr+i), 10, 8, 70, GreenScreenConst.HEIGHT-i/4, GreenScreenConst.DEFAULT_COLOR);
      }
      byte t = MAGIC.rMem8(startAddr + i);
      GreenScreenDirect.printInt(t,16, 2, 50+(i%4)*4, GreenScreenConst.HEIGHT-i/4, GreenScreenConst.DEFAULT_COLOR);
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

}
