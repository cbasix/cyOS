package rte;

import io.Color;
import io.GreenScreenDirect;
import kernel.Kernel;

public class DynamicRuntime {

  public static final int POINTER_SIZE = MAGIC.ptrSize;
  private static int firstObjAddr;
  private static int lastObjAddr;
  private static int nextFreeAddr;
  // get some distance between the image and our new object (allows do use the hex viewer do debug offset errors, instead of just crashing)s
  public static final int SAFETY_DISTANCE = 0;

  public static class ImageInfo extends STRUCT {
    public int start, size; //, classDescStart, codebyteAddr, firstObjInImageAddr, ramInitAddr;
  }

  public static int getNextFreeAddr() {
    return nextFreeAddr;
  }

  public static void initializeMemoryPointers(){
    ImageInfo image = (ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
    nextFreeAddr = image.start + image.size + SAFETY_DISTANCE;

    if(image.size != MAGIC.rMem32(MAGIC.imageBase+4) || image.start != MAGIC.imageBase){
      Kernel.debug("Something is wrong with your image info struct", Kernel.ERROR);
      while (true){}
    }
    // allign to 4 byte, (last two address bits zero)
    //nextFreeAddr = (nextFreeAddr + 0x3) &~ 0x3;

    // allign to next sector boundary
    nextFreeAddr = (nextFreeAddr + 0xFFF) &~ 0xFFF;

    // plant visual mines in memory for the next 1024 bytes to verify correct 0 initialisation
    Kernel.debug("before mines", Kernel.ERROR);
    for(int i = 0; i < 1024; i++){
      MAGIC.wMem8(nextFreeAddr+i, (byte)0xFF);
    }
    Kernel.debug("after mines", Kernel.ERROR);
  }
  
  public static Object newInstance(int scalarSize, int relocEntries, SClassDesc type) {
    //MAGIC.inline(0xCC); //TODO remove this line


    // allign scalar size to 4 byte
    scalarSize = (scalarSize + 0x3) &~ 0x3;

    int line = 15;
    GreenScreenDirect.printStr("Last Obj", 0, line, Color.GREEN);
    GreenScreenDirect.printStr("scalarSize", 0, ++line, Color.GREEN);
    GreenScreenDirect.printInt(scalarSize, 10,10,15, line, Color.GREEN);

    GreenScreenDirect.printStr("relocEntries", 0, ++line, Color.GREEN);
    GreenScreenDirect.printInt(relocEntries, 10,10,15, line, Color.GREEN);

    // calculate memory requirements
    int objSize = scalarSize + relocEntries * POINTER_SIZE;

    GreenScreenDirect.printStr("objSize", 0, ++line, Color.GREEN);
    GreenScreenDirect.printInt(objSize, 10,10,15, line, Color.GREEN);

    // TODO do it in 32 bit steps
    // clear allocated memory
    for(int i = 0; i < objSize; i++){
      MAGIC.wMem8(nextFreeAddr+i, (byte)0x00);
    }

    // TODO check!!!!!
    // calculate object address inside allocated memory
    int objAddr = nextFreeAddr + relocEntries * POINTER_SIZE;


    GreenScreenDirect.printStr("objAddr", 0, ++line, Color.GREEN);
    GreenScreenDirect.printInt(objAddr, 10,10,15, line, Color.GREEN);

    GreenScreenDirect.printStr("nextFreeAddr", 0, ++line, Color.GREEN);
    GreenScreenDirect.printInt(nextFreeAddr, 10,10,15, line, Color.GREEN);

    Object newObject = MAGIC.cast2Obj(objAddr);

    // fill kernel fields of object
    MAGIC.assign(newObject._r_type, type);
    MAGIC.assign(newObject._r_relocEntries, relocEntries);
    MAGIC.assign(newObject._r_scalarSize, scalarSize);


    if(lastObjAddr == 0) {
      // first object ever, save it for having a startpoint for _r_next iteration GC (later on)
      firstObjAddr = objAddr;

    } else{
      // set r_next on last object
      Object lastObject = MAGIC.cast2Obj(lastObjAddr);
      MAGIC.assign(lastObject._r_next, newObject);

      // print status
      GreenScreenDirect.printStr("Setting last r_next", 2, 23, Color.CYAN);
      GreenScreenDirect.printInt(MAGIC.addr(lastObject._r_next), 10, 10, 2, 24, Color.CYAN);
      GreenScreenDirect.printStr(" to ", 12, 24, Color.CYAN);
      GreenScreenDirect.printInt(MAGIC.rMem32(MAGIC.addr(lastObject._r_next)), 10, 10, 16, 24, Color.CYAN);
      Kernel.wait(1);
    }

    // allocate requested memory
    nextFreeAddr = nextFreeAddr + objSize;

    lastObjAddr = objAddr;

    return newObject;
  }

  
  public static SArray newArray(int length, int arrDim, int entrySize, int stdType,
      SClassDesc unitType) { //unitType is not for sure of type SClassDesc
    int scS, rlE;
    SArray me;
    
    if (stdType==0 && unitType._r_type!=MAGIC.clssDesc("SClassDesc"))
      MAGIC.inline(0xCC); //check type of unitType, we don't support interface arrays
    scS=MAGIC.getInstScalarSize("SArray");
    rlE=MAGIC.getInstRelocEntries("SArray");
    if (arrDim>1 || entrySize<0) rlE+=length;
    else scS+=length*entrySize;
    me=(SArray)newInstance(scS, rlE, MAGIC.clssDesc("SArray")); // I ADDED THE CHAST HERE !!!!!!!
    MAGIC.assign(me.length, length);
    MAGIC.assign(me._r_dim, arrDim);
    MAGIC.assign(me._r_stdType, stdType);
    MAGIC.assign(me._r_unitType, unitType);
    return me;
  }
  
  public static void newMultArray(SArray[] parent, int curLevel, int destLevel,
      int length, int arrDim, int entrySize, int stdType, SClassDesc clssType) {
    int i;
    
    if (curLevel+1<destLevel) { //step down one level
      curLevel++;
      for (i=0; i<parent.length; i++) {
        newMultArray((SArray[])((Object)parent[i]), curLevel, destLevel,
            length, arrDim, entrySize, stdType, clssType);
      }
    }
    else { //create the new entries
      destLevel=arrDim-curLevel;
      for (i=0; i<parent.length; i++) {
        parent[i]=newArray(length, destLevel, entrySize, stdType, clssType);
      }
    }
  }
  
  public static boolean isInstance(Object o, SClassDesc dest, boolean asCast) {
    SClassDesc check;
    
    if (o==null) {
      if (asCast) return true; //null matches all
      return false; //null is not an instance
    }
    check=o._r_type;
    while (check!=null) {
      if (check==dest) return true;
      check=check.parent;
    }
    if (asCast) MAGIC.inline(0xCC);
    return false;
  }
  
  public static SIntfMap isImplementation(Object o, SIntfDesc dest, boolean asCast) {
    SIntfMap check;
    
    if (o==null) return null;
    check=o._r_type.implementations;
    while (check!=null) {
      if (check.owner==dest) return check;
      check=check.next;
    }
    if (asCast) MAGIC.inline(0xCC);
    return null;
  }
  
  public static boolean isArray(SArray o, int stdType, SClassDesc clssType, int arrDim, boolean asCast) {
    SClassDesc clss;
    
    //in fact o is of type "Object", _r_type has to be checked below - but this check is faster than "instanceof" and conversion
    if (o==null) {
      if (asCast) return true; //null matches all
      return false; //null is not an instance
    }
    if (o._r_type!=MAGIC.clssDesc("SArray")) { //will never match independently of arrDim
      if (asCast) MAGIC.inline(0xCC);
      return false;
    }
    if (clssType==MAGIC.clssDesc("SArray")) { //special test for arrays
      if (o._r_unitType==MAGIC.clssDesc("SArray")) arrDim--; //an array of SArrays, make next test to ">=" instead of ">"
      if (o._r_dim>arrDim) return true; //at least one level has to be left to have an object of type SArray
      if (asCast) MAGIC.inline(0xCC);
      return false;
    }
    //no specials, check arrDim and check for standard type
    if (o._r_stdType!=stdType || o._r_dim<arrDim) { //check standard types and array dimension
      if (asCast) MAGIC.inline(0xCC);
      return false;
    }
    if (stdType!=0) {
      if (o._r_dim==arrDim) return true; //array of standard-type matching
      if (asCast) MAGIC.inline(0xCC);
      return false;
    }
    //array of objects, make deep-check for class type (PicOS does not support interface arrays)
    if (o._r_unitType._r_type!=MAGIC.clssDesc("SClassDesc")) MAGIC.inline(0xCC);
    clss=o._r_unitType;
    while (clss!=null) {
      if (clss==clssType) return true;
      clss=clss.parent;
    }
    if (asCast) MAGIC.inline(0xCC);
    return false;
  }
  
  public static void checkArrayStore(SArray dest, SArray newEntry) {
    if (dest._r_dim>1) isArray(newEntry, dest._r_stdType, dest._r_unitType, dest._r_dim-1, true);
    else if (dest._r_unitType==null) MAGIC.inline(0xCC);
    else isInstance(newEntry, dest._r_unitType, true);
  }
}
