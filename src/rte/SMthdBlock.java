package rte;

public class SMthdBlock { //Klasse mit zusätzlichen Instanz-Variablen
    public final static int M_STAT = 0x00000020; //"static" modifier, taken from compiler
    public String namePar; //einfacher Name, vollqualifizierte Parametertypen
    public SMthdBlock nextMthd; //nächste Methode der aktuellen Klasse
    public int modifier; //Modifier der Methode
    public int[] lineInCodeOffset; //optionale Zeilen-Zuordnung zu Code-Bytes**
    public SClassDesc owner;
}