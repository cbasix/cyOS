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
    public void showSquirrelPicture(){
        for (int i = 0; i < binimp.ByteData.squirrel.length; i++){
            graphMem.pixels[i] = binimp.ByteData.squirrel[i]; //convert(red, green, blue);
        }
    }

    public void showGreyscaleSquirrelPicture(){
        for (int i = 0; i < binimp.ByteData.squirrel_grey.length; i++){
            graphMem.pixels[i] = binimp.ByteData.squirrel_grey[i]; //convert(red, green, blue);
        }
    }



    public void showColorPicture(){
        for (int i = 0; i < WIDTH*HEIGHT; i++){
            if (i % WIDTH <= 255) {
                graphMem.pixels[i] = (byte) (i % WIDTH);
            }
        }
    }

    // doesnt work
    public void setRgbPalette(){
        int step = 255 / 6; // = 255^(1/3)
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < 6*6*6; i++){
             red += step;
             if (red >= 6*step){
                 red = 0;
                 green += step;

                 if (green >= 6*step){
                     green = 0;
                     blue += step;
                 }
             }
             setColor(i, red, green, blue);
        }

    }

    public static final int PALETTE_SET_PORT = 0x0408; //0x3C8;

    // doesnt work
    public void setColor(int color, int red, int green, int blue)    {
        MAGIC.wMem8(PALETTE_SET_PORT, (byte) color);
        MAGIC.wMem8(PALETTE_SET_PORT, (byte)(red >> 2));
        MAGIC.wMem8(PALETTE_SET_PORT, (byte)(green >> 2));
        MAGIC.wMem8(PALETTE_SET_PORT, (byte)(blue >> 2));
    }

    public void switchToGraphics(){
        rte.BIOS.regs.EAX=0x0013;
        rte.BIOS.rint(0x10);
    }

    public void switchToTextMode(){
        rte.BIOS.regs.EAX=0x0003;
        rte.BIOS.rint(0x10);
    }


}
