package tasks.shell.commands;

import datastructs.RingBuffer;
import kernel.Kernel;
import rte.SClassDesc;
import rte.SMthdBlock;
import rte.SPackage;
import tasks.Blocking;
import tasks.Editor;
import tasks.LogEvent;


public class TextualCall extends Command{
    @Override
    public String getCmd() {
        return "call";
    }

    @Override
    public void execute(RingBuffer shellMessageBuffer, String[] args) {
        //LowlevelLogging.debug(String.join(args, " "), LowlevelLogging.ERROR);
        if (args.length <= 3){
            shellMessageBuffer.push(new LogEvent("Usage: call <package> <unit> <mthd>"));
        } else {
            callByName(args[1], args[2], args[3], shellMessageBuffer);
        }
    }

    /*
        copied from compiler documentation and modified. can only call methods on first package level!
     */
    private static void callByName(String pack, String unit, String mthd, RingBuffer shellMessageBuffer) {
        SClassDesc u;
        int mthdAddr;
        SPackage p=SPackage.root.subPacks;
        SMthdBlock m;
        while (p!=null) { //alle Packages durchsuchen
            if (pack.equals(p.name)) { //Package gefunden
                u=p.units; //erste Unit des Packages auswählen
                while (u!=null) { //alle Units durchsuchen
                    if (unit.equals(u.name)) { //Klasse gefunden
                        m=u.mthds; //erste Methode der Unit auswählen
                        while (m!=null) { //alle Methoden durchsuchen
                            if (mthd.equals(m.namePar)) { //Method gefunden
                                if ((m.modifier & SMthdBlock.M_STAT)==0) {
                                    shellMessageBuffer.push(new LogEvent("Error: method not static"));
                                    return; //Fehler: Methode ist nicht statisch
                                }

                                mthdAddr=MAGIC.cast2Ref(m)+MAGIC.getCodeOff();
                                MAGIC.inline(0x57); //push edi
                                MAGIC.inline(0x8B, 0x7D, 0xFC); //mov edi,[ebp-4]
                                MAGIC.inline(0xFF, 0x55, 0xF8); //call dword [ebp-8]
                                MAGIC.inline(0x5F); //pop edi
                                shellMessageBuffer.push(new LogEvent("Call done"));
                                return; //call war erfolgreich
                            }
                            m=m.nextMthd; //nächste Methode in aktueller Unit versuchen
                        }
                        shellMessageBuffer.push(new LogEvent("Error: method not found"));
                        return; //Fehler: Methode nicht gefunden
                    }
                    u=u.nextUnit; //nächste Unit im aktuellen Package versuchen
                }
                shellMessageBuffer.push(new LogEvent("Error: Class not found"));
                return; //Fehler: Klasse nicht gefunden
            }
            p=p.nextPack; //nächstes Package versuchen
        }
        shellMessageBuffer.push(new LogEvent("Error: Package not found"));
        //Fehler: Package nicht gefunden
    }
}
