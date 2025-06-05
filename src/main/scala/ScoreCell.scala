package geneweaver

import chisel3._
import chisel3.util._

class ScoreCell(val dataWidth: Int = 8) extends Module {
  val io = IO(new Bundle {
    val diag     = Input(SInt(dataWidth.W))
    val up       = Input(SInt(dataWidth.W))
    val left     = Input(SInt(dataWidth.W))
    val charA    = Input(UInt(2.W)) // A,C,G,T → 0,1,2,3
    val charB    = Input(UInt(2.W))
    val matchScore = Input(SInt(dataWidth.W))
    val mismatchScore = Input(SInt(dataWidth.W))
    val gapScore = Input(SInt(dataWidth.W))

    val outScore = Output(SInt(dataWidth.W))
    val tracebackDir = Output(UInt(2.W)) // 0: diag, 1: up, 2: left
  })

  // Diagonal move
  val isMatch = io.charA === io.charB
  val diagScore = io.diag + Mux(isMatch, io.matchScore, io.mismatchScore)

  // Insertion and deletion
  val upScore = io.up + io.gapScore
  val leftScore = io.left + io.gapScore

  // Compute max score
  val scores = Seq(diagScore, upScore, leftScore)
  val maxScore = scores.reduce((a, b) => Mux(a > b, a, b))
  io.outScore := maxScore

  // Direction encoding: 0 = diag, 1 = up, 2 = left
  val dir = Wire(UInt(2.W))
  when(maxScore === diagScore) {
    dir := 0.U
  }.elsewhen(maxScore === upScore) {
    dir := 1.U
  }.otherwise {
    dir := 2.U
  }
  io.tracebackDir := dir
}
