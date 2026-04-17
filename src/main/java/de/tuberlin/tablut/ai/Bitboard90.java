package de.tuberlin.tablut.ai;


//mit vordefinierter Größe 9x9, d.h. 81 Bits + je Seperation am Ende der Zeile für 9x10 Bits d.h. 90 Bits
public class Bitboard90 {

    /////////////////////////////////////////////////////////////////////////////
    /// globale Variablen
    /////////////////////////////////////////////////////////////////////////////
    static final int rows = 9;
    static final int cols = 10;
    static final Bitboard90 fieldMask = createBoardMask();

    /////////////////////////////////////////////////////////////////////////////
    /// lokale Variablen
    /////////////////////////////////////////////////////////////////////////////

    long low; // erste 0-63 Bits
    long high; // Bits 64-89; 90 bis 127 ungenutzt


    /////////////////////////////////////////////////////////////////////////////
    /// Infrastrukturfunktionen
    /////////////////////////////////////////////////////////////////////////////
    public Bitboard90(long low, long high) {
        this.low = low;
        this.high = high;
    }
    public Bitboard90() {
        this.low = 0;
        this.high = 0;
    }

    static Bitboard90 createBoardMask() {
        Bitboard90 mask = new Bitboard90();
        for (int row = 0; row < rows; row++){
            for (int col = 0; col < cols-1; col++) {//hier wird die 10te Spalte übersprungen!
                int pos = row * cols + col;
                setBit(mask,pos);
            }
        }
        return mask;
    }



/*
Grundsätzliche Ideen im Code:
- (1L << x) erzeugt ein 0...010...0, wobei die 1 an x-ter Stelle steht, also nur das xte Bit 1 ist.
- & ist das bitweise AND, d.h. im Ergebnis bleibt nur eine 1, wenn beide operanden eine 1 am selben Bit haben.
- Da 2 long nötig sind, um das Bitboard zu repräsentieren, muss immer eine Fallunterscheidung abhängig von Pos gemacht werden, um zu prüfen, welche Bits angesprochen werden.
*/

    /////////////////////////////////////////////////////////////////////////////
    /// Bit Operationen
    /////////////////////////////////////////////////////////////////////////////

    //Setzt ein Bit an Stelle Pos (keine Prüfung, ob da schon eins ist)
    //grundsätzlich erlaubt die Methode auch Separation Bits auf 1 zu setzen
    static void setBit(Bitboard90 bb,int pos){
        if (pos < 64){
            bb.low |= (1L << pos);
        }
        else {
            bb.high |= (1L << (pos-64));
        }
    }

    //  Prüft, ob an Stelle pos ein Bit gesetzt ist
    static boolean getBit(Bitboard90 bb,int pos){
        if (pos < 64){
//          ist an Stelle pos eine 1, so bleibt ein Bit gesetzt und das Ergebnis ist !=0, alle anderen Stellen werden durch die Maske (1L<<Pos) ignoriert beim AND
            return (bb.low & (1L << pos)) != 0;
        }
        else{
            return (bb.high & (1L <<(pos-64))) !=0;
        }
    }

