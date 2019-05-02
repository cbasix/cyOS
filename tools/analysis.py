from struct import *
import networkx as nx
import numpy as np
import matplotlib.pyplot as plt
import sys

if __name__ == '__main__':
    #G = nx.DiGraph(directed=True)

    with open(sys.argv[1], "rb") as f:

        t = f.read(4)
        print(t)

        t = f.read(12)
        print(t)
        mode, frm, to = unpack('iii', t)
        edge_colors = []
        lvl = 0
        visited = []

        while (mode in [ 0x0000DE3C, 0x0000000B]):


            #G.add_edges_from(
            #    [(hex(frm), hex(to))])
            #edge_colors.append("black" if mode == 0x0000000B else "red")

            #print(hex(mode), hex(frm), hex(to))
            if mode == 0x0000000B:
                lvl -= 1
                print(" "*(lvl), "UP   ",hex(to), "<-", hex(frm))

            elif mode == 0x0000DE3C:

                print(" "*(lvl), "DESC ",hex(frm), "->", hex(to))
                lvl += 1

            else:
                print("unknown")

            visited.append(to),
            visited.append(frm)

            t = f.read(12)
            mode, frm, to = unpack('iii', t)



    pi
    #nx.draw(G, cmap = plt.get_cmap('jet'), edge_colors=edge_colors)
    #plt.show()

