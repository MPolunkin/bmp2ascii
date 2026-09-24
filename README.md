# bmp2ascii

[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![picocli](https://img.shields.io/badge/picocli-4.7.7-blue.svg)](https://picocli.info/)
[![JUnit](https://img.shields.io/badge/JUnit-6.1.3-25A162.svg)](https://junit.org/junit6/)

A lightweight Java CLI tool for bidirectional conversion between uncompressed 24-bit BMP images and ASCII art text files.

Developed for the **DAI** (*Développement d'Applications Internet*) course at **HEIG-VD**.

## Authors

- **Michel Polunkin** ([@MPolunkin](https://github.com/MPolunkin))
- **Shanshe Gundishvili** ([@MinusW](https://github.com/MinusW))

---

## Prerequisites

- **Java JDK 21+** (developed and tested on Java 25)

## Build

Build the executable JAR using the Maven wrapper:

```bash
# Linux / macOS
./mvnw clean package

# Windows
.\mvnw.cmd clean package
```

The executable JAR is generated at `target/bmp2ascii-1.0-SNAPSHOT.jar`.

---

## Usage

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar [COMMAND] [OPTIONS]
```

### Commands

#### `asciify` — Convert BMP to ASCII

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar asciify -i input.bmp -o output.txt
```

| Option | Description | Default |
|---|---|---|
| `-i, --input=<file>` | Input 24-bit uncompressed `.bmp` file *(required)* | — |
| `-o, --output=<file>` | Output `.txt` file *(required)* | — |
| `-O, --output-encoding=<enc>` | Character encoding for output | `UTF-8` |
| `-w, --width=<cols>` | Downscale width (preserves aspect ratio) | Original |
| `--invert` | Invert brightness mapping | `false` |

#### `bmpify` — Convert ASCII to BMP

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar bmpify -i input.txt -o output.bmp
```

| Option | Description | Default |
|---|---|---|
| `-i, --input=<file>` | Input ASCII art `.txt` file *(required)* | — |
| `-o, --output=<file>` | Output `.bmp` file *(required)* | — |
| `-I, --input-encoding=<enc>` | Character encoding for input | `UTF-8` |
| `-s, --scale=<factor>` | Pixel scale factor per character | `1` |

---

## Commit Guidelines

To ensure repository cleanliness and traceability, all contributions must adhere to the following standards:

### 1. Conventional Commits
All commit messages must follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```text
<type>[optional scope]: <description>
```

Common types:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring without behavioral changes
- `test`: Adding or updating tests
- `chore`: Maintenance, build tasks, or dependency updates

### 2. Signed Commits
All commits must be cryptographically signed using GPG or SSH.

Enable automated signing:
```bash
git config --global commit.gpgsign true
```

Or sign manually:
```bash
git commit -S -m "feat: add bmp header validation"
```

Unsigned commits will not be accepted.

---

## License

This project is licensed under the [MIT License](LICENSE).
