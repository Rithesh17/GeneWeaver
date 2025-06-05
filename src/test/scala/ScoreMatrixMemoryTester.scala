package geneweaver

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScoreMatrixMemoryTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ScoreMatrix (Memory IO)"

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

  def runTest(label: String, seqA_bin: Seq[String], seqB_bin: Seq[String]): Unit = {
    val N = seqA_bin.length
    val matchScore = 2
    val mismatchScore = -1
    val gapScore = -2
    val seqA_uint = seqA_bin.map(s => s.U)
    val seqB_uint = seqB_bin.map(s => s.U)
    val seqA_int = seqA_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val seqB_int = seqB_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val expected = needlemanWunschScore(seqA_int, seqB_int, matchScore, mismatchScore, gapScore)

    it should s"correctly align $label using memory-mapped IO" in {
      test(new ScoreMatrix(N, enableTraceback = true)) { c =>
        for (i <- 0 until N) {
          c.io.seqA.get(i).poke(seqA_uint(i))
          c.io.seqB.get(i).poke(seqB_uint(i))
        }
        c.clock.step(N + 1)
        val score = c.io.outScore.peek().litValue.toInt
        println(s"[$label | Memory] Alignment Score = $score (Expected = $expected)")
        c.io.outScore.expect(expected.S)

        val traceback = c.io.traceback.get
        println(s"[$label] Traceback (row-major):")
        for (i <- 0 until N) {
          val row = (0 until N).map(j => traceback(i * N + j).peek().litValue.toInt)
          println(s"  Row $i: ${row.mkString(" ")}")
        }
      }
    }
  }

  runTest("Perfect Match", Seq.fill(4)("b00"), Seq.fill(4)("b00"))
  runTest("Complete Mismatch", Seq("b00", "b01", "b10", "b11"), Seq("b11", "b10", "b01", "b00"))
  runTest("Single Match", Seq("b10"), Seq("b10"))
  runTest("Single Mismatch", Seq("b10"), Seq("b01"))
  runTest("Gap Heavy A", Seq("b00", "b01", "b10", "b11"), Seq.fill(4)("b00"))
  runTest("Gap Heavy B", Seq.fill(4)("b00"), Seq("b00", "b01", "b10", "b11"))
  runTest("Short Sequence", Seq("b00", "b01"), Seq("b00", "b01"))
  runTest("Long Match", Seq("b00", "b01", "b10", "b11", "b00", "b01", "b10", "b11"), Seq("b00", "b01", "b10", "b11", "b00", "b01", "b10", "b11"))
  runTest("Long Mismatch", Seq("b00", "b01", "b10", "b11", "b00", "b01", "b10", "b11"), Seq("b11", "b10", "b01", "b00", "b11", "b10", "b01", "b00"))
  runTest("Zigzag Alignment", Seq("b00", "b10", "b01", "b11", "b00", "b01", "b10", "b11"), Seq("b00", "b01", "b10", "b11", "b11", "b00", "b10", "b01"))
}
