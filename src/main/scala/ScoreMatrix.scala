package geneweaver

import chisel3._
import chisel3.util._

class ScoreMatrix extends Module {
  val io = IO(new Bundle {
    val outScore = Output(SInt(8.W)) // Final: bottom-right cell
  })

  // Hardcoded sequences of length 8 (AGCTACGT)
  val seqA = VecInit("b00".U, "b10".U, "b01".U, "b11".U, "b00".U, "b01".U, "b10".U, "b11".U) // A G C T A C G T
  val seqB = VecInit("b00".U, "b01".U, "b10".U, "b11".U, "b11".U, "b00".U, "b10".U, "b01".U) // A C G T T A G C

  val matchScore = 2.S
  val mismatchScore = (-1).S
  val gapScore = (-2).S

  val matrix = Seq.fill(8, 8)(Module(new ScoreCell(8)))

  // Initializing top-left corner
  matrix(0)(0).io.diag := 0.S
  matrix(0)(0).io.up := gapScore
  matrix(0)(0).io.left := gapScore
  matrix(0)(0).io.charA := seqA(0)
  matrix(0)(0).io.charB := seqB(0)
  matrix(0)(0).io.matchScore := matchScore
  matrix(0)(0).io.mismatchScore := mismatchScore
  matrix(0)(0).io.gapScore := gapScore

  // Filling top row and left column
  for (j <- 1 until 8) {
    val cell = matrix(0)(j)
    cell.io.diag := gapScore * j.S
    cell.io.up := gapScore * j.S
    cell.io.left := matrix(0)(j - 1).io.outScore
    cell.io.charA := seqA(j)
    cell.io.charB := seqB(0)
    cell.io.matchScore := matchScore
    cell.io.mismatchScore := mismatchScore
    cell.io.gapScore := gapScore
  }

  for (i <- 1 until 8) {
    val cell = matrix(i)(0)
    cell.io.diag := gapScore * i.S
    cell.io.up := matrix(i - 1)(0).io.outScore
    cell.io.left := gapScore * i.S
    cell.io.charA := seqA(0)
    cell.io.charB := seqB(i)
    cell.io.matchScore := matchScore
    cell.io.mismatchScore := mismatchScore
    cell.io.gapScore := gapScore
  }

  // Filling the rest
  for (i <- 1 until 8) {
    for (j <- 1 until 8) {
      val cell = matrix(i)(j)
      cell.io.diag := matrix(i - 1)(j - 1).io.outScore
      cell.io.up := matrix(i - 1)(j).io.outScore
      cell.io.left := matrix(i)(j - 1).io.outScore
      cell.io.charA := seqA(j)
      cell.io.charB := seqB(i)
      cell.io.matchScore := matchScore
      cell.io.mismatchScore := mismatchScore
      cell.io.gapScore := gapScore
    }
  }

  // Output: bottom-right cell score
  io.outScore := matrix(7)(7).io.outScore
}
