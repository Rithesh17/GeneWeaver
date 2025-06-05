package geneweaver

import chisel3._
import chisel3.stage.ChiselStage

object ScoreMatrixGenerator extends App {
  // Default parameters
  val N = 8
  val dataWidth = 8
  val matchScore = 2
  val mismatchScore = -1
  val gapScore = -2
  val enableTraceback = true
  val useStreamingIO = false

  // Generate the ScoreMatrix hardware
  (new ChiselStage).emitVerilog(
    new ScoreMatrix(
      N = N,
      dataWidth = dataWidth,
      matchScore = matchScore,
      mismatchScore = mismatchScore,
      gapScore = gapScore,
      enableTraceback = enableTraceback,
      useStreamingIO = useStreamingIO
    ),
    Array("--target-dir", "generated")
  )
}