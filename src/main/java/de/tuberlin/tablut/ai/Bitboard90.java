package de.tuberlin.tablut.ai;


import java.util.Arrays;
import java.util.Objects;

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

    int bitCount(){
        return Long.bitCount(this.low) + Long.bitCount(this.high);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Bitboard90 that = (Bitboard90) o;
        return this.low == that.low && this.high == that.high;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
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

    /////////////////////////////////////////////////////////////////////////////
    /// Shift Operationen
    /////////////////////////////////////////////////////////////////////////////

    // es gibt auch geschicktere Varianten, die ohne Branches auskommen, aber das vllt eher später...
    static Bitboard90 shiftLeft(Bitboard90 bb, int nBits){
        if (nBits == 0){
            return bb;
        }
        if (nBits < 64){
            long resLow = bb.low << nBits;

            long lowOverflow = bb.low >>> (64 - nBits); //Overflow-Bits berechnen, die bei resHigh "vorgehängt werden"
            long resHigh = (bb.high << nBits) | lowOverflow;
            return new Bitboard90(resLow,resHigh);
        }
        else {
            long resLow = 0; // alles in low wird "geleert", da es kleiner 64 Bit ist
            long resHigh = (bb.low << (nBits-64)); // resHigh dann das um nBits-64 geshiftete low (musste es mir aufmalen...)
            return new Bitboard90(resLow,resHigh);
        }

    }
    static Bitboard90 shiftRight(Bitboard90 bb, int nBits){
        if(nBits==0){
            return bb;
        }
        if(nBits<64){
            long resHigh = bb.high >>> nBits;
            long highOverflow = bb.high << (64 - nBits);
            long resLow = (bb.low >>> nBits) | highOverflow;
            return new Bitboard90(resLow,resHigh);
        }
        else {
            long resHigh = 0;
            long resLow = (bb.high >>> (nBits-64));
            return new Bitboard90(resLow,resHigh);
        }
    }

    //Shift jeweils um 1 Feld
    static Bitboard90 shiftN(Bitboard90 bb){
        return shiftRight(bb,cols);
    }
    static Bitboard90 shiftS(Bitboard90 bb){
        return shiftLeft(bb,cols);
    }
    static Bitboard90 shiftE(Bitboard90 bb){
        return shiftRight(bb,1);
    }
    static Bitboard90 shiftW(Bitboard90 bb){
        return shiftLeft(bb,1);
    }

    //Grundlage für Positions-checking, ggf. so nicht notwendig, sondern nur NS bzw. EW-check nötig
    static Bitboard90 dilation(Bitboard90 bb){
        Bitboard90 bbN = shiftN(bb);
        Bitboard90 bbE = shiftE(bb);
        Bitboard90 bbS = shiftS(bb);
        Bitboard90 bbW = shiftW(bb);
        return or(
                bbW,or(
                    bbS,or(
                        bbE,or(
                                bbN,bb
                                )
                        )
                )
        );

    }
    static Bitboard90 erosion(Bitboard90 bb){
        Bitboard90 bbN = shiftN(bb);
        Bitboard90 bbE = shiftE(bb);
        Bitboard90 bbS = shiftS(bb);
        Bitboard90 bbW = shiftW(bb);
        return and(
                bbW,and(
                        bbS,and(
                                bbE,and(
                                        bbN,bb
                                )
                        )
                )
        );
    }


    /////////////////////////////////////////////////////////////////////////////
    /// Quality of Life Operationen
    /////////////////////////////////////////////////////////////////////////////
    static boolean getBitAsMatrix(Bitboard90 bb, int row, int col){
        return getBit(bb,col+row*cols);
    }
    static void setBitAsMatrix(Bitboard90 bb,int row, int col){
        setBit(bb,col+row*cols);
    }
    //Prüfe Bits auf benachbarten Feldern: NESW, Ausgangsfeld als Matrix
    static boolean getBitAsMatrix_N(Bitboard90 bb, int row, int col){
        return getBit(bb,col+row*cols - cols);
    }
    static boolean getBitAsMatrix_E(Bitboard90 bb, int row, int col){
        return getBit(bb,col+row*cols + 1);
    }
    static boolean getBitAsMatrix_S(Bitboard90 bb, int row, int col){
        return getBit(bb,col+row*cols + cols);
    }
    static boolean getBitAsMatrix_W(Bitboard90 bb, int row, int col){
        return getBit(bb,col+row*cols - 1);
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
        System.out.print("\n");
    }

    //gibt die entsprechende Zeile und Splte zu einem BitWert zurück
    static int[] bitToMatrix(int bit){
        int col = bit % cols;
        int row = Math.floorDiv(bit,cols);
        return new int[]{row,col};
    }

    /////////////////////////////////////////////////////////////////////////////
    /// Testing mittels Main
    /////////////////////////////////////////////////////////////////////////////
    static void main() {

        System.out.println(Arrays.toString(Bitboard90.bitToMatrix(88)));

//        Bitboard90 testBB = new Bitboard90();
//        Bitboard90.setBitAsMatrix(testBB,8,8);
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,10));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,20));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,30));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,40));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,50));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,60));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,70));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,80));
//        Bitboard90.printBBToConsole(Bitboard90.shiftRight(testBB,90));


//        //Tests for Shift
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
//        System.out.println("dilation");
//        printBBToConsole(dilation(z));
//
//        System.out.println("erosion-test");
//        Bitboard90 et = new Bitboard90();
//        for(int row = 0; row <5; row++){
//            for(int col = 0; col <5; col++) {
//                if (!(row == 2 && (col == 1 || col == 3))) {
//                    setBitAsMatrix(et, row, col);
//                }
//            }
//        }
//        System.out.println("Ausgangs-BB et:");
//        printBBToConsole(et);
//        System.out.println("Erosion et:");
//        printBBToConsole(erosion(et));


        //Tests for Bit-Methods und Logische Operationen
//        Bitboard90 x = new Bitboard90();
//        setBitAsMatrix(x,0,1);
//        setBitAsMatrix(x,1,5);
//        setBitAsMatrix(x,0,8);
//        setBitAsMatrix(x,8,8);
//
//        Bitboard90 y = new Bitboard90();
//        setBitAsMatrix(y,0,0);
//        setBitAsMatrix(y,1,0);
//        setBitAsMatrix(y,8,8);
//
//        System.out.println("Ausgangs-BB x:");
//        printBBToConsole(x);
//        System.out.println();
//
//        System.out.println("Ausgangs-BB y:");
//        printBBToConsole(y);
//        System.out.println();
//
//        System.out.println("NOT-x:");
//        printBBToConsole(not(x));
//        System.out.println();
//
//        System.out.println("AND-x,y:");
//        printBBToConsole(and(x,y));
//        System.out.println();
//
//        System.out.println("OR-x,y:");
//        printBBToConsole(or(x,y));
//        System.out.println();
//
//        System.out.println("XOR-x:");
//        printBBToConsole(xor(x,y));
//        System.out.println();
//
//        System.out.print("getBitAsMatrix(x,8,8):");
//        System.out.println(getBitAsMatrix(x,8,8));
//        System.out.print("getBitAsMatrix(x,1,5):");
//        System.out.println(getBitAsMatrix(x,1,5));

    }


}