# GeneWeaver: Parametric Matrix Module in Chisel

**GeneWeaver** is a hardware design project developed using Chisel (Constructing Hardware In a Scala Embedded Language). It implements a configurable scoring matrix for gene compatibility analysis using a modular design. The matrix is composed of reusable score cells that allow flexible experimentation in gene network modeling, computation, and visualization.

## Project Overview

This project simulates an 8×8 matrix of `ScoreCell` modules. Each `ScoreCell` performs a simple computation based on input gene compatibility scores and can be extended to support more complex biological computations. The matrix can be configured for different sizes and scoring behaviors with minor modifications.

Key features:
- Modular design using `ScoreCell` as the fundamental computation unit
- Parametric scalability via `ScoreMatrix`
- Test suite to verify correct behavior of the matrix structure

---

## Project Structure

```
GeneWeaver/
├── build.sbt
├── src/
│   ├── main/
│   │   └── scala/
│   │       ├── ScoreCell.scala         # Defines a basic scoring cell
│   │       └── ScoreMatrix8x8.scala    # Instantiates an 8×8 matrix of ScoreCells
│   └── test/
│       └── scala/
│           └── ScoreMatrixTester.scala # Unit tests for ScoreMatrix8x8
```

---

## How to Run

### Prerequisites

Make sure you have the following installed:

- Java (JDK 8 or higher)
- [sbt (Scala Build Tool)](https://www.scala-sbt.org/)
- Chisel (via sbt dependencies)

### Build & Run Tests

To compile the project and run unit tests:

```bash
sbt test
```

You should see the test output verifying the behavior of `ScoreMatrix8x8`.

---

## Modules

### `ScoreCell.scala`
A basic cell that computes a gene compatibility score based on input values. Can be extended to include more complex operations or states.

### `ScoreMatrix8x8.scala`
Constructs a matrix of interconnected `ScoreCell`s. Each row and column can be thought of as a gene input, and the resulting matrix represents interaction scores.

### `ScoreMatrixTester.scala`
Unit test suite validating matrix correctness using example inputs and expected outputs.
