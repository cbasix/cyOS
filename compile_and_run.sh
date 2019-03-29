cd compiler/exec0190/
./compile ../../src/ ../../blobs/ -o boot -s 2M
qemu-system-i386 -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144