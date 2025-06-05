package geneweaver

import chisel3._
import chisel3.util._

class ScoreMatrix(
  val N: Int,
  val dataWidth: Int = 8,
  val matchScore: Int = 2,
  val mismatchScore: Int = -1,
  val gapScore: Int = -2,
  val enableTraceback: Boolean = false,
  val useStreamingIO: Boolean = false
) extends Module {
  val io = IO(new Bundle {
    // For MEMORY_IO mode
    val seqA = if (!useStreamingIO) Some(Input(Vec(N, UInt(2.W)))) else None
    val seqB = if (!useStreamingIO) Some(Input(Vec(N, UInt(2.W)))) else None

    // For STREAMING_IO mode
    val validIn = if (useStreamingIO) Some(Input(Bool())) else None
    val charAIn = if (useStreamingIO) Some(Input(UInt(2.W))) else None
    val charBIn = if (useStreamingIO) Some(Input(UInt(2.W))) else None

    val outScore = Output(SInt(dataWidth.W))
    val traceback = if (enableTraceback) Some(Output(Vec(N * N, UInt(2.W)))) else None
  })

  val matchScoreS = matchScore.S(dataWidth.W)
  val mismatchScoreS = mismatchScore.S(dataWidth.W)
  val gapScoreS = gapScore.S(dataWidth.W)

  // Register buffers for sequences
  val seqAReg = Reg(Vec(N, UInt(2.W)))
  val seqBReg = Reg(Vec(N, UInt(2.W)))
  val loadCount = RegInit(0.U(log2Ceil(N + 1).W))
  val loaded = Wire(Bool())

  if (useStreamingIO) {
    loaded := loadCount === N.U
    when(io.validIn.get && !loaded) {
      seqAReg(loadCount) := io.charAIn.get
      seqBReg(loadCount) := io.charBIn.get
      loadCount := loadCount + 1.U
    }
  } else {
    for (i <- 0 until N) {
      seqAReg(i) := io.seqA.get(i)
      seqBReg(i) := io.seqB.get(i)
    }
    loaded := true.B
  }

  val matrix = Seq.fill(N, N)(Module(new ScoreCell(dataWidth)))
  val tracebackMatrix = Wire(Vec(N * N, UInt(2.W)))

  for (i <- 0 until N; j <- 0 until N) {
    val cell = matrix(i)(j)
    val idx = i * N + j

    if (i == 0 && j == 0) {
      cell.io.diag := 0.S
      cell.io.up := gapScoreS
      cell.io.left := gapScoreS
      cell.io.charA := seqAReg(0)
      cell.io.charB := seqBReg(0)
    } else if (i == 0) {
      cell.io.diag := gapScoreS * j.S
      cell.io.up := gapScoreS * j.S
      cell.io.left := matrix(i)(j - 1).io.outScore
      cell.io.charA := seqAReg(j)
      cell.io.charB := seqBReg(0)
    } else if (j == 0) {
      cell.io.diag := gapScoreS * i.S
      cell.io.up := matrix(i - 1)(j).io.outScore
      cell.io.left := gapScoreS * i.S
      cell.io.charA := seqAReg(0)
      cell.io.charB := seqBReg(i)
    } else {
      cell.io.diag := matrix(i - 1)(j - 1).io.outScore
      cell.io.up := matrix(i - 1)(j).io.outScore
      cell.io.left := matrix(i)(j - 1).io.outScore
      cell.io.charA := seqAReg(j)
      cell.io.charB := seqBReg(i)
    }

    cell.io.matchScore := matchScoreS
    cell.io.mismatchScore := mismatchScoreS
    cell.io.gapScore := gapScoreS

    tracebackMatrix(idx) := cell.io.tracebackDir
  }

  // Output score only when sequences are loaded
  io.outScore := Mux(loaded, matrix(N - 1)(N - 1).io.outScore, 0.S)

  if (enableTraceback) {
    io.traceback.get := tracebackMatrix
  }
}
