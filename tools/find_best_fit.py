import numpy as np
from PIL import Image
im = Image.open("default_palette.png")
rgb_im = im.convert('RGB')
import pickle

def dist(color1, color2):
    COLOR_FACTOR = 4
    return abs(color1[0]-color2[0]) + abs(color1[0]-color2[1]) + 2*abs(color1[0]-color2[2]) + \
           COLOR_FACTOR*(abs((color1[0]-color1[1])-(color2[0]-color2[1])) + abs((color1[1]-color1[2])-(color2[1]-color2[2])))

SHIFT = 2

if __name__ == '__main__':

    ref = {}
    cnt = 0
    last = (-1, -1 , -1)
    for i in range(rgb_im.size[0]):
        pxl = rgb_im.getpixel((i, 0))
        ref[cnt] = pxl
        cnt += 1
        #print(dist(pxl, last))

    print(len(ref))
    print(ref)
    with open('ref.pickle', 'wb') as f:
        # Pickle the 'data' dictionary using the highest protocol available.
        pickle.dump(ref, f, pickle.HIGHEST_PROTOCOL)

    best_match = np.zeros((64,64,64))
    min_dist = np.full((64, 64, 64), 256+256+256, dtype=int)


    for red in range(0, 256>>SHIFT):
        for green in range(0, 256>>SHIFT):
            for blue in range(0, 256>>SHIFT):
                for key, value in ref.items():
                     if dist(((red<<SHIFT, green<<SHIFT, blue<<SHIFT)), value) < min_dist[red][green][blue]:
                        min_dist[red][green][blue] = dist((red<<SHIFT, green<<SHIFT, blue<<SHIFT), value)
                        best_match[red][green][blue] = key
                #print("found best", red, green, blue, "dist", min_dist[red][green][blue], best_match[red][green][blue], ref[best_match[red][green][blue]])


    print(best_match)
    with open('best_match3.pickle', 'wb') as f:
        # Pickle the 'data' dictionary using the highest protocol available.
        pickle.dump(best_match, f, pickle.HIGHEST_PROTOCOL)





#print(file.bits, file.size, file.format)

#out = open("../blobs/squirrel.bib", "wb")
#out.write(jpgfile.convert("P", palette=Image.MAXCOVERAGE, colors=256).tobytes())