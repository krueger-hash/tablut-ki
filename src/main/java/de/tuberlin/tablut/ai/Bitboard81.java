package de.tuberlin.tablut.ai;

//mit vordefinierter Größe 9x9, d.h. 81 Bits
public class Bitboard81 {

    long low; // erste 0-63 Bits
    long high; // Bits 64-127, wobei 81 bis 127 ungenutzt


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
    void setBit(int pos){
        if (pos < 64){
            this.low |= (1L << pos);
        }
        else {
            this.high |= (1L << (pos-64));
        }
    }

//  Prüft, ob an Stelle pos ein Bit gesetzt ist
    boolean getBit(int pos){
        if (pos < 64){
//          ist an Stelle pos eine 1, so bleibt ein Bit gesetzt und das Ergebnis ist !=0, alle anderen Stellen werden durch die Maske (1L<<Pos) ignoriert beim AND
            return (this.low & (1L << pos)) != 0;
        }
        else{
            return (this.high & (1L <<(pos-64))) !=0;
        }
    }

    //Entfernt Bit an Stelle pos; keine Prüfung, ob Bit überhaupt gesetzt ist
    void removeBit(int pos){
        if (pos <64){
            // mask ist die invertierte Maske, d.h. nur das Bit an Pos ist 0, der Rest 1, durch das AND wird die Ursprungsfolge beibehalten, aber Bit an pos auf jeden Fall auf 0 gesetzt
            long mask = ~(1L << pos);
            this.low = this.low & mask;
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    /// Logische Operationen
    /////////////////////////////////////////////////////////////////////////////

    static Bitboard81 not(Bitboard81 a){
        Bitboard81 res = new Bitboard81();
        res.low = ~a.low;
        res.high = ~a.high;
        return res;
    }

    static Bitboard81 and(Bitboard81 a, Bitboard81 b){
        Bitboard81 res = new Bitboard81();
        res.high = a.high & b.high;
        res.low = a.low & b.low;
        return res;
    }
    static Bitboard81 or(Bitboard81 a, Bitboard81 b){
        Bitboard81 res = new Bitboard81();
        res.high = a.high | b.high;
        res.low = a.low | b.low;
        return res;
    }
    static Bitboard81 xor(Bitboard81 a, Bitboard81 b){
        Bitboard81 res = new Bitboard81();
        res.high = a.high ^ b.high;
        res.low = a.low ^ b.low;
        return res;
    }

    /////////////////////////////////////////////////////////////////////////////
    /// Shift Operationen
    /////////////////////////////////////////////////////////////////////////////

//    void shiftUp(int n){
//
//    }
//    void shiftLeft(int n){
//
//    }
//    static Bitboard81 dilation(Bitboard81 a){
//
//    }
//    static Bitboard81 erosion(Bitboard81 a){
//
//    }

    static char[][] bitBoardToMatrix(Bitboard81 bb){
        char[][] matrix = new char[9][9];
        for (int row = 0; row < 9; row++){
            for(int col = 0; col <9; col++){
                if( bb.getBit(row*9 +col) ){
                    matrix[row][col] = '1';
                }
                else {
                    matrix[row][col] = '_';
                }
            }
        }
        return matrix;
    }

    static void printBBToConsole(Bitboard81 bb){
        char[][] matrix = bitBoardToMatrix(bb);
        System.out.println("Bitboard as Matrix");
        for (int row = 0; row < matrix.length;row++){
            for (int col = 0; col <matrix[row].length; col++){
                System.out.print("["+matrix[row][col]+"]");
            }
            System.out.print("\n");
        }
    }

    static void main() {
        Bitboard81 x = new Bitboard81();
        x.setBit(1);
        x.setBit(8);
        x.setBit(80);

        Bitboard81 y = new Bitboard81();
        y.setBit(0);
        y.setBit(9);
        y.setBit(80);

        System.out.println("Ausgangs-BB x:");
        printBBToConsole(x);

        System.out.println("Ausgangs-BB y:");
        printBBToConsole(y);

        System.out.println("NOT-x:");
        printBBToConsole(not(x));

        System.out.println("AND-x:");
        printBBToConsole(and(x,y));

        System.out.println("OR-x:");
        printBBToConsole(or(x,y));

        System.out.println("XOR-x:");
        printBBToConsole(xor(x,y));



    }
}
