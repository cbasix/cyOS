# cyOS
A small monolitic educational Java OS which was created as an exercise for the lecture "Betriebssysteme im Eigenbau" (Operating Systems self-made)

![Screenshot](blobs/cyOS.png)

# Implemented Parts
- Virtual Memory (1:1 Mapped)
- Flexible Interrupt Handling
- Keyboard Driver
- VGA-Mode embedded Picture viewer
- Debug Screen for Exceptions
- Task Scheduler
- Cooperative Multitasking
- Kill blocking Tasks via keystroke
- Simple Shell (with buggy scrolling ;)
- Some test commands 
- Get memory map from BIOS
- Garbage collection (manually triggerable)
- Garbage collection statistics
- Virtio net 1.0 driver
- Ethernet / ARP / IPv4
- UDP (without checksum)
- DHCP Client & Server

And some more stuff. 

# In work
- DNS Client & Server (only ARecords)

# An OS in Java?
cyOS is compiled using the SJC Java to native compiler, which can be found here: http://www.fam-frenz.de/stefan/compiler.html