    //Entfernt Bit an Stelle pos; keine Prüfung, ob Bit überhaupt gesetzt ist
    static void removeBit(Bitboard90 bb, int pos){
        // mask ist die invertierte Maske, d.h. nur das Bit an Pos ist 0, der Rest 1, durch das AND wird die Ursprungsfolge beibehalten, aber Bit an pos auf jeden Fall auf 0 gesetzt
        if (pos <64){
            long mask = ~(1L << pos);
            bb.low = bb.low & mask;
        }
        else {
            long mask = ~(1L << (pos-64));
            bb.high = bb.high & mask;
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /// Logische Operationen
    /////////////////////////////////////////////////////////////////////////////

    static Bitboard90 not(Bitboard90 a){
        long resLow = ~a.low;
        long resHigh = ~a.high;
        return new Bitboard90(resLow,resHigh);
    }
    static Bitboard90 and(Bitboard90 a, Bitboard90 b){
        long resHigh = a.high & b.high;
        long resLow = a.low & b.low;
        return new Bitboard90(resLow,resHigh);
    }
    static Bitboard90 or(Bitboard90 a, Bitboard90 b){
        long resHigh = a.high | b.high;
        long resLow = a.low | b.low;
        return new Bitboard90(resLow,resHigh);
    }
    static Bitboard90 xor(Bitboard90 a, Bitboard90 b){
        Bitboard90 res = new Bitboard90();
        long resHigh = a.high ^ b.high;
        long resLow = a.low ^ b.low;
        return new Bitboard90(resLow,resHigh);
    }
//
//    /////////////////////////////////////////////////////////////////////////////
//    /// Shift Operationen
//    /////////////////////////////////////////////////////////////////////////////
//
//    //funktioniert so nur für nBits < 64 und nBits !=0
//    static de.tuberlin.tablut.ai.Bitboard81 shiftLeft(de.tuberlin.tablut.ai.Bitboard81 bb, int nBits){
//        de.tuberlin.tablut.ai.Bitboard81 res = new de.tuberlin.tablut.ai.Bitboard81();
//        res.low = bb.low << nBits;
//        long lowOverflow = bb.low >>> (64 - nBits);
//        res.high = (bb.high << nBits) | lowOverflow;
//        return res;
//    }
//    static de.tuberlin.tablut.ai.Bitboard81 shiftRight(de.tuberlin.tablut.ai.Bitboard81 bb, int nBits){
//        de.tuberlin.tablut.ai.Bitboard81 res = new de.tuberlin.tablut.ai.Bitboard81();
//        res.high = bb.high >>> nBits;
//        long highOverflow = bb.high << (64 - nBits);
//        res.low = (bb.low >>> nBits) | highOverflow;
//        return res;
//    }
//
//    //Die könnten alle ein Problem mit Overflow haben!!
//    static de.tuberlin.tablut.ai.Bitboard81 shiftN(de.tuberlin.tablut.ai.Bitboard81 bb){
//        return shiftRight(bb,9);
//    }
//    static de.tuberlin.tablut.ai.Bitboard81 shiftS(de.tuberlin.tablut.ai.Bitboard81 bb){
//        return shiftLeft(bb,9);
//    }
//    static de.tuberlin.tablut.ai.Bitboard81 shiftE(de.tuberlin.tablut.ai.Bitboard81 bb){
//        return shiftRight(bb,1);
//    }
//    static de.tuberlin.tablut.ai.Bitboard81 shiftW(de.tuberlin.tablut.ai.Bitboard81 bb){
//        return shiftLeft(bb,1);
//    }
////    static Bitboard81 dilation(Bitboard81 a){
////
////    }
////    static Bitboard81 erosion(Bitboard81 a){
////
////    }
//

    /////////////////////////////////////////////////////////////////////////////
    /// Quality of Life Operationen
    /////////////////////////////////////////////////////////////////////////////

    static boolean getBitAsMatrix(Bitboard90 bb, int row, int col){
        return getBit(bb,col+row*cols);
    }
    static void setBitAsMatrix(Bitboard90 bb,int row, int col){
        setBit(bb,col+row*cols);
    }

    static char[][] bbToMatrix(Bitboard90 bb){
        char[][] matrix = new char[rows][cols];
        for (int row = 0; row < rows; row++){
            for(int col = 0; col <cols; col++){
                if( getBit(bb,row*cols +col) ){
                    matrix[row][col] = '1';
                }
                else {
                    matrix[row][col] = '_';
                }
            }
        }
        return matrix;
    }
    static void printBBToConsole(Bitboard90 bb){
        char[][] matrix = bbToMatrix(bb);
//        System.out.println("Bitboard as Matrix");
        for (int row = 0; row < rows; row++){
            for (int col = 0; col < cols; col++){
                if (col == cols-1){
                    System.out.print("<"+matrix[row][col]+">");
                }
                else {
                    System.out.print("[" + matrix[row][col] + "]");
                }
            }
            System.out.print("\n");
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /// Testing mittels Main
    /////////////////////////////////////////////////////////////////////////////
    static void main() {


        //Tests for Shift
//        Bitboard90 z = new Bitboard90();
//        setBitAsMatrix(z,0,2);
//        setBitAsMatrix(z,1,5);
//        setBitAsMatrix(z,3,0);
//        setBitAsMatrix(z,6,8);
//        setBitAsMatrix(z,8,3);
//
//        System.out.println("Ausgangsboard z");
//        printBBToConsole(z);
//
//        System.out.println("shiftN");
//        printBBToConsole(shiftN(z));
//
//        System.out.println("shiftS");
//        printBBToConsole(shiftS(z));
//
//        System.out.println("shiftE");
//        printBBToConsole(shiftE(z));
//
//        System.out.println("shiftW");
//        printBBToConsole(shiftW(z));
//
//
        //Tests for Bit-Methods und Logische Operationen
        Bitboard90 x = new Bitboard90();
        setBitAsMatrix(x,0,1);
        setBitAsMatrix(x,1,5);
        setBitAsMatrix(x,0,8);
        setBitAsMatrix(x,8,8);

        Bitboard90 y = new Bitboard90();
        setBitAsMatrix(y,0,0);
        setBitAsMatrix(y,1,0);
        setBitAsMatrix(y,8,8);

        System.out.println("Ausgangs-BB x:");
        printBBToConsole(x);
        System.out.println();

        System.out.println("Ausgangs-BB y:");
        printBBToConsole(y);
        System.out.println();

        System.out.println("NOT-x:");
        printBBToConsole(not(x));
        System.out.println();

        System.out.println("AND-x,y:");
        printBBToConsole(and(x,y));
        System.out.println();

        System.out.println("OR-x,y:");
        printBBToConsole(or(x,y));
        System.out.println();

        System.out.println("XOR-x:");
        printBBToConsole(xor(x,y));
        System.out.println();

        System.out.print("getBitAsMatrix(x,8,8):");
        System.out.println(getBitAsMatrix(x,8,8));
        System.out.print("getBitAsMatrix(x,1,5):");
        System.out.println(getBitAsMatrix(x,1,5));

    }


}