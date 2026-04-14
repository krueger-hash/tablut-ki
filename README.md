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
