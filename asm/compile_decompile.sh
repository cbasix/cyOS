yasm -p nasm test.asm -a x86 -o out
python3 distorm.py out
