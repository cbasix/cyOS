cd compiler/exec0190/
#./compile ../../src/ ../../blobs/ -o boot -s 2M -D code addr.txt -Q -I 5
./compile ../../src/ ../../blobs/ -o boot -s 2M -Q -I 5
qemu-system-i386 -no-kvm -monitor stdio -d guest_errors -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
#qemu-system-i386 -monitor stdio -no-reboot -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
#qemu-system-i386 -monitor stdio -s -S -no-reboot -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
#qemu-system-i386 -monitor stdio -trace-unassigned -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144