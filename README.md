# cyOS
A small monolitic educational Java OS which was created as an exercise for the lecture "Betriebssysteme im Eigenbau" (Operating Systems self-made)

![Screenshot](blobs/cyOS.png)

# Implemented Parts
- Virtual Memory (1:1 Mapped)
- Flexible Interrupt Handling
- Simple Keyboard Driver
- VGA-Mode embedded Picture viewer
- Debug Screen for Exceptions
- Simplistic Task Scheduler
- Cooperative Multitasking
- Kill blocking Tasks via keystroke
- Simple Shell (with buggy scrolling ;)
- Some test commands 
- Get memory map from BIOS
- Garbage collection (manually triggable)
- Simple Garbage collection statistics
- Virtio net driver
- Ethernet / ARP / IPv4

And some more small stuff. 

# Planned
- UDP
- DHCP Client & Server
- DNS Client & Server

# An OS in Java?
It is compiled using the SJC Java to native compiler, which can be found here: http://www.fam-frenz.de/stefan/compiler.html
