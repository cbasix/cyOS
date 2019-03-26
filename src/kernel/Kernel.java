package kernel;


public class Kernel {
  private static final int vidMemBase=0xB8000;
  private static int vidMemCursor=0xB8000;

  public static class Test{
    public int data;
  }

  public static void main() {
    rte.DynamicRuntime.initializeMemoryPointers();
    clearScreen();
    print("Welcome to cyOS", 33, 10);
    int i = 0;
    int z0, z1 = 0;
    print("Loading ", 35, 12);
    print('%', 45, 12);
    while(true){
      //simple int to string conversion for the last two positions of i
      z0 = i % 10;
      z1 = (i / 10) % 10;
      print((char) (48+z1), 43, 12);
      print((char) (48+z0), 44, 12);
      i++;
      //slow the prog down
      int j;
      for(j = 0; j < 100000000; j++){
      }
      if (i > 4) {
        Test t = new Test();
        t.data = i;
        z0 = i % 10;
        z1 = (i / 10) % 10;
        print((char) (48+z1), 43, 20);
        print((char) (48+z0), 44, 20);
      }

    }

  }
  public static void print(String str, int x, int y) {
    int i;
    for (i=0; i<str.length(); i++) {
      print(str.charAt(i), x+i, y);
    }
  }
  public static void print(char c, int x, int y) {
    int vidMem = vidMemBase + (y*80 + x)*2;
    MAGIC.wMem8(vidMem, (byte) c);
    MAGIC.wMem8(vidMem + 1, (byte)0x07);
  }
  public static void clearScreen(){
    int vidMem = vidMemBase;
    int i;
    for (i = 0; i<80*25; i++){
      MAGIC.wMem8(vidMem++, (byte)0);
      MAGIC.wMem8(vidMem++, (byte)0x35);

    }
  }
  public static void print(String str) {
    int i;
    for (i=0; i<str.length(); i++) print(str.charAt(i));
  }
  public static void print(char c) {
    MAGIC.wMem8(vidMemCursor++, (byte)c);
    MAGIC.wMem8(vidMemCursor++, (byte)0x07);
  }
  /*public void println(char c) { print(c); println(); }
  public void println(int i) { print(i); println(); }
  public void println(long l) { print(l); println(); }
  public void println(String str) { print(str); println(); }*/

}
