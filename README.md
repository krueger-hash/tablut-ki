# Tablut-KI

Simple Java project for Tablut AI.

## Prerequisites

- Java 25
- Maven 3.9+

Check your setup:

```powershell
java -version
mvn -version
```

## Run Tests

```powershell
mvn test
```

## Build The Project

```powershell
mvn clean package
```

## Run The Application

After compiling, start the main class with:
````powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main"
````

## Run Random Game Loop With Server Connection

Start the Tablut game server first. The random game loop connects as a client; it does not start the server itself.

The default connection settings are:

- Host: `127.0.0.1`
- Port: `5000`
- Lobby: `game1`
- Mode: create lobby

Run with the defaults:

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main"
```

Create a lobby explicitly:

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main" "-Dexec.args=--host 127.0.0.1 --port 5000 --lobby game1 --create"
```

Join an existing lobby from a second client:

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main" "-Dexec.args=--host 127.0.0.1 --port 5000 --lobby game1 --join"
```

If you already have a server token, pass it with `--token`:

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main" "-Dexec.args=--host 127.0.0.1 --port 5000 --lobby game1 --join --token YOUR_TOKEN"
```

## Choose The Search Algorithm (MCTS vs. Negamax)

Each client picks its move with a configurable search algorithm via the `--search` flag:

- `--search negamax` — alpha-beta / negamax with iterative deepening (default)
- `--search mcts` — Monte Carlo Tree Search

To play **MCTS against Negamax**, start the server, then launch two clients in the same lobby.

Client 1 (creates the lobby, plays with MCTS):

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main" "-Dexec.args=--lobby game1 --create --search mcts"
```

Client 2 (joins the lobby, plays with Negamax):

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Main" "-Dexec.args=--lobby game1 --join --search negamax"
```

If `--search` is omitted, the client defaults to `negamax`.

## Configure MCTS Enhancements Per Client

When `--search mcts` is used, the active enhancements are chosen with `--mcts-variant`:

- `base` — plain UCT (no Progressive Bias, no MAST)
- `bias` — UCT + Progressive Bias
- `mast` — UCT + MAST
- `bias_mast` — UCT + Progressive Bias + MAST

Because every client runs in its own JVM, two opponents can use different variants, e.g. plain
MCTS against MCTS+bias. Two more flags help when scripting matches:

- `--time-account <seconds>` — per-player time budget the lobby creator requests (default 300)
- `--label <name>` — tag echoed back in the machine-readable `GAME_RESULT` line each client prints
  on game end (used to attribute results to a variant)
- `--scheduler <random|round_robin>` — scheduler the lobby creator requests (default `random`).
  `round_robin` keeps the join order, so the lobby creator deterministically plays BLACK; this is
  what lets the tournament guarantee an even colour split.

## Run The MCTS Tournament

`de.tuberlin.tablut.ai.Tournament` plays a full round-robin between the four MCTS variants and then
pits the strongest variant against negamax. It drives everything through the real server by
spawning client JVMs into fresh lobbies, and runs games in parallel.

Start the server once and leave it running:

```powershell
# in the Gameserver25 checkout
uv run python -m gameserver
```

Then launch the tournament (it auto-resolves the classpath and connects to the running server):

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Tournament"
```

Optional arguments (shown with their defaults):

```powershell
mvn exec:java "-Dexec.mainClass=de.tuberlin.tablut.ai.Tournament" "-Dexec.args=--games 20 --time-account 60 --concurrency 6 --phase all --host 127.0.0.1 --port 5000"
```

- `--games N` — games per matchup. The tournament forces an even colour split by using the
  `round_robin` scheduler and alternating which variant creates the lobby, so each side plays
  exactly `N/2` games as BLACK and `N/2` as WHITE (use an even `N`).
- `--concurrency N` — number of games to run in parallel
- `--phase all|rr|vs` — run both phases, only the round-robin, or only best-vs-negamax
- `--best <variant>` — skip the round-robin and use this variant for the negamax phase
- `--max-pairs N` — limit the round-robin to the first N variant pairs (0 = all six)

Per-game results are written to `target/tournament_results.csv`; standings tables are printed to
the console.

## Project Structure

- `src/main/java/de.tuberlin.tablut.ai`: application source code
- `src/test/java/de.tuberlin.tablut.ai`: test code
- `pom.xml`: Maven build configuration

## Contribution / Commit Messages

Format: `<type>(<scope>): <subject>`

`<scope>` is optional

Example

```
feat: add hat wobble
^--^  ^------------^
|     |
|     +-> Summary in present tense.
|
+-------> Type: chore, docs, feat, fix, refactor, style, or test.
```

More Examples:

- `feat`: (new feature for the user, not a new feature for build script)
- `fix`: (bug fix for the user, not a fix to a build script)
- `docs`: (changes to the documentation)
- `style`: (formatting, missing semi colons, etc; no production code change)
- `refactor`: (refactoring production code, eg. renaming a variable)
- `test`: (adding missing tests, refactoring tests; no production code change)
- `chore`: (updating grunt tasks etc; no production code change)
