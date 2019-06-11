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
qemu_default="-no-kvm -d guest_errors -m 64 -no-reboot "
qemu_debug="-s -S "
qemu_monitor="-monitor stdio "

boot_floppy="-drive format=raw,if=none,file=BOOT_FLP.IMG -device floppy,drive=none0,drive-type=144"
boot_iso="-cdrom cyos.iso "

mac1=",mac=52:54:00:12:34:61 "
mac2=",mac=52:54:00:12:34:62 "
mac3=",mac=52:54:00:12:34:63 "
macKali1=",mac=52:54:00:12:34:63 "
macKali2=",mac=52:54:00:12:34:64 "

net1="-netdev socket,id=cynet,listen=:1408 -device virtio-net-pci,netdev=cynet$mac1"
net2="-netdev socket,id=cynet,connect=:1408 -device virtio-net-pci,netdev=cynet$mac2"

net_user="-netdev user,id=ext -device virtio-net-pci,netdev=ext$mac3"
#net_kali="-netdev socket,id=cynet,connect=:1409 -device virtio-net-pci,netdev=cynet$mac2"

net_tap1="-netdev tap,ifname=cynet-tap1,script=no,downscript=no,id=cynet -device virtio-net-pci,netdev=cynet,id=cynet$mac1"
net_tap2="-netdev tap,ifname=cynet-tap2,script=no,downscript=no,id=cynet -device virtio-net-pci,netdev=cynet,id=cynet$mac2"

dump_ext="-object filter-dump,id=dump_ext,netdev=ext,file=ext.pcap"
dump_cynet="-object filter-dump,id=dump_cynet,netdev=cynet,file=cynet.pcap"

# variants
# run two via two seperate user networks connected instances
if [ $# -eq 0 ]; then
 cmd="$compiler $path $default $iso $debug_writer $infos_rte"
    echo $cmd
    eval $cmd

    echo $mkisofs
    eval $mkisofs

    cmd="$qemu $qemu_default $boot_iso $net1 $net_user $dump_cynet $dump_ext  &"
    echo $cmd
    eval $cmd

    sleep 1

    cmd="$qemu $qemu_default $boot_iso $net2"
    echo $cmd
    eval $cmd

    exit 0;
fi

# one in user net
if [[ $1 == "user" ]]; then
    cmd="$compiler $path $default $iso $debug_writer $infos_rte"
    echo $cmd
    eval $cmd

    echo $mkisofs
    eval $mkisofs

    cmd="$qemu $qemu_default $boot_iso $net_user $qemu_monitor $dump_ext $qemu_monitor"
    echo $cmd
    eval $cmd

    exit 0;
fi

# run two via tap connected instances
if [[ $1 == "two" ]]; then
    cmd="$compiler $path $default $iso $debug_writer $infos_rte"
    echo $cmd
    eval $cmd

    echo $mkisofs
    eval $mkisofs

    cmd="$qemu $qemu_default $boot_iso $net_tap1 &"
    echo $cmd
    eval $cmd

    sleep 1

    cmd="$qemu $qemu_default $boot_iso $net_tap2"
    echo $cmd
    eval $cmd

    exit 0;
fi

# run one instance which can connect to kali
if [[ $1 == "single" ]]; then
      cmd="$compiler $path $default $iso $debug_writer $infos_rte"
    echo $cmd
    eval $cmd

    echo $mkisofs
    eval $mkisofs

    cmd="$qemu $qemu_default $boot_iso $net_tap1 $qemu_monitor"
    echo $cmd
    eval $cmd
    exit 0;
fi

# test bootable usb stick
# qemu-system-i386 -hda /dev/sdb

#-netdev tap,id=mynet0 # needs root

# get method of addr java -cp compiler/nightly/sjc.jar sjc.ui.GetMthd compiler/nightly/addr.txt ADDR

# call this with sudo !
if [[ $1 == "setuptap" ]]; then
    ip link add cynet-br0 type bridge
    ip tuntap add dev cynet-tap0 mode tap user $(whoami)
    ip tuntap add dev cynet-tap1 mode tap user $(whoami)
    ip tuntap add dev cynet-tap2 mode tap user $(whoami)
    ip tuntap add dev cynet-tap3 mode tap user $(whoami)

    ip link set cynet-tap0 master cynet-br0
    ip link set cynet-tap1 master cynet-br0
    ip link set cynet-tap2 master cynet-br0
    ip link set cynet-tap3 master cynet-br0

    ip link set dev cynet-br0 up
    ip link set dev cynet-tap0 up
    ip link set dev cynet-tap1 up
    ip link set dev cynet-tap2 up
    ip link set dev cynet-tap3 up

    ufw allow in on cynet-br0
    ufw allow out on cynet-br0
fi

if [[ $1 == "runkali" ]]; then
    # qemu-img create -f qcow2 kali.qcow2 5G
    qemu="qemu-system-i386 -m 2048 -hda /home/cyberxix/Documents/studium/os-im-eigenbau/kali/kali.qcow2 "

    ext="-netdev user,id=ext -device e1000,netdev=ext "

    cynet="-netdev socket,id=cynet_direct,listen=:1409 -device virtio-net-pci,netdev=cynet_direct,id=cynet_direct "

    cynet_tap="-netdev tap,ifname=cynet-tap0,script=no,downscript=no,id=cynet -device virtio-net-pci,netdev=cynet,id=cynet$macKali1 "
    cynet_router="-netdev tap,ifname=cynet-tap3,script=no,downscript=no,id=cynet_router -device virtio-net-pci,netdev=cynet_router,id=cynet_router$macKali2 "

    cmd="$qemu $ext $cynet $cynet_tap $cynet_router"
    echo $cmd
    eval $cmd
fi