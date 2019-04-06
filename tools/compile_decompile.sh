#!/usr/bin/env bash
yasm -p nasm ../asm/test.asm -a x86 -o ../asm/out
python3 distorm.py ../asm/out
