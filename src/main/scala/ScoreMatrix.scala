package geneweaver

import chisel3._
import chisel3.util._

class ScoreMatrix(
  val N: Int,
  val dataWidth: Int = 8,
  val matchScore: Int = 2,
  val mismatchScore: Int = -1,
  val gapScore: Int = -2
) extends Module {
  val io = IO(new Bundle {
    val seqA = Input(Vec(N, UInt(2.W)))
    val seqB = Input(Vec(N, UInt(2.W)))
    val outScore = Output(SInt(dataWidth.W)) // Final: bottom-right cell
  })

  val matchScoreS = matchScore.S(dataWidth.W)
  val mismatchScoreS = mismatchScore.S(dataWidth.W)
  val gapScoreS = gapScore.S(dataWidth.W)

  val matrix = Seq.fill(N, N)(Module(new ScoreCell(dataWidth)))

  // Top-left corner initialization
  matrix(0)(0).io.diag := 0.S
  matrix(0)(0).io.up := gapScoreS
  matrix(0)(0).io.left := gapScoreS
  matrix(0)(0).io.charA := io.seqA(0)
  matrix(0)(0).io.charB := io.seqB(0)
  matrix(0)(0).io.matchScore := matchScoreS
  matrix(0)(0).io.mismatchScore := mismatchScoreS
  matrix(0)(0).io.gapScore := gapScoreS

  // Top row
  for (j <- 1 until N) {
    val cell = matrix(0)(j)
    cell.io.diag := gapScoreS * j.S
    cell.io.up := gapScoreS * j.S
    cell.io.left := matrix(0)(j - 1).io.outScore
    cell.io.charA := io.seqA(j)
    cell.io.charB := io.seqB(0)
    cell.io.matchScore := matchScoreS
    cell.io.mismatchScore := mismatchScoreS
    cell.io.gapScore := gapScoreS
  }

  // Left column
  for (i <- 1 until N) {
    val cell = matrix(i)(0)
    cell.io.diag := gapScoreS * i.S
    cell.io.up := matrix(i - 1)(0).io.outScore
    cell.io.left := gapScoreS * i.S
    cell.io.charA := io.seqA(0)
    cell.io.charB := io.seqB(i)
    cell.io.matchScore := matchScoreS
    cell.io.mismatchScore := mismatchScoreS
    cell.io.gapScore := gapScoreS
  }

  // Fill rest of matrix
  for (i <- 1 until N) {
    for (j <- 1 until N) {
      val cell = matrix(i)(j)
      cell.io.diag := matrix(i - 1)(j - 1).io.outScore
      cell.io.up := matrix(i - 1)(j).io.outScore
      cell.io.left := matrix(i)(j - 1).io.outScore
      cell.io.charA := io.seqA(j)
      cell.io.charB := io.seqB(i)
      cell.io.matchScore := matchScoreS
      cell.io.mismatchScore := mismatchScoreS
      cell.io.gapScore := gapScoreS
    }
  }

  io.outScore := matrix(N - 1)(N - 1).io.outScore
}
