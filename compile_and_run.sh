#!/bin/bash
set -e
set -o pipefail

cd compiler/nightly/

# compiler options
compiler="./compile "
path="../../src/ ../../blobs/ "

default="-s 32M -Q -g -I 6 -t ia32 -T nsop "

floppy="-o boot "
iso="-o raw "

debug_writer="-D code addr.txt -D sym syminfo.txt "

infos_rte="-u rte "
infos_raw="-u raw "

# mkisofs
mkisofs="mkisofs -o cyos.iso -N -b bbk_iso.bin -no-emul-boot -boot-load-seg 0x7C0 -boot-load-size 4 -V \"cyOS\" -A \"SJC compiled bootable OS\" -graft-points CDBOOT/BOOT_ISO.IMG=raw_out.bin bbk_iso.bin"

# qemu options
qemu="qemu-system-i386 "
qemu_default="-no-kvm -monitor stdio -d guest_errors -m 32 "
qemu_debug="-s -S -no-reboot "

boot_floppy="-drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144"
boot_iso="-cdrom cyos.iso "

net1="-netdev socket,id=cynet,listen=:1408 -device virtio-net-pci,netdev=cynet,mac=52:54:00:12:34:60 "
net2="-netdev socket,id=cynet,connect=:1408 -device virtio-net-pci,netdev=cynet,mac=52:54:00:12:34:61 "

net_kali="-netdev socket,id=cynet,connect=:1409 -device virtio-net-pci,netdev=cynet,mac=52:54:00:12:34:62 "
dump_net="-object filter-dump,id=id,netdev=cynet,file=cynet.dmp"

# variants
# run one instance which can connect to kali
if [ $# -eq 0 ]; then
     cmd="$compiler $path $default $iso $debug_writer $infos_rte"
    echo $cmd
    eval $cmd

    echo $mkisofs
    eval $mkisofs

    cmd="$qemu $qemu_default $boot_iso $net_kali &"
    echo $cmd
    eval $cmd
    exit 0;
fi

# run two connected instances
if [[ $1 -eq "net" ]]; then
    cmd="$compiler $path $default $iso $debug_writer $infos_rte"
    echo $cmd
    eval $cmd

    echo $mkisofs
    eval $mkisofs

    cmd="$qemu $qemu_default $boot_iso $net1 &"
    echo $cmd
    eval $cmd

    cmd="$qemu $qemu_default $boot_iso $net2"
    echo $cmd
    eval $cmd

    exit 0;
fi

# test bootable usb stick
# qemu-system-i386 -hda /dev/sdb

#-netdev tap,id=mynet0 # needs root

# get method of addr java -cp compiler/nightly/sjc.jar sjc.ui.GetMthd compiler/nightly/addr.txt ADDR
