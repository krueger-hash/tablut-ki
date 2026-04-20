package de.tuberlin.tablut.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Bitboard90Test {

    @Test
    void createBoardMask() {
        Bitboard90 mask = Bitboard90.fieldMask;
        assertEquals(81,Long.bitCount(mask.low) + Long.bitCount(mask.high));
        for(int i = 9;i<90;i+=10){
            // eigentlich ist es schlechter Stil, andere zu testende Methoden in Tests zu verwenden ...
            assertFalse(Bitboard90.getBit(mask, i));
        }
    }

    @Test
    void bitCount() {
        long high = 0b01111; //4 bits gesetzt
        long low = 0b01010111; //5 bits gesetzt
        Bitboard90 testBB = new Bitboard90(low,high);
        assertEquals(9,testBB.bitCount());
    }

    @Test
    void setBit() {
        Bitboard90 testBB = new Bitboard90();
        Bitboard90.setBit(testBB,1);
        assertEquals(0b10,testBB.low);
        Bitboard90.setBit(testBB,64);
        assertEquals(0b1,testBB.high);
    }

    @Test
    void getBit() {
        Bitboard90 testBB = new Bitboard90();
        testBB.low = 0b10;
        assertTrue(Bitboard90.getBit(testBB,1));
        assertFalse(Bitboard90.getBit(testBB,0));
        testBB.high = 0b101;
        assertTrue(Bitboard90.getBit(testBB,64));
        assertFalse(Bitboard90.getBit(testBB,65));
        assertTrue(Bitboard90.getBit(testBB,66));
    }

    @Test
    void removeBit() {
        Bitboard90 testBB = new Bitboard90();
        testBB.low = 0b10;
        testBB.high = 0b101;

        Bitboard90.removeBit(testBB,1);
        assertEquals(0,testBB.low);

        Bitboard90.removeBit(testBB,64);
        assertEquals(0b100,testBB.high);
        Bitboard90.removeBit(testBB,65);
        assertEquals(0b100,testBB.high);
        Bitboard90.removeBit(testBB,66);
        assertEquals(0,testBB.high);

    }

    @Test
    void not() {
        Bitboard90 testBB = new Bitboard90();
        assertEquals(0,testBB.bitCount());
        assertEquals(128,Bitboard90.not(testBB).bitCount()); // alle Bits werden geflippt, auch die, die nicht für das Board genutzt werden

        Bitboard90.setBit(testBB,64);
        Bitboard90 notTestBB = Bitboard90.not(testBB);
        assertFalse(Bitboard90.getBit(notTestBB,64));
    }

    @Test
    void and() {
        Bitboard90 x = new Bitboard90();
        Bitboard90.setBitAsMatrix(x,0,1);
        Bitboard90.setBitAsMatrix(x,1,5);
        Bitboard90.setBitAsMatrix(x,0,8);
        Bitboard90.setBitAsMatrix(x,8,8);

        Bitboard90 y = new Bitboard90();
        Bitboard90.setBitAsMatrix(y,0,0);
        Bitboard90.setBitAsMatrix(y,1,0);
        Bitboard90.setBitAsMatrix(y,8,8);

        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,8,8);
        assertEquals(expected,Bitboard90.and(x,y));
    }

    @Test
    void or() {
        Bitboard90 x = new Bitboard90();
        Bitboard90.setBitAsMatrix(x,0,1);
        Bitboard90.setBitAsMatrix(x,1,5);
        Bitboard90.setBitAsMatrix(x,0,8);
        Bitboard90.setBitAsMatrix(x,8,8);

        Bitboard90 y = new Bitboard90();
        Bitboard90.setBitAsMatrix(y,0,0);
        Bitboard90.setBitAsMatrix(y,1,0);
        Bitboard90.setBitAsMatrix(y,8,8);

        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,0,1);
        Bitboard90.setBitAsMatrix(expected,1,5);
        Bitboard90.setBitAsMatrix(expected,0,8);
        Bitboard90.setBitAsMatrix(expected,8,8);
        Bitboard90.setBitAsMatrix(expected,0,0);
        Bitboard90.setBitAsMatrix(expected,1,0);

        assertEquals(expected,Bitboard90.or(x,y));

    }

    @Test
    void xor() {
        Bitboard90 x = new Bitboard90();
        Bitboard90.setBitAsMatrix(x,0,1);
        Bitboard90.setBitAsMatrix(x,1,5);
        Bitboard90.setBitAsMatrix(x,0,8);
        Bitboard90.setBitAsMatrix(x,8,8);

        Bitboard90 y = new Bitboard90();
        Bitboard90.setBitAsMatrix(y,0,0);
        Bitboard90.setBitAsMatrix(y,1,0);
        Bitboard90.setBitAsMatrix(y,8,8);

        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,0,1);
        Bitboard90.setBitAsMatrix(expected,1,5);
        Bitboard90.setBitAsMatrix(expected,0,8);
        Bitboard90.setBitAsMatrix(expected,0,0);
        Bitboard90.setBitAsMatrix(expected,1,0);

        assertEquals(expected,Bitboard90.xor(x,y));
    }

    @Test
    void shiftLeft() {
    }

    @Test
    void shiftRight() {
    }


    @Test
    void shiftN() {
        Bitboard90 z = new Bitboard90();
        Bitboard90.setBitAsMatrix(z,0,2);
        Bitboard90.setBitAsMatrix(z,1,5);
        Bitboard90.setBitAsMatrix(z,3,0);
        Bitboard90.setBitAsMatrix(z,6,5);
        Bitboard90.setBitAsMatrix(z,6,8);
        Bitboard90.setBitAsMatrix(z,8,3);
        Bitboard90.setBitAsMatrix(z,8,8);


        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,0,5);
        Bitboard90.setBitAsMatrix(expected,2,0);
        Bitboard90.setBitAsMatrix(expected,5,5);
        Bitboard90.setBitAsMatrix(expected,5,8);
        Bitboard90.setBitAsMatrix(expected,7,3);
        Bitboard90.setBitAsMatrix(expected,7,8);

        assertEquals(expected,Bitboard90.shiftN(z));

    }

    @Test
    void shiftS() {
        Bitboard90 z = new Bitboard90();
        Bitboard90.setBitAsMatrix(z,0,2);
        Bitboard90.setBitAsMatrix(z,1,5);
        Bitboard90.setBitAsMatrix(z,3,0);
        Bitboard90.setBitAsMatrix(z,6,5);
        Bitboard90.setBitAsMatrix(z,6,8);
        Bitboard90.setBitAsMatrix(z,8,3);
        Bitboard90.setBitAsMatrix(z,8,8);

        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,1,2);
        Bitboard90.setBitAsMatrix(expected,2,5);
        Bitboard90.setBitAsMatrix(expected,4,0);
        Bitboard90.setBitAsMatrix(expected,7,5);
        Bitboard90.setBitAsMatrix(expected,7,8);

        //Bei shiftS werden Bits im high ggf. über die Grenze der Spielfeld-Bits hinweggeschoben ~col9 ...
        Bitboard90.setBitAsMatrix(expected,9,3);
        Bitboard90.setBitAsMatrix(expected,9,8);

        assertEquals(expected,Bitboard90.shiftS(z));
    }

    @Test
    void shiftE() {
        Bitboard90 z = new Bitboard90();
        Bitboard90.setBitAsMatrix(z,0,2);
        Bitboard90.setBitAsMatrix(z,1,5);
        Bitboard90.setBitAsMatrix(z,3,0);
        Bitboard90.setBitAsMatrix(z,6,5);
        Bitboard90.setBitAsMatrix(z,6,8);
        Bitboard90.setBitAsMatrix(z,8,3);
        Bitboard90.setBitAsMatrix(z,8,8);

        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,0,1);
        Bitboard90.setBitAsMatrix(expected,1,4);
        //Bit auf Seperation Bit!
        Bitboard90.setBitAsMatrix(expected,2,9);

        Bitboard90.setBitAsMatrix(expected,6,4);
        Bitboard90.setBitAsMatrix(expected,6,7);
        Bitboard90.setBitAsMatrix(expected,7,2);
        Bitboard90.setBitAsMatrix(expected,7,7);

        assertEquals(expected,Bitboard90.shiftE(z));
    }

    @Test
    void shiftW() {
        Bitboard90 z = new Bitboard90();
        Bitboard90.setBitAsMatrix(z,0,2);
        Bitboard90.setBitAsMatrix(z,1,5);
        Bitboard90.setBitAsMatrix(z,3,0);
        Bitboard90.setBitAsMatrix(z,6,5);
        Bitboard90.setBitAsMatrix(z,6,8);
        Bitboard90.setBitAsMatrix(z,8,3);
        Bitboard90.setBitAsMatrix(z,8,8);

        Bitboard90 expected = new Bitboard90();
        Bitboard90.setBitAsMatrix(expected,0,4);
        Bitboard90.setBitAsMatrix(expected,1,6);
        Bitboard90.setBitAsMatrix(expected,3,1);
        Bitboard90.setBitAsMatrix(expected,6,6);
        Bitboard90.setBitAsMatrix(expected,6,9);
        Bitboard90.setBitAsMatrix(expected,7,2);
        Bitboard90.setBitAsMatrix(expected,7,7);
    }

    @Test
    void dilation() {
    }

    @Test
    void erosion() {
    }

    @Test
    void getBitAsMatrix() {
    }

    @Test
    void setBitAsMatrix() {
    }

    @Test
    void bbToMatrix() {
    }

    @Test
    void printBBToConsole() {
    }
}