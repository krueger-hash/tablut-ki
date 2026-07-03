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

import de.tuberlin.tablut.ai.SearchAlgorithms.MCTS.MCTS_search;
import de.tuberlin.tablut.ai.SearchAlgorithms.MCTS.MCTS_Control_Parameters;

public class TablutRandomGameLoop {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_LOBBY = "game1";
    private static final SearchType DEFAULT_SEARCH = SearchType.NEGAMAX;
    private static final Random RANDOM = new Random();

    // Which search algorithm this client uses to pick its moves
    public enum SearchType {
        NEGAMAX,
        MCTS;

        static SearchType fromString(String value) {
            if (value == null) {
                return DEFAULT_SEARCH;
            }
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "mcts" -> MCTS;
                case "negamax" -> NEGAMAX;
                default -> throw new IllegalArgumentException("Unknown search algorithm: " + value
                        + " (expected 'negamax' or 'mcts')");
            };
        }
    }

    private Board board = new Board();
    private Player localPlayer = Player.BLACK;
    private SearchType searchType = DEFAULT_SEARCH;

    // Experiment metadata: label is an arbitrary tag for this client (e.g. the MCTS variant)
    // that is echoed back in the GAME_RESULT line so an orchestrator can attribute results.
    private String label = "";
    // Time account (seconds) requested when this client creates the lobby.
    private int timeAccountSeconds = 300;
    // Scheduler the lobby creator requests. "round_robin" keeps the join order
    // (creator plays BLACK), "random" shuffles colours.
    private String scheduler = "random";
    // Ensures exactly one GAME_RESULT line is emitted per game.
    private boolean resultReported = false;

    // Persistent MCTS tree so we can reuse the subtree across moves (updateRoot)
    private MCTS_search mcts;
    private Move lastOwnMove;          // our previous move (one ply down in the MCTS tree)
    private Move lastOpponentMove;     // opponent's most recent move (set in playGame)


    private static final int EXPECTED_MOVES = 60; // Total expected moves by current player in the entire game
    private static final int MIN_EXPECTED_MOVES = 10; // Expect that the game has always at least 10 moves left
    private static final int MAX_SEARCH_DEPTH = 6; // Maximum search depth for alpha-beta search
    private static final long SAFETY_BUFFER_MS = 1000; // Global tail reserve so the account never hits zero on the final move
    private static final long MOVE_OVERHEAD_MS = 250; // Per-move reserve for move I/O, network, server accounting and OS scheduling under load
    private long remainingMs = 300_000; // Remaining Ms for the entire game
    private int moveNumber = 0; // Number of moves made by the current player

    public static void run(String[] args) {
        ClientOptions options = ClientOptions.fromArgs(args);
        applyMctsVariant(options.mctsVariant);
        try {
            new TablutRandomGameLoop().connectAndPlay(options);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Configure which MCTS enhancements are active for this JVM. Each client runs in its own
    // process, so the static MCTS_Control_Parameters flags can differ between opponents.
    // null/unspecified leaves the compiled defaults untouched.
    private static void applyMctsVariant(String variant) {
        if (variant == null) {
            return;
        }
        switch (variant.trim().toLowerCase(Locale.ROOT)) {
            case "base", "uct", "plain" -> {
                MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE = false;
                MCTS_Control_Parameters.MAST_ACTIVE = false;
            }
            case "bias" -> {
                MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE = true;
                MCTS_Control_Parameters.MAST_ACTIVE = false;
            }
            case "mast" -> {
                MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE = false;
                MCTS_Control_Parameters.MAST_ACTIVE = true;
            }
            case "bias_mast", "mast_bias", "all", "full" -> {
                MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE = true;
                MCTS_Control_Parameters.MAST_ACTIVE = true;
            }
            default -> throw new IllegalArgumentException("Unknown MCTS variant: " + variant
                    + " (expected base | bias | mast | bias_mast)");
        }
        System.out.println("MCTS variant: " + variant
                + " (bias=" + MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE
                + ", mast=" + MCTS_Control_Parameters.MAST_ACTIVE + ")");
    }

    public void connectAndPlay(ClientOptions options) throws IOException, InterruptedException {
        this.searchType = options.searchType;
        this.label = options.label;
        this.timeAccountSeconds = options.timeAccountSeconds;
        this.scheduler = options.scheduler;
        // Seed our remaining-time estimate from the requested account so the first move's
        // budget is sized correctly even before the server reports a time update.
        this.remainingMs = this.timeAccountSeconds * 1000L;
        System.out.println("Using search algorithm: " + this.searchType);
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
        sendAndExpectOk(in, out, "set scheduler " + scheduler);
        sendAndExpectOk(in, out, "set min_players 2");
        sendAndExpectOk(in, out, "set max_players 2");
        sendAndExpectOk(in, out, "set game.time_account " + timeAccountSeconds);
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
//            makeRandomMove(in, out);
            board.sideToMove = localPlayer;
            playMove(out, in, board);
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
                reportResult(null);
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
                board.makeMove(opponentMove);
                // remember it so MCTS can advance its tree root before the next search
                lastOpponentMove = opponentMove;
                System.out.println("Opponent Player");
                board.printBoard();
                System.out.println("Opponent played " + line.substring("move ".length()));
                // The opponent's move may have ended the game (king captured/escaped or stalemate).
                // Don't run a search on a terminal position; just wait for the server's "over".
                if (board.gameIsEnd()) {
                    System.out.println("Opponent's move ended the game; awaiting 'over'");
                    continue;
                }
                board.sideToMove = localPlayer;
                playMove(out, in, board);
                continue;
            }
            if (line.startsWith("err")) {
                throw new IOException("Server error: " + line);
            }
            throw new IOException("Unexpected game message: " + line);
        }
    }

    public long calculateMoveBudgetMs(){
        int expectedMovesLeft = Math.max(MIN_EXPECTED_MOVES, EXPECTED_MOVES - moveNumber);
        // Reserve a global tail so we never spend the account down to zero on the last move.
        long usable = Math.max(0, remainingMs - SAFETY_BUFFER_MS);
        // Distribute the usable time equally over the moves we still expect to make.
        long slice = usable / expectedMovesLeft;
        // Spend strictly less than our slice: the server charges wall-clock time from when it sent
        // the opponent's move until it receives ours, which includes I/O, network, its own
        // accounting and (under parallel games) OS scheduling. Reserving per-move overhead keeps
        // the measured thinking time inside the budget and prevents losing on time.
        long budget = slice - MOVE_OVERHEAD_MS;
        // 10 ms floor so the search always has some time (BestMoveInTime needs a positive limit).
        return Math.max(10, Math.min(budget, usable));
    }

    private void playMove(BufferedWriter out, BufferedReader in, Board board) throws IOException{
        long moveBudgetMs = calculateMoveBudgetMs();
        // Count this as one of our moves so the budget ramps up as the game progresses.
        moveNumber++;
        Move move = computeMove(board, moveBudgetMs);

        System.out.println("Our Calculated Best Move: "+ move);
        int[] indices = Move.moveToIndizes(move);
        String moveMessage = indices[0] + "," + indices[1] + "," + indices[2] + "," + indices[3];
        send(out, "move " + moveMessage);

        String response = read(in);
        if (response == null) {
            throw new IOException("Connection closed after move " + moveMessage);
        }
        if (response.startsWith("err")) {
            // Most commonly the server rejects our move because we exceeded the time account,
            // which means we lose this game and the opponent wins. Report it and let the main
            // loop drain the subsequent "over" message.
            System.out.println("Server rejected our move (" + response + "); treating as a loss");
            Player opponent = (localPlayer == Player.BLACK) ? Player.WHITE : Player.BLACK;
            reportResult(opponent);
            return;
        }
        // parse the new time
//        parseConfigLine(response);
        String[] parts = response.split(" ");
        if (parts.length >= 2 && "time".equals(parts[0])) {
            remainingMs = (long) (Double.parseDouble(parts[1]) * 1000);
        }
        System.out.println("New Time Account: " + remainingMs + " ms");
//        expectCommand(response, "time");

        board.makeMove(move);

        System.out.println("New Board After Our Move");
        board.printBoard();
    }

    // Emit a single machine-parseable result line so an external orchestrator can tally games.
    // Both clients apply the full move history, so they independently agree on the winner colour.
    // forcedWinner is set only for terminations not reflected on the board (e.g. a time loss).
    private void reportResult(Player forcedWinner) {
        if (resultReported) {
            return;
        }
        resultReported = true;

        String winnerColor;
        String reason;
        if (forcedWinner != null) {
            winnerColor = forcedWinner.toString();
            reason = "timeloss";
        } else if (board.hasBlackWon()) {
            winnerColor = "BLACK";
            reason = "normal";
        } else if (board.hasWhiteWon()) {
            winnerColor = "WHITE";
            reason = "normal";
        } else if (board.isStalemate()) {
            winnerColor = "DRAW";
            reason = "stalemate";
        } else {
            // We received "over" cleanly (we did not lose on time ourselves, or that path would
            // have reported already) yet the board is neither a win/loss nor a stalemate. The only
            // remaining cause is that the opponent forfeited: disconnect, crash, or server timeout.
            // In every such case the server awards the game to the surviving player, i.e. us.
            winnerColor = localPlayer.toString();
            reason = "opponent_forfeit";
        }

        String outcome;
        if ("DRAW".equals(winnerColor)) {
            outcome = "DRAW";
        } else if ("UNKNOWN".equals(winnerColor)) {
            outcome = "UNKNOWN";
        } else {
            outcome = winnerColor.equals(localPlayer.toString()) ? "WIN" : "LOSS";
        }

        System.out.println("GAME_RESULT"
                + " label=" + ((label == null || label.isBlank()) ? "-" : label)
                + " search=" + searchType
                + " player=" + localPlayer
                + " winnerColor=" + winnerColor
                + " outcome=" + outcome
                + " reason=" + reason);
    }

    // Compute the move with the configured search algorithm
    private Move computeMove(Board board, long moveBudgetMs) {
        return switch (searchType) {
            case MCTS -> computeMctsMove(board, moveBudgetMs);
            case NEGAMAX -> new BestMoveInTime(Board.deepCopy(board), (int) moveBudgetMs).getMove();
        };
    }

    // Reuse the MCTS tree between moves: advance the root by (our last move, opponent's reply)
    // before searching, instead of rebuilding the tree from scratch every turn.
    private Move computeMctsMove(Board board, long moveBudgetMs) {
        if (mcts == null) {
            // First own move: build the tree from the current position.
            mcts = new MCTS_search(Board.deepCopy(board));
        } else {
            // We have searched before; the tree root is the position before our last move.
            // Descend our last move and the opponent's reply to reach the current position.
            mcts.updateRoot(lastOwnMove, lastOpponentMove);
        }

        Move move = mcts.search(moveBudgetMs);
        lastOwnMove = move;
        return move;
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
        // The server announces the per-player time budget as "time_account"; keep our
        // remaining-time estimate in sync so the first move's budget is sized correctly.
        if (("time".equals(key) || "time_account".equals(key)) && !value.isBlank()) {
            remainingMs = (long) (Double.parseDouble(value) * 1000);
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
        final SearchType searchType;
        final String mctsVariant;
        final int timeAccountSeconds;
        final String label;
        final String scheduler;

        private ClientOptions(String host, int port, String lobbyName, boolean createLobby, String token,
                              SearchType searchType, String mctsVariant, int timeAccountSeconds, String label,
                              String scheduler) {
            this.host = host;
            this.port = port;
            this.lobbyName = lobbyName;
            this.createLobby = createLobby;
            this.token = token;
            this.searchType = searchType;
            this.mctsVariant = mctsVariant;
            this.timeAccountSeconds = timeAccountSeconds;
            this.label = label;
            this.scheduler = scheduler;
        }

        static ClientOptions fromArgs(String[] args) {
            String host = DEFAULT_HOST;
            int port = DEFAULT_PORT;
            String lobby = DEFAULT_LOBBY;
            boolean create = true;
            String token = null;
            SearchType search = DEFAULT_SEARCH;
            String mctsVariant = null;
            int timeAccountSeconds = 300;
            String label = "";
            String scheduler = "random";

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--host" -> host = args[++i];
                    case "--port" -> port = Integer.parseInt(args[++i]);
                    case "--lobby" -> lobby = args[++i];
                    case "--create" -> create = true;
                    case "--join" -> create = false;
                    case "--token" -> token = args[++i];
                    case "--search" -> search = SearchType.fromString(args[++i]);
                    case "--mcts-variant" -> mctsVariant = args[++i];
                    case "--time-account" -> timeAccountSeconds = Integer.parseInt(args[++i]);
                    case "--label" -> label = args[++i];
                    case "--scheduler" -> scheduler = args[++i];
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

            return new ClientOptions(host, port, lobby, create, token, search, mctsVariant, timeAccountSeconds, label, scheduler);
        }
    }
}
