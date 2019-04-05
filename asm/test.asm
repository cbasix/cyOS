USE32

;push eax  ; sichere register
push ebx

;mov eax, 0xA0A1A2A3  ; schreibe den festen wert B in adresse A
mov ebx, 0xB0B1B2B3  ; B ist die InterruptNummer, A zeigt auf das "Last interrupt" Feld der Interrupt Jump Table
mov [0xA0A1A2A3], ebx
pop ebx ; restore register
;pop eax

;jmp [0xC0C1C2C3] ; sprige zu Adresse C einem der beiden globalen int handler
;jmp 0x8:0x10421042h
;jmp FAR DWORD [0xC0C1C2C3]
jmp 0x8:0xffff00cf


