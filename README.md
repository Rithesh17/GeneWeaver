# GeneWeaver: Parametric DNA Sequence Alignment Module in Chisel

**GeneWeaver** is a hardware design project developed in Chisel (Constructing Hardware in a Scala Embedded Language). It implements a fully parametric, hardware-accelerated Needleman-Wunsch algorithm for DNA sequence alignment, designed for flexibility, efficiency, and clarity.

## Overview

This project simulates a matrix of `ScoreCell` modules that align two DNA sequences using customizable match/mismatch/gap scoring. It supports optional traceback for alignment reconstruction and offers two modes of operation: memory-mapped and streaming.

### Key Features

- Modular hierarchy: `ScoreCell`, `ScoreMatrix`, and testbenches
- Parametric matrix size and scoring values
- Optional traceback reconstruction
- Streaming or memory-mapped I/O interface
- Verilog generation with `ScoreMatrixGenerator.scala`

---

## Project Structure

```
GeneWeaver/
├── build.sbt
├── src/
│   ├── main/
│   │   └── scala/
│   │       ├── ScoreCell.scala              # Basic scoring unit for DNA pairwise comparison
│   │       ├── ScoreMatrix.scala            # Configurable matrix of ScoreCells
│   │       └── ScoreMatrixGenerator.scala   # Generator script for Verilog
│   └── test/
│       └── scala/
│           ├── ScoreCellTester.scala        # Tests for ScoreCell logic
│           ├── ScoreMatrixMemoryTester.scala # Tests matrix with memory-mapped I/O
│           └── ScoreMatrixStreamTester.scala # Tests matrix with streaming I/O
```

---

## How to Run

### Prerequisites

- Java (JDK 8+)
- [sbt (Scala Build Tool)](https://www.scala-sbt.org/)
- Internet (for fetching Chisel dependencies)

### Run Tests

```bash
sbt test
```

### Generate Verilog

```bash
sbt "runMain ScoreMatrixGenerator"
```

This generates the Verilog output for a configurable instance of the `ScoreMatrix` module.

---

## Module Descriptions

### `ScoreCell.scala`
A single comparison unit computing alignment scores between DNA bases. Accepts three directions (diag, up, left) and outputs the best score and traceback direction.

### `ScoreMatrix.scala`
Constructs a parametric matrix of `ScoreCell`s, processes two input sequences, and optionally reconstructs alignment via traceback.

### `ScoreMatrixGenerator.scala`
Generates Verilog from a custom configuration of the ScoreMatrix.

### `ScoreCellTester.scala`
Unit tests for standalone `ScoreCell` behavior.

### `ScoreMatrixMemoryTester.scala` and `ScoreMatrixStreamTester.scala`
Test the matrix behavior using two different I/O styles — memory-mapped and streaming.

---

## License

This project is for academic use and learning purposes.