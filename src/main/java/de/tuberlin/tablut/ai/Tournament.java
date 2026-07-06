package de.tuberlin.tablut.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Self-contained tournament orchestrator that runs entirely through the real game server and the
 * existing {@link TablutGameLoop} client.
 *
 * <p>Prerequisite: start the game server once and leave it running, e.g.
 * <pre>cd ../Gameserver25 &amp;&amp; uv run python -m gameserver</pre>
 *
 * <p>This class then plays every match by spawning two client JVMs (one {@code --create}, one
 * {@code --join}) into a fresh lobby and parsing the {@code GAME_RESULT} line each client prints.
 * Games run in parallel via a fixed thread pool.
 *
 * <p>Phase 1 is a full round-robin between the MCTS variants (base, bias, mast, bias_mast).
 * Phase 2 plays the strongest variant from phase 1 against negamax.
 *
 * <p>Run with (after the server is up):
 * <pre>mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Tournament"</pre>
 * optionally passing arguments, e.g.
 * <pre>... "-Dexec.args=--games 20 --time-account 60 --concurrency 6"</pre>
 */
public class Tournament {

    private static final String[] VARIANTS = {"base", "bias", "mast", "bias_mast"};

    private static final Pattern COLOR_RE = Pattern.compile("Playing (BLACK|WHITE)");
    private static final Pattern RESULT_RE =
            Pattern.compile("GAME_RESULT .*winnerColor=(\\S+) outcome=(\\S+) reason=(\\S+)");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    // --- configuration (overridable via args) ---
    private String host = "127.0.0.1";
    private int port = 5000;
    private int games = 30;
    private int timeAccount = 20;
    private int concurrency = 6;
    private String phase = "all";       // all | rr | vs
    private String forcedBest = "";     // skip phase 1 and use this variant for phase 2
    private int maxPairs = 0;           // limit round-robin pairs (0 = all)

    // --- runtime ---
    private String javaBin;
    private String clientClasspath;
    private PrintWriter csv;
    private final Object csvLock = new Object();
    private final Object printLock = new Object();

    public static void main(String[] args) throws Exception {
        new Tournament().run(args);
    }

    private void run(String[] args) throws Exception {
        parseArgs(args);

        javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        clientClasspath = buildClientClasspath();
        log("Java: " + javaBin);
        log("Client classpath: " + clientClasspath.length() + " chars");

        if (!serverReachable()) {
            System.err.println("ERROR: game server not reachable on " + host + ":" + port + ".");
            System.err.println("Start it once and leave it running, then re-run this tournament:");
            System.err.println("    cd ../Gameserver25 && uv run python -m gameserver");
            System.exit(1);
        }
        log("Server reachable on " + host + ":" + port);

        Path csvPath = Path.of("target", "tournament_results.csv");
        Files.createDirectories(csvPath.getParent());
        csv = new PrintWriter(Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8));
        csv.println("phase,matchup,game,create_label,create_color,join_label,join_color,"
                + "winner_color,winner_label,reason,duration_s");
        csv.flush();

        log("games/matchup=" + games + ", time=" + timeAccount + "s, concurrency=" + concurrency);

