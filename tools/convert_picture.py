from PIL import Image
jpgfile = Image.open("squirrel_small.jpg")

print(jpgfile.bits, jpgfile.size, jpgfile.format)

out = open("../blobs/squirrel.bib", "wb")
out.write(jpgfile.convert("P", palette=Image.MAXCOVERAGE, colors=256).tobytes())
