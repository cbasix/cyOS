set -e
set -o pipefail

# qemu-img create -f qcow2 kali.qcow2 5G
qemu-system-i386 -m 2048 -hda ~/Documents/studium/os-im-eigenbau/kali/kali.qcow2 -netdev user,id=ext -device e1000,netdev=ext -netdev socket,id=cynet,listen=:1409 -device virtio-net-pci,netdev=cynet,id=cynet
