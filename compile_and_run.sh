cd compiler/exec0190/
./compile /home/cyberxix/Git/cyOS/src -o boot
qemu-system-x86_64 -no-kvm -m 32 -boot a -drive format=raw,file=BOOT_FLP.IMG,if=floppy