package de.tuberlin.tablut.ai;
import java.util.Arrays;
import java.util.Objects;

//mit vordefinierter Größe 9x9, d.h. 81 Bits + je Seperation am Ende der Zeile für 9x10 Bits d.h. 90 Bits
public class Bitboard90 {

    /////////////////////////////////////////////////////////////////////////////
    /// globale Variablen
    /////////////////////////////////////////////////////////////////////////////
    public static final int rows = 9;
    public static final int cols = 10;
    @Deprecated
    static final Bitboard90 fieldMask = createBoardMask();

    /////////////////////////////////////////////////////////////////////////////
    /// lokale Variablen
    /////////////////////////////////////////////////////////////////////////////
    public long low; // erste 0-63 Bits
    public long high; // Bits 64-89; 90 bis 127 ungenutzt


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

    @Deprecated
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

    // count bits on bit-board
    public int bitCount(){
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
        if (pos < 0 || pos >= 128) {
            throw new IllegalArgumentException();
        }
        if (pos < 64){
            bb.low |= (1L << pos);
        }
        else {
            bb.high |= (1L << (pos-64));
        }
    }

    //  Prüft, ob an Stelle pos ein Bit gesetzt ist
    static boolean getBit(Bitboard90 bb,int pos){
        if (pos < 0 || pos >= 128) {
            throw new IllegalArgumentException();
        }
        if (pos < 64){
            return (bb.low & (1L << pos)) != 0;
        }
        else{
            return (bb.high & (1L <<(pos-64))) !=0;
        }
    }

    //Entfernt Bit an Stelle pos; keine Prüfung, ob Bit überhaupt gesetzt ist
    static void removeBit(Bitboard90 bb, int pos){
        if (pos < 0 || pos >= 128) {
            throw new IllegalArgumentException();
        }
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    static boolean getBitAsMatrix_N(Bitboard90 bb, int row, int col){
        // N is out of bounds
        if(row == 0) return false;
        return getBit(bb,col+row*cols - cols);
    }
    @Deprecated
    static boolean getBitAsMatrix_E(Bitboard90 bb, int row, int col){
        if(row == 8 && col == 8) return false;
        return getBit(bb,col+row*cols + 1);
    }
    @Deprecated
    static boolean getBitAsMatrix_S(Bitboard90 bb, int row, int col){
        // S out of bounds
        if(row == 8) return false;
        return getBit(bb,col+row*cols + cols);
    }
    @Deprecated
    static boolean getBitAsMatrix_W(Bitboard90 bb, int row, int col){
        if(row == 0 && col == 0) return false;
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

    //gibt die entsprechende Zeile und Spalte zu einem BitWert zurück
    static int[] bitToMatrix(int bit){
        int col = bit % cols;
        int row = bit / cols;//Math.floorDiv(bit,cols); // mit floor unnötig, da immer nur positive Werte verwendet werden
        return new int[]{row,col};
    }

    // Returns list of indices of all set bits
    static int[] BitboardToIndexList(Bitboard90 board){
        int[] indexList = new int[board.bitCount()];
        long lows = board.low;
        int i = 0;
        while (lows != 0) {
            indexList[i++] = Long.numberOfTrailingZeros(lows);
            lows &= lows - 1;
        }
        long highs = board.high;
        while (highs != 0) {
            indexList[i++] = 64 + Long.numberOfTrailingZeros(highs);
            highs &= highs - 1;
        }
        return indexList;
    }
}