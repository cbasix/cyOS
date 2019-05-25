set -e
set -o pipefail

cd compiler/nightly/
#./compile ../../src/ ../../blobs/ -o boot -s 2M -D code addr.txt -Q -I 5
#./compile ../../src/ ../../blobs/ -o boot -s 2M -Q -I 6 -t ia32 -T nsop -D code addr.txt -D sym syminfo.txt -u raw
#rte !
# ./compile ../../src/ ../../blobs/ -o boot -s 2M -Q -I 5 -t ia32 -T nsop -n -b
#qemu-system-i386 -no-kvm -monitor stdio -d guest_errors -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
#qemu-system-i386 -monitor stdio -no-reboot -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
#qemu-system-i386 -monitor stdio -s -S -no-reboot -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
#qemu-system-i386 -monitor stdio -trace-unassigned -no-kvm -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144
# get method of addr java -cp compiler/nightly/sjc.jar sjc.ui.GetMthd compiler/nightly/addr.txt ADDR

# boot via floppy image
#./compile ../../src/ ../../blobs/ -o boot -s 2M -Q -I 6 -t ia32 -T nsop -D code addr.txt -D sym syminfo.txt -u raw  # gc works, with rte -> nullptr
#qemu-system-i386 -no-kvm -monitor stdio -d guest_errors -m 32 -boot a -drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144

# boot via iso image
./compile ../../src/ ../../blobs/ -o raw -s 32M -Q -I 6 -t ia32 -T nsop -D code addr.txt -D sym syminfo.txt -u rte  # gc null ptr ;) with rte ->nullptr
mkisofs -o cyos.iso -N -b bbk_iso.bin -no-emul-boot -boot-load-seg 0x7C0 -boot-load-size 4 -V "cyOS" -A "SJC compiled bootable OS" -graft-points CDBOOT/BOOT_ISO.IMG=raw_out.bin bbk_iso.bin

#qemu-system-i386 -cdrom cyos.iso -no-kvm -monitor stdio -d guest_errors -netdev socket,id=cynet,listen=:1409 -device virtio-net-pci,netdev=cynet
qemu-system-i386 -m 32 -cdrom cyos.iso -no-kvm -monitor stdio -d guest_errors -netdev socket,id=cynet,connect=:1409 -device virtio-net-pci,netdev=cynet -object filter-dump,id=id,netdev=cynet,file=cynet.dmp
#qemu-system-i386 -m 32 -cdrom cyos.iso -no-kvm -monitor stdio -d guest_errors

#-netdev tap,id=mynet0 # needs root
#-netdev socket,id=mynet0,listen=:1234
#-netdev socket,id=mynet0,connect=:1234
#-netdev user,id=n1 -device virtio-net-pci,netdev=n1

# test bootable usb stick
# qemu-system-i386 -hda /dev/sdb