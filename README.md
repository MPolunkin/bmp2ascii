# bmp2ascii

[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![picocli](https://img.shields.io/badge/picocli-4.7.7-blue.svg)](https://picocli.info/)
[![JUnit](https://img.shields.io/badge/JUnit-6.1.3-25A162.svg)](https://junit.org/junit6/)

A lightweight Java CLI tool for bidirectional conversion between uncompressed 24-bit BMP images and ASCII art text files.

Developed for the **DAI** (*Développement d'Applications Internet*) course at **HEIG-VD**.

## Authors

- **Michel Polunkin** ([@MPolunkin](https://github.com/MPolunkin))
- **Shanshe Gundishvili** ([@MinusW](https://github.com/MinusW))

---

## Installation & Build

### Prerequisites

- **Java JDK 25+**

### Clone & Build

Clone the repository:

```bash
git clone https://github.com/MPolunkin/bmp2ascii.git
cd bmp2ascii
```

Build the executable JAR using the Maven wrapper:

```bash
# Linux / macOS
./mvnw clean package

# Windows
.\mvnw.cmd clean package
```

The executable JAR is generated at `target/bmp2ascii-1.0-SNAPSHOT.jar`.

### Run Tests

```bash
./mvnw test
```

---

## Usage

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar [COMMAND] [OPTIONS]
```

### Commands

#### `asciify` — Convert BMP to ASCII

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar asciify -i <input.bmp> -o <output.txt> [OPTIONS]
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
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar bmpify -i <input.txt> -o <output.bmp> [OPTIONS]
```

| Option | Description | Default |
|---|---|---|
| `-i, --input=<file>` | Input ASCII art `.txt` file *(required)* | — |
| `-o, --output=<file>` | Output `.bmp` file *(required)* | — |
| `-I, --input-encoding=<enc>` | Character encoding for input | `UTF-8` |
| `-s, --scale=<factor>` | Pixel scale factor per character | `1` |

---

## Examples & Output

Sample files are available in the [`examples/`](examples) directory.

### Example 1: Convert BMP to ASCII art (`asciify`)

Convert the sample ice cream BMP into terminal-friendly ASCII art by downscaling to 60 columns:

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar asciify -i examples/icecream.bmp -o output.txt -w 60
```

Generated `output.txt`:

```text
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
________________________________+  `________________________
___________________________I  u]]+d@ _______________________
_________________________`@____=__=+= ______________________
_______________________ &1?k%O_m___f{,"=____________________
_______________________$]__/___1__f>/{$z'___________________
______________________ |={X]____+[q@]+>+.___________________
______________________ {-___________v+>% ___________________
_______________________J(]{+l"^^^^^^^,@ ____________________
________________________ ?<><<<<<<<<>8______________________
_________________________O[<i<<<<i<<l ______________________
_________________________ (<!<<<<i<lv_______________________
____________________________<>#k#<<r._______________________
__________________________^)<<<<<<,l________________________
___________________________b]<<<>>#;________________________
___________________________ }<<1>> _________________________
____________________________1[<>-(~_________________________
____________________________,v<?-~__________________________
_____________________________`_<b;__________________________
_____________________________~(" ___________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
____________________________________________________________
```

*(For smaller icons like `examples/smiley.bmp`, omit `-w` to preserve original 1:1 pixel dimensions).*

### Example 2: Convert ASCII art to BMP (`bmpify`)

Convert an ASCII art text file into a 24-bit uncompressed BMP image, scaling up each character for visibility:

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar bmpify -i examples/sample_art.txt -o output.bmp --scale 4
```

This generates an uncompressed 24-bit BMP image scaled 4x from the ASCII art character grid. You can also re-encode generated ASCII art back to BMP:

```bash
java -jar target/bmp2ascii-1.0-SNAPSHOT.jar bmpify -i output.txt -o icecream.bmp --scale 2
```

---

## Dependencies

- **[picocli](https://picocli.info/)** (`4.7.7`): CLI parsing, subcommands routing, and help generation.
- **[JUnit Jupiter](https://junit.org/)** (`6.1.3`): Automated unit testing.

*Note: All binary BMP parsing/writing and text stream processing are implemented purely with standard Java I/O (`InputStream`, `OutputStream`, `Reader`, `Writer`) without external image libraries, in accordance with course requirements.*

---

## Commit Guidelines

All contributions adhere to:
1. **[Conventional Commits](https://www.conventionalcommits.org/)**: e.g., `feat: ...`, `fix: ...`, `docs: ...`, `test: ...`, `refactor: ...`.
2. **Signed Commits**: All commits are cryptographically signed using GPG or SSH (`git commit -S`).

---

## License

This project is licensed under the [MIT License](LICENSE).
