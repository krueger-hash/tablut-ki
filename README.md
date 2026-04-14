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
