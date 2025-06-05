package geneweaver

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScoreMatrixStreamingTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ScoreMatrix (Streaming IO)"

  def needlemanWunschScore(seqA: Seq[Int], seqB: Seq[Int], matchScore: Int, mismatchScore: Int, gapScore: Int): Int = {
    val N = seqA.length
    val dp = Array.ofDim[Int](N + 1, N + 1)
    for (i <- 0 to N) {
      dp(i)(0) = i * gapScore
      dp(0)(i) = i * gapScore
    }
    for (i <- 1 to N; j <- 1 to N) {
      val matchMismatch = if (seqA(j - 1) == seqB(i - 1)) matchScore else mismatchScore
      dp(i)(j) = Seq(
        dp(i - 1)(j - 1) + matchMismatch,
        dp(i - 1)(j) + gapScore,
        dp(i)(j - 1) + gapScore
      ).max
    }
    dp(N)(N)
  }

  def runStreamingTest(label: String, seqA_bin: Seq[String], seqB_bin: Seq[String]): Unit = {
    val N = seqA_bin.length
    val matchScore = 2
    val mismatchScore = -1
    val gapScore = -2
    val seqA_int = seqA_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val seqB_int = seqB_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val expected = needlemanWunschScore(seqA_int, seqB_int, matchScore, mismatchScore, gapScore)

    it should s"correctly align $label using streaming IO" in {
      test(new ScoreMatrix(N, matchScore = matchScore, mismatchScore = mismatchScore, gapScore = gapScore, useStreamingIO = true)) { c =>
        for (i <- 0 until N) {
          c.io.validIn.get.poke(true.B)
          c.io.charAIn.get.poke(seqA_bin(i).U)
          c.io.charBIn.get.poke(seqB_bin(i).U)
          c.clock.step(1)
        }
        c.io.validIn.get.poke(false.B)
        c.clock.step(N + 1)
        val score = c.io.outScore.peek().litValue.toInt
        println(s"[$label | Streaming] Alignment Score = $score (Expected = $expected)")
        c.io.outScore.expect(expected.S)
      }
    }
  }

  runStreamingTest("Perfect Match", Seq.fill(4)("b00"), Seq.fill(4)("b00"))
  runStreamingTest("Complete Mismatch", Seq("b00", "b01", "b10", "b11"), Seq("b11", "b10", "b01", "b00"))
  runStreamingTest("Single Match", Seq("b10"), Seq("b10"))
  runStreamingTest("Single Mismatch", Seq("b10"), Seq("b01"))
  runStreamingTest("Gap Heavy A", Seq("b00", "b01", "b10", "b11"), Seq.fill(4)("b00"))
  runStreamingTest("Gap Heavy B", Seq.fill(4)("b00"), Seq("b00", "b01", "b10", "b11"))
  runStreamingTest("Short Sequence", Seq("b00", "b01"), Seq("b00", "b01"))
  runStreamingTest("Long Match", Seq("b00", "b01", "b10", "b11", "b00", "b01", "b10", "b11"), Seq("b00", "b01", "b10", "b11", "b00", "b01", "b10", "b11"))
  runStreamingTest("Long Mismatch", Seq("b00", "b01", "b10", "b11", "b00", "b01", "b10", "b11"), Seq("b11", "b10", "b01", "b00", "b11", "b10", "b01", "b00"))
  runStreamingTest("Zigzag Alignment", Seq("b00", "b10", "b01", "b11", "b00", "b01", "b10", "b11"), Seq("b00", "b01", "b10", "b11", "b11", "b00", "b10", "b01"))
}
