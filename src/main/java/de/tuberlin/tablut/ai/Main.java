package de.tuberlin.tablut.ai;

/**
 * Main class for the Tablut AI.
 * Can be executed from the terminal: mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main"
 * More configuration options can be passed as arguments - for detailed descriptions read the README.md
 */
public class Main {
    public static void main(String[] args) {
        TablutGameLoop gameLoop = new TablutGameLoop();
        gameLoop.run(args);
    }
}
