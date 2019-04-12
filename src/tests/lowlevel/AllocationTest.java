package tests.lowlevel;

import rte.DynamicRuntime;
import tests.TestObject;

public class AllocationTest {

    public static class TestObjectStruct extends STRUCT {
        public int _r_referenceToStr, _r_next, _r_type, _r_relocEntrys, _r_scalarSize, _r_scalarData;
    }

    public static int test() {
        DynamicRuntime.ImageInfo image = (DynamicRuntime.ImageInfo) MAGIC.cast2Struct(MAGIC.imageBase);
        int startAddr = DynamicRuntime.getNextFreeAddr();
        int shouldObjSize = 6*MAGIC.ptrSize;

        // initalize empty memory with visual "mines" for the next 1024 bytes
        // to verify correct 0 initialisation and bounds
        for (int i = 0; i < 1024; i++) {
            MAGIC.wMem8(startAddr + i, (byte) 0xFB);
        }


        TestObject o1 = new TestObject();
        TestObject o2 = new TestObject();

        // check memory layout directly via structs
        TestObjectStruct tos1 = (TestObjectStruct) MAGIC.cast2Struct(startAddr);
        TestObjectStruct tos2 = (TestObjectStruct) MAGIC.cast2Struct(startAddr+shouldObjSize);
        TestObjectStruct tos3 = (TestObjectStruct) MAGIC.cast2Struct(startAddr+shouldObjSize*2);

        // type must point to class descriptor which must be within image
        if (!(image.start < tos1._r_type && tos1._r_type < (image.start + image.size))){ return 5;}
        // object must have 3 reloc entrys _r_type, _r_next and a pointer to a constant string defined in TestObj Class
        if (tos1._r_relocEntrys != 3){ return 15;}
        // object must have a scalar size of 3 integers ( a 4 byte)
        if (tos1._r_scalarSize != 3*4){ return 20;}
        // pointer to static string must point to an address within image
        if (!(image.start < tos1._r_referenceToStr && tos1._r_referenceToStr < image.start + image.size)){ return 25;}
        // data of scalar (int) must be initialized with zero
        if ( tos1._r_scalarData != 0){ return 30;}
        // rnext must point to the relocEntrys field of the next obj
        /*LowlevelOutputTest.printChar("DEBUG ", 25, 13, Color.RED);
        LowlevelOutputTest.printInt(MAGIC.addr(tos2._r_scalarSize), 10, 10, 25, 14, Color.RED);
        LowlevelOutputTest.printInt(tos1._r_next, 10, 10, 25, 15, Color.RED);
        LowlevelLogging.printHexdump(startAddr);
        while (tos1._r_next != MAGIC.addr(tos2._r_relocEntrys)){}*/
        if (tos1._r_next != MAGIC.addr(tos2._r_relocEntrys)){ return 35;}

        // second object
        //the error may be that the offset between the two objects is wrong
        if (tos2._r_relocEntrys != 3){ return 40;}
        // in obj2 the next pointer has to be zero, since its the last created object
        if (tos2._r_next != 0){ return 40;}
        // test setting the scalar data
        o1.setData(5);
        if (tos1._r_scalarData != 5){ return 45;}

        // check if the area of the to come object 3 is still untouched (check fist and last)
        /*LowlevelOutputTest.printChar("DEBUG ", 25, 13, Color.RED);
        LowlevelOutputTest.printHex(tos3._r_referenceToStr, 10, 25, 14, Color.RED);
        LowlevelOutputTest.printHex(tos3._r_scalarData,10, 25, 15, Color.RED);
        LowlevelLogging.printHexdump(startAddr);
        while (tos3._r_referenceToStr != 0xFBFBFBFB || tos3._r_scalarData != 0xFBFBFBFB){}*/
        if (tos3._r_referenceToStr != 0xFBFBFBFB || tos3._r_scalarData != 0xFBFBFBFB){ return 50;}
        // create third object
        TestObject o3 = new TestObject();
        // check next field of object 2 again it must now be set to object 3s relocEntry addr
        if (tos2._r_next != MAGIC.addr(tos3._r_relocEntrys)){ return 55;}
        // next of object 3 must be zero, since it is now the latest object
        if (tos3._r_next != 0){ return 60;}

        return 0;
    }

}
