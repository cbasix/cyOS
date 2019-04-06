package io;

public class Screen {
    public static final int GRAPH_MEM_BASE = 0xA0000;
    public static final int WIDTH = 320;
    public static final int HEIGHT = 200;

    private GraphMem graphMem =(GraphMem) MAGIC.cast2Struct(GRAPH_MEM_BASE);


    public static class GraphMem extends STRUCT {
        @SJC(offset=0,count=WIDTH*HEIGHT)
        public byte[] pixels;
    }

    public byte convert(byte red, byte green, byte blue){
       // todo implement
        return 0;
    }

    public void erase(){
        for (int i = 0; i < WIDTH*HEIGHT; i++){
            graphMem.pixels[i] = (byte) i;
        }
    }

    /*public void drawLine(int start_x, int start_y, int end_x, int end_y){

    }*/
    public void showWelcomePicture(){
        for (int i = 0; i < binimp.ByteData.squirrel.length; i++){
            //byte red = binimp.ByteData.squirrel[i];
            //byte green = binimp.ByteData.squirrel[i+1];
            //byte blue = binimp.ByteData.squirrel[i+2];
            graphMem.pixels[i] = binimp.ByteData.squirrel[i]; //convert(red, green, blue);
        }
    }


}
