package de.tuberlin.tablut.ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class TablutRandomGameLoop {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_LOBBY = "game1";
    private static final Random RANDOM = new Random();

    private Board board = new Board();
    private Player localPlayer = Player.BLACK;

    public static void run(String[] args) {
        ClientOptions options = ClientOptions.fromArgs(args);
        try {
            new TablutRandomGameLoop().connectAndPlay(options);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectAndPlay(ClientOptions options) throws IOException, InterruptedException {
        try (Socket socket = new Socket(options.host, options.port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            // Authenticate user using login / register
            authenticate(in, out, options.token);
            // create or enter a lobby
            boolean queuedAlreadyReceived = enterLobby(in, out, options);
            // get config settings
            receiveConfig(in, out, queuedAlreadyReceived);
            // main game loop
            playGame(in, out);
        }
    }

    private void authenticate(BufferedReader in, BufferedWriter out, String token) throws IOException {
        send(out, "gspy");
        expectCommand(read(in), "ok");

        String loginToken = token;
        if (loginToken == null || loginToken.isBlank()) {
            send(out, "register");
            loginToken = read(in);
            if (loginToken == null || loginToken.startsWith("err")) {
                throw new IOException("Could not register client: " + loginToken);
            }
            System.out.println("Registered token: " + loginToken);
        }

        send(out, "login " + loginToken);
        expectCommand(read(in), "ok");
        System.out.println("Logged in");
    }

    private boolean enterLobby(BufferedReader in, BufferedWriter out, ClientOptions options)
            throws IOException, InterruptedException {
        if (options.createLobby) {
            send(out, "create " + options.lobbyName);
            String createResponse = read(in);
            // fallback - if there is an error, simply try to join the lobby
            if (createResponse != null && createResponse.startsWith("err")) {
                System.out.println("Create failed (" + createResponse + "), trying to join existing lobby");
                send(out, "join " + options.lobbyName);
                expectCommand(read(in), "ok");
                return false;
            }
            expectCommand(createResponse, "ok");
            configureLobby(in, out);
            return startWhenReady(in, out);
        } else {
            send(out, "join " + options.lobbyName);
            expectCommand(read(in), "ok");
            System.out.println("Joined lobby " + options.lobbyName);
            return false;
        }
    }

    private void configureLobby(BufferedReader in, BufferedWriter out) throws IOException {
        sendAndExpectOk(in, out, "set game.type tablut");
        sendAndExpectOk(in, out, "set scheduler random");
        sendAndExpectOk(in, out, "set min_players 2");
        sendAndExpectOk(in, out, "set max_players 2");
        sendAndExpectOk(in, out, "set game.time_account 300");
    }

    // Start a game, or retry to start it, until second player is ready
    private boolean startWhenReady(BufferedReader in, BufferedWriter out) throws IOException, InterruptedException {
        while (true) {
            send(out, "start");
            String response = read(in);
            if (response == null) {
                throw new IOException("Connection closed while waiting for lobby start");
            }
            if (response.equals("queued")) {
                System.out.println("Lobby started");
                return true;
            }
            if (response.startsWith("err")) {
                System.out.println("Waiting for second player: " + response);
                Thread.sleep(1_000);
                continue;
            }
            throw new IOException("Unexpected start response: " + response);
        }
    }

    private void receiveConfig(BufferedReader in, BufferedWriter out, boolean queuedAlreadyReceived) throws IOException {
        /*
            << queued
            << config
            << set type tablut
            << set time_account 300.0
            << set player_time_accounts '{}'
            << set start_pos
            << ok
         */
        // If player created the lobby, skip "queued" message, since this message was already used in startWhenReady
        if (!queuedAlreadyReceived) {
            String first = read(in);
            expectCommand(first, "queued");
        }

        expectCommand(read(in), "config");
        // parse config lines until "ok" is received
        while (true) {
            String line = read(in);
            if ("ok".equals(line)) {
                send(out, "ok");
                return;
            }
            expectCommand(line, "set");
            parseConfigLine(line);
        }
    }

    // main game-loop
    private void playGame(BufferedReader in, BufferedWriter out) throws IOException {
        String state = read(in);
        // If server sends start, we are playing BLACK. If server sends wait, we are playing WHITE.
        if ("start".equals(state)) {
            localPlayer = Player.BLACK;
            System.out.println("Playing BLACK and moving first");
            makeRandomMove(in, out);
        } else if ("wait".equals(state)) {
            localPlayer = Player.WHITE;
            System.out.println("Playing WHITE and waiting for BLACK");
        } else {
            throw new IOException("Expected start or wait, got: " + state);
        }

        // main game-loop
        while (true) {
            String line = read(in);
            // Check for end of game
            if (line == null || "over".equals(line)) {
                System.out.println("Game over");
                return;
            }
            // Opponent move
            if (line.startsWith("move ")) {
                String[] parts = line.substring("move ".length()).split(",");
                if (parts.length != 4) {
                    throw new IOException("Invalid Tablut move from server: " + line);
                }
                Move opponentMove = Move.inputToMove(
                        board,
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[3].trim()),
                        Integer.parseInt(parts[2].trim())
                );
                // apply opponent move
                board.applyMove(opponentMove);
                System.out.println("Opponent Player");
                board.printBoard();
                System.out.println("Opponent played " + line.substring("move ".length()));
                // TODO: currently respond with random move - later -> respond with calculated move
                makeRandomMove(in, out);
                System.out.println("Our Move");
                board.printBoard();
                continue;
            }
            if (line.startsWith("err")) {
                throw new IOException("Server error: " + line);
            }
            throw new IOException("Unexpected game message: " + line);
        }
    }

    private void makeRandomMove(BufferedReader in, BufferedWriter out) throws IOException {
        ArrayList<Move> moves = Board.generateLegalMoves(board, localPlayer);
        if (moves.isEmpty()) {
            throw new IOException("No legal moves for " + localPlayer);
        }

        // Select random move from moves
        Move move = moves.get(RANDOM.nextInt(moves.size()));
        // Transform move to server format
        int[] indices = Move.moveToIndizes(move);
        String moveMessage = indices[0] + "," + indices[1] + "," + indices[2] + "," + indices[3];
        send(out, "move " + moveMessage);

        // Get time response from server
        String response = read(in);
        if (response == null) {
            throw new IOException("Connection closed after move " + moveMessage);
        }
        if (response.startsWith("err")) {
            throw new IOException("Server rejected generated move " + moveMessage + ": " + response);
        }
        expectCommand(response, "time");

        board.applyMove(move);
        System.out.println("Played " + moveMessage + " (" + response + ")");
    }

    private void parseConfigLine(String line) {
        /*
            << queued
            << config
            << set type tablut
            << set time_account 300.0
            << set player_time_accounts '{}'
            << set start_pos
            << ok
         */
        // TODO: also parse other fields later
        String[] parts = line.split(" ", 3);
        if (parts.length < 3) {
            return;
        }

        String key = parts[1];
        String value = unquote(parts[2]);
        if ("start_pos".equals(key) && !value.isBlank()) {
            board = Board.fenToBoard(String.join("", value.split("\\s+")));
        }
    }

    private void sendAndExpectOk(BufferedReader in, BufferedWriter out, String command) throws IOException {
        send(out, command);
        expectCommand(read(in), "ok");
    }

    private static void send(BufferedWriter out, String command) throws IOException {
        out.write(command);
        out.newLine();
        out.flush();
    }

    private static String read(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line != null) {
            System.out.println("<< " + line);
        }
        return line;
    }

    private static void expectCommand(String line, String expected) throws IOException {
        if (line == null || !line.startsWith(expected)) {
            throw new IOException("Expected " + expected + " from server, got: " + line);
        }
    }

    // Remove single or double quotes, because server uses shell-style quoting
    private static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return trimmed.substring(1, trimmed.length() - 1);
            }
        }
        return trimmed;
    }

    public static final class ClientOptions {
        final String host;
        final int port;
        final String lobbyName;
        final boolean createLobby;
        final String token;

        private ClientOptions(String host, int port, String lobbyName, boolean createLobby, String token) {
            this.host = host;
            this.port = port;
            this.lobbyName = lobbyName;
            this.createLobby = createLobby;
            this.token = token;
        }

        static ClientOptions fromArgs(String[] args) {
            String host = DEFAULT_HOST;
            int port = DEFAULT_PORT;
            String lobby = DEFAULT_LOBBY;
            boolean create = true;
            String token = null;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--host" -> host = args[++i];
                    case "--port" -> port = Integer.parseInt(args[++i]);
                    case "--lobby" -> lobby = args[++i];
                    case "--create" -> create = true;
                    case "--join" -> create = false;
                    case "--token" -> token = args[++i];
                    default -> {
                        if (i == 0) {
                            host = arg;
                        } else if (i == 1) {
                            port = Integer.parseInt(arg);
                        } else if (i == 2) {
                            lobby = arg;
                        } else if (i == 3) {
                            create = !"join".equals(arg.toLowerCase(Locale.ROOT));
                        } else if (i == 4) {
                            token = arg;
                        }
                    }
                }
            }

            return new ClientOptions(host, port, lobby, create, token);
        }
    }
}
