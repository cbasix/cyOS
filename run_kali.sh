set -e
set -o pipefail

# qemu-img create -f qcow2 kali.qcow2 5G
qemu-system-i386 -m 2048 -hda ~/Documents/studium/os-im-eigenbau/kali/kali.qcow2 -cdrom ~/Documents/studium/os-im-eigenbau/kali/kali.iso -netdev socket,id=cynet,connect=:1409 -device virtio-net-pci,netdev=cynet -netdev user,id=n1 -device virtio-net-pci,netdev=n1
