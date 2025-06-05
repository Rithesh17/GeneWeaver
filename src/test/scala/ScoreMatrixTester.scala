package geneweaver

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScoreMatrixTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ScoreMatrix"

  // Character encoding: A=00, C=01, G=10, T=11
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

  def runTest(
    seqA_bin: Seq[String],
    seqB_bin: Seq[String],
    label: String,
    matchScore: Int = 2,
    mismatchScore: Int = -1,
    gapScore: Int = -2,
    dataWidth: Int = 8
  ): Unit = {
    val N = seqA_bin.length
    val seqA_uint = seqA_bin.map(s => s.U)
    val seqB_uint = seqB_bin.map(s => s.U)
    val seqA_int = seqA_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val seqB_int = seqB_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val expected = needlemanWunschScore(seqA_int, seqB_int, matchScore, mismatchScore, gapScore)

    it should s"correctly align sequences for $label" in {
      test(new ScoreMatrix(N, dataWidth, matchScore, mismatchScore, gapScore)) { c =>
        for (i <- 0 until N) {
          c.io.seqA(i).poke(seqA_uint(i))
          c.io.seqB(i).poke(seqB_uint(i))
        }

        c.clock.step(N + 1)

        val out = c.io.outScore.peek().litValue.toInt
        println(s"[$label] Alignment Score = $out (Expected = $expected)")
        c.io.outScore.expect(expected.S)
      }
    }
  }

  // Test Cases
  runTest(
    Seq.fill(4)("b00"), // A A A A
    Seq.fill(4)("b00"), // A A A A
    "Perfect Match"
  )

  runTest(
    Seq("b00", "b01", "b10", "b11"), // A C G T
    Seq("b11", "b10", "b01", "b00"), // T G C A
    "Complete Mismatch"
  )

  runTest(
    Seq("b10"), // G
    Seq("b10"), // G
    "Single Match"
  )

  runTest(
    Seq("b10"), // G
    Seq("b01"), // C
    "Single Mismatch"
  )

  runTest(
    Seq("b00", "b01", "b10", "b11"), // A C G T
    Seq("b00", "b00", "b00", "b00"), // A A A A
    "Gap-Heavy Alignment"
  )

  runTest(
    Seq("b00", "b10", "b01", "b11", "b00", "b01", "b10", "b11"), // A G C T A C G T
    Seq("b00", "b01", "b10", "b11", "b11", "b00", "b10", "b01"), // A C G T T A G C
    "Custom Long Sequence"
  )
}
