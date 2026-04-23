package de.tuberlin.tablut.ai;

import java.io.*;
import java.net.Socket;

public class TablutRandomGameLoop {
    Board board;
    /*
        Arguments
        - register
        - login <token>
        - join <lobby-name>
        - move [0-8],[0-8],[0-8],[0-8]
        Bestätigung:
        - gspy ok
        Zeit-Bestätigung durch Server:
        - time <time-in-seconds>
     */

    public static void connectAndStart(String host, int port, String lobbyName) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // 1. Handshake
            send(out, "gspy");
            expectOk(in.readLine(), "Handshake");
            System.out.println("Handshake successful");

            // 2. Register
            send(out, "register");
            String registerResponse = in.readLine();
            System.out.println("Register response: " + registerResponse);
            if (registerResponse == null) throw new IOException("Register response is null");

            // 3. Login
            send(out, "login " + registerResponse);
            String loginResponse = in.readLine();
            expectOk(loginResponse, "Login");
            System.out.println("Login successful");

            // 4. Join Provided Lobby
            send(out, "join " + lobbyName);
            String joinResponse = in.readLine();
            expectOk(joinResponse, "Join");
            System.out.println("Joined lobby " + lobbyName);

            /*
                5. Wait and get initial config:
                    queued
                    config
                    set type tablut
                    set time_account 300.0
                    set player_time_accounts '{}'
                    set start_pos ''
                    ok
                    lobby
             */
            expectCommand(in.readLine(), "queued");
            expectCommand(in.readLine(), "config");
            expectCommand(in.readLine(), "set");




        }
    }

    private static void send(BufferedWriter out, String command) throws IOException {
        out.write(command);
        out.newLine();
        out.flush();
    }


    private static void expectOk(String line, String expected) throws IOException {
        expectCommand(line, "ok");
    }

    private static void expectCommand(String line, String expected) throws IOException{
        if (line == null || !line.startsWith(expected)) {
            throw new IOException("Expected "+expected+" from server, got: " + line);
        }
    }

//    private static void parseConfig (String message) {
//        if(message.startsWith("set time_account")){
//
//        }else(message.startsWith("set start_pos")){
//
//        }
//    }

    public static void run(String[] args) {
        try {
            connectAndStart("127.0.0.1", 5000, "game1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}