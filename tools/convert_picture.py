from PIL import Image

from tools.find_best_fit import SHIFT

jpgfile = Image.open("squirrel_small.jpg")
import pickle

print(jpgfile.bits, jpgfile.size, jpgfile.format)

with open('best_match3.pickle', 'rb') as f:
    # Pickle the 'data' dictionary using the highest protocol available.
    best_match = pickle.load(f)

with open('ref.pickle', 'rb') as f:
    # Pickle the 'data' dictionary using the highest protocol available.
    ref = pickle.load(f)

out = open("../blobs/squirrel.bib", "wb")
#out.write(jpgfile.convert("P", palette=Image.MAXCOVERAGE, colors=256).tobytes())


img = Image.new('RGB', (320, 200), color = 'red')
#img.save('out.png')
img = img.convert("P", palette=Image.MAXCOVERAGE, colors=256)
pixels = img.load()

rgb_im = jpgfile.convert('RGB')

for i in range(rgb_im.size[0]):
    for j in range(rgb_im.size[1]):
        pxl = rgb_im.getpixel((i, j))
        print(bytes([int(best_match[pxl[0]>>SHIFT][pxl[1]>>SHIFT][pxl[2]>>SHIFT])]))
        pixels[i, j] = int(best_match[pxl[0]>>SHIFT][pxl[1]>>SHIFT][pxl[2]>>SHIFT])
out.write(img.tobytes())

img.show()