        try {
            String best = forcedBest;

            if (phase.equals("all") || phase.equals("rr")) {
                List<GameOutcome> all = new ArrayList<>();
                int pairsRun = 0;
                outer:
                for (int i = 0; i < VARIANTS.length; i++) {
                    for (int j = i + 1; j < VARIANTS.length; j++) {
                        if (maxPairs > 0 && pairsRun >= maxPairs) {
                            break outer;
                        }
                        pairsRun++;
                        String v1 = VARIANTS[i];
                        String v2 = VARIANTS[j];
                        String tag = v1 + "-VS-" + v2;
                        log("");
                        log("#### Round-robin: " + v1 + " vs " + v2 + " (" + games + " games) ####");
                        all.addAll(runMatchup("rr", tag,
                                "mcts", v1, v1,
                                "mcts", v2, v2));
                    }
                }
                Map<String, Stats> stats = summarize(all, VARIANTS);
                List<String> ranked = printTable("ROUND-ROBIN STANDINGS", stats);
                best = ranked.get(0);
                log("");
                log("Best MCTS variant: " + best);
            }

            if (phase.equals("all") || phase.equals("vs")) {
                if (best == null || best.isBlank()) {
                    best = "bias_mast";
                }
                String tag = best + "-VS-negamax";
                log("");
                log("#### Phase 2: best MCTS (" + best + ") vs negamax (" + games + " games) ####");
                List<GameOutcome> vs = runMatchup("vs", tag,
                        "mcts", best, best,
                        "negamax", null, "negamax");
                Map<String, Stats> stats = summarize(vs, new String[]{best, "negamax"});
                printTable("BEST MCTS (" + best + ") vs NEGAMAX", stats);
            }

            log("");
            log("Done. Per-game CSV at: " + csvPath.toAbsolutePath());
        } finally {
            csv.flush();
            csv.close();
        }
    }

    // --- matchup execution -------------------------------------------------

    private List<GameOutcome> runMatchup(String phase, String tag,
                                         String aSearch, String aVariant, String aLabel,
                                         String bSearch, String bVariant, String bLabel)
            throws Exception {
        List<GameOutcome> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        try {
            List<Future<GameOutcome>> futures = new ArrayList<>();
            for (int g = 0; g < games; g++) {
                final int idx = g;
                // Alternate which side creates the lobby. With the round_robin scheduler the
                // creator always plays BLACK, so over an even number of games each side gets
                // exactly half its games as BLACK and half as WHITE.
                final boolean side1Creates = (g % 2 == 0);
                final String cSearch = side1Creates ? aSearch : bSearch;
                final String cVariant = side1Creates ? aVariant : bVariant;
                final String cLabel = side1Creates ? aLabel : bLabel;
                final String jSearch = side1Creates ? bSearch : aSearch;
                final String jVariant = side1Creates ? bVariant : aVariant;
                final String jLabel = side1Creates ? bLabel : aLabel;
                futures.add(pool.submit(() -> playGameWithRetry(tag, idx,
                        cSearch, cVariant, cLabel, jSearch, jVariant, jLabel)));
            }
            for (Future<GameOutcome> f : futures) {
                GameOutcome r = f.get();
                results.add(r);
                writeCsv(phase, tag, r);
                log(String.format(Locale.ROOT, "  [%s g%d] %s(%s) vs %s(%s) -> winner=%s (%s, %s, %.1fs)",
                        tag, r.gameIdx, r.createLabel, r.createColor, r.joinLabel, r.joinColor,
                        r.winnerLabel, r.winnerColor, r.reason, r.durationMs / 1000.0));
            }
        } finally {
            pool.shutdownNow();
        }
        return results;
    }

    private GameOutcome playGameWithRetry(String tag, int idx,
                                          String aSearch, String aVariant, String aLabel,
                                          String bSearch, String bVariant, String bLabel) {
        GameOutcome r = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            String lobby = "exp_" + tag + "_" + idx + "_" + attempt + "_"
                    + (System.currentTimeMillis() % 100000);
            r = playGame(tag, idx, lobby, aSearch, aVariant, aLabel, bSearch, bVariant, bLabel);
            if (!r.winnerLabel.equals("UNKNOWN")) {
                return r;
            }
            log("  [" + tag + " g" + idx + "] UNKNOWN result, retrying (" + (attempt + 1) + "/2)");
        }
        return r;
    }

    private GameOutcome playGame(String tag, int idx, String lobby,
                                 String aSearch, String aVariant, String aLabel,
                                 String bSearch, String bVariant, String bLabel) {
        long t0 = System.currentTimeMillis();
        long deadlineMs = timeAccount * 2L * 1000L + 90_000L;

        Process a = null;
        Process b = null;
        StringBuilder outA = new StringBuilder();
        StringBuilder outB = new StringBuilder();
        try {
            a = startClient(lobby, true, aSearch, aVariant, aLabel);
            Thread ta = drain(a, outA);
            ta.start();

            Thread.sleep(1200); // let the creator configure + open the lobby first

            b = startClient(lobby, false, bSearch, bVariant, bLabel);
            Thread tb = drain(b, outB);
            tb.start();

            a.waitFor(deadlineMs, TimeUnit.MILLISECONDS);
            long remaining = Math.max(1000L, deadlineMs - (System.currentTimeMillis() - t0));
            b.waitFor(remaining, TimeUnit.MILLISECONDS);

            ta.join(5000);
            tb.join(5000);
        } catch (Exception e) {
            // fall through: whatever we captured is parsed below
        } finally {
            if (a != null && a.isAlive()) {
                a.destroyForcibly();
            }
            if (b != null && b.isAlive()) {
                b.destroyForcibly();
            }
        }

        Parsed pa = parse(outA.toString());
        Parsed pb = parse(outB.toString());

        // Both clients apply the same move history, so they independently agree on the winner.
        String winnerColor = firstNonNull(pa.winnerColor, pb.winnerColor);
        if (pa.winnerColor != null && pb.winnerColor != null
                && !pa.winnerColor.equals(pb.winnerColor)) {
            if (isDecisive(pa.winnerColor) && !isDecisive(pb.winnerColor)) {
                winnerColor = pa.winnerColor;
            } else if (isDecisive(pb.winnerColor)) {
                winnerColor = pb.winnerColor;
            }
        }
        if (winnerColor == null) {
            winnerColor = "UNKNOWN";
        }

        Map<String, String> colorToLabel = new LinkedHashMap<>();
        if (pa.color != null) {
            colorToLabel.put(pa.color, aLabel);
        }
        if (pb.color != null) {
            colorToLabel.put(pb.color, bLabel);
        }

        String winnerLabel;
        if (winnerColor.equals("DRAW")) {
            winnerLabel = "DRAW";
        } else if (winnerColor.equals("BLACK") || winnerColor.equals("WHITE")) {
            winnerLabel = colorToLabel.getOrDefault(winnerColor, "UNKNOWN");
        } else {
            winnerLabel = "UNKNOWN";
        }

        GameOutcome r = new GameOutcome();
        r.gameIdx = idx;
        r.createLabel = aLabel;
        r.createColor = pa.color != null ? pa.color : "?";
        r.joinLabel = bLabel;
        r.joinColor = pb.color != null ? pb.color : "?";
        r.winnerColor = winnerColor;
        r.winnerLabel = winnerLabel;
        r.reason = pb.reason != null ? pb.reason : (pa.reason != null ? pa.reason : "?");
        r.durationMs = System.currentTimeMillis() - t0;
        return r;
    }

    private Process startClient(String lobby, boolean create, String search, String variant,
                                String label) throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add(javaBin);
        cmd.add("-cp");
        cmd.add(clientClasspath);
        cmd.add("de.tuberlin.tablut.ai.Main");
        cmd.add("--host");
        cmd.add(host);
        cmd.add("--port");
        cmd.add(String.valueOf(port));
        cmd.add("--lobby");
        cmd.add(lobby);
        cmd.add(create ? "--create" : "--join");
        cmd.add("--search");
        cmd.add(search);
        cmd.add("--time-account");
        cmd.add(String.valueOf(timeAccount));
        cmd.add("--label");
        cmd.add(label);
        // round_robin keeps the join order, so the creator deterministically plays BLACK.
        // This (combined with alternating the creator) lets us guarantee an even colour split.
        if (create) {
            cmd.add("--scheduler");
            cmd.add("round_robin");
        }
        if ("mcts".equals(search) && variant != null) {
            cmd.add("--mcts-variant");
            cmd.add(variant);
        }
        return new ProcessBuilder(cmd).redirectErrorStream(true).start();
    }

    private Thread drain(Process p, StringBuilder sink) {
        Thread t = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sink.append(line).append('\n');
                }
            } catch (IOException ignored) {
                // process killed / stream closed
            }
        });
        t.setDaemon(true);
        return t;
    }

    // --- parsing & stats ---------------------------------------------------

    private static final class Parsed {
        String color;
        String winnerColor;
        String reason;
    }

    private Parsed parse(String out) {
        Parsed p = new Parsed();
        Matcher m = COLOR_RE.matcher(out);
        if (m.find()) {
            p.color = m.group(1);
        }
        m = RESULT_RE.matcher(out);
        if (m.find()) {
            p.winnerColor = m.group(1);
            p.reason = m.group(3);
        }
        return p;
    }

    private static boolean isDecisive(String winnerColor) {
        return "BLACK".equals(winnerColor) || "WHITE".equals(winnerColor)
                || "DRAW".equals(winnerColor);
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }

    private static final class GameOutcome {
        int gameIdx;
        String createLabel, createColor, joinLabel, joinColor;
        String winnerColor, winnerLabel, reason;
        long durationMs;
    }

    private static final class Stats {
        int wins, losses, draws, unknown;
        double points;
    }

    private Map<String, Stats> summarize(List<GameOutcome> results, String[] labels) {
        Map<String, Stats> stats = new LinkedHashMap<>();
        for (String l : labels) {
            stats.put(l, new Stats());
        }
        for (GameOutcome r : results) {
            String wl = r.winnerLabel;
            String[] parts = {r.createLabel, r.joinLabel};
            if (wl.equals("DRAW")) {
                for (String p : parts) {
                    Stats s = stats.get(p);
                    if (s != null) {
                        s.draws++;
                        s.points += 0.5;
                    }
                }
            } else if (stats.containsKey(wl)) {
                Stats winner = stats.get(wl);
                winner.wins++;
                winner.points += 1.0;
                for (String p : parts) {
                    if (!p.equals(wl)) {
                        Stats s = stats.get(p);
                        if (s != null) {
                            s.losses++;
                        }
                    }
                }
            } else { // UNKNOWN
                for (String p : parts) {
                    Stats s = stats.get(p);
                    if (s != null) {
                        s.unknown++;
                    }
                }
            }
        }
        return stats;
    }

    private List<String> printTable(String title, Map<String, Stats> stats) {
        List<String> ranked = new ArrayList<>(stats.keySet());
        ranked.sort((x, y) -> {
            Stats sx = stats.get(x);
            Stats sy = stats.get(y);
            if (sy.points != sx.points) {
                return Double.compare(sy.points, sx.points);
            }
            return Integer.compare(sy.wins, sx.wins);
        });
        log("");
        log("==== " + title + " ====");
        log(String.format(Locale.ROOT, "%-12s %3s %3s %3s %3s %5s %8s",
                "variant", "W", "L", "D", "U", "pts", "winrate"));
        for (String lbl : ranked) {
            Stats s = stats.get(lbl);
            int decided = s.wins + s.losses + s.draws;
            double wr = decided > 0 ? (s.wins * 100.0 / decided) : 0.0;
            log(String.format(Locale.ROOT, "%-12s %3d %3d %3d %3d %5.1f %7.1f%%",
                    lbl, s.wins, s.losses, s.draws, s.unknown, s.points, wr));
        }
        return ranked;
    }

    private void writeCsv(String phase, String tag, GameOutcome r) {
        synchronized (csvLock) {
            csv.printf(Locale.ROOT, "%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%.1f%n",
                    phase, tag, r.gameIdx, r.createLabel, r.createColor, r.joinLabel, r.joinColor,
                    r.winnerColor, r.winnerLabel, r.reason, r.durationMs / 1000.0);
            csv.flush();
        }
    }

    // --- infrastructure ----------------------------------------------------

    private boolean serverReachable() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Build the classpath used to launch client JVMs: our compiled classes plus the runtime
     * dependencies. The dependency list is produced once via Maven's build-classpath goal.
     */
    private String buildClientClasspath() throws IOException, InterruptedException {
        Path classes = Path.of("target", "classes");
        Path cpFile = Path.of("target", "client-classpath.txt");

        List<String> mvn = new ArrayList<>();
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        if (windows) {
            mvn.add("cmd");
            mvn.add("/c");
        }
        mvn.add("mvn");
        mvn.add("-q");
        mvn.add("dependency:build-classpath");
        mvn.add("-Dmdep.outputFile=" + cpFile);

        log("Resolving dependency classpath via Maven ...");
        Process p = new ProcessBuilder(mvn).redirectErrorStream(true).start();
        StringBuilder mvnOut = new StringBuilder();
        Thread d = drain(p, mvnOut);
        d.start();
        boolean done = p.waitFor(120, TimeUnit.SECONDS);
        d.join(5000);
        if (!done || p.exitValue() != 0 || !Files.exists(cpFile)) {
            throw new IOException("Failed to resolve classpath via Maven:\n" + mvnOut);
        }

        String deps = Files.readString(cpFile, StandardCharsets.UTF_8).strip();
        return classes.toAbsolutePath() + java.io.File.pathSeparator + deps;
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--games" -> games = Integer.parseInt(args[++i]);
                case "--time-account" -> timeAccount = Integer.parseInt(args[++i]);
                case "--concurrency" -> concurrency = Integer.parseInt(args[++i]);
                case "--phase" -> phase = args[++i];
                case "--best" -> forcedBest = args[++i];
                case "--max-pairs" -> maxPairs = Integer.parseInt(args[++i]);
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
    }

    private void log(String msg) {
        synchronized (printLock) {
            System.out.println("[" + LocalTime.now().format(CLOCK) + "] " + msg);
            System.out.flush();
        }
    }
}
