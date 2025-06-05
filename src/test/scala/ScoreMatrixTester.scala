package geneweaver

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScoreMatrixTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ScoreMatrix"

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
    dataWidth: Int = 8,
    enableTraceback: Boolean = false
  ): Unit = {
    val N = seqA_bin.length
    val seqA_uint = seqA_bin.map(s => s.U)
    val seqB_uint = seqB_bin.map(s => s.U)
    val seqA_int = seqA_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val seqB_int = seqB_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val expected = needlemanWunschScore(seqA_int, seqB_int, matchScore, mismatchScore, gapScore)

    it should s"correctly align sequences for $label" in {
      test(new ScoreMatrix(N, dataWidth, matchScore, mismatchScore, gapScore, enableTraceback)) { c =>
        for (i <- 0 until N) {
          c.io.seqA.get(i).poke(seqA_uint(i))
          c.io.seqB.get(i).poke(seqB_uint(i))
        }

        c.clock.step(N + 1)

        val out = c.io.outScore.peek().litValue.toInt
        println(s"[$label] Alignment Score = $out (Expected = $expected)")
        c.io.outScore.expect(expected.S)

        if (enableTraceback) {
          val traceback = c.io.traceback.get
          println(s"[$label] Traceback Path (row-major):")
          for (i <- 0 until N) {
            val row = (0 until N).map(j => traceback(i * N + j).peek().litValue.toInt)
            println(s"  Row $i: ${row.mkString(" ")}")
          }
        }
      }
    }
  }

  // MEMORY_IO Tests
  runTest(
    Seq.fill(4)("b00"), Seq.fill(4)("b00"),
    "Perfect Match", enableTraceback = true
  )

  runTest(
    Seq("b00", "b01", "b10", "b11"),
    Seq("b11", "b10", "b01", "b00"),
    "Complete Mismatch", enableTraceback = true
  )

  runTest(Seq("b10"), Seq("b10"), "Single Match", enableTraceback = true)
  runTest(Seq("b10"), Seq("b01"), "Single Mismatch", enableTraceback = true)

  runTest(
    Seq("b00", "b01", "b10", "b11"),
    Seq("b00", "b00", "b00", "b00"),
    "Gap-Heavy Alignment", enableTraceback = true
  )

  runTest(
    Seq("b00", "b10", "b01", "b11", "b00", "b01", "b10", "b11"),
    Seq("b00", "b01", "b10", "b11", "b11", "b00", "b10", "b01"),
    "Custom Long Sequence", enableTraceback = true
  )

  // STREAMING_IO Test
  it should "correctly align sequences with streaming input" in {
    val seqA_bin = Seq("b00", "b01", "b10", "b11") // A C G T
    val seqB_bin = Seq("b00", "b00", "b00", "b00") // A A A A
    val N = seqA_bin.length
    val seqA_int = seqA_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val seqB_int = seqB_bin.map(s => Integer.parseInt(s.drop(1), 2))
    val expected = needlemanWunschScore(seqA_int, seqB_int, 2, -1, -2)

    test(new ScoreMatrix(N, dataWidth = 8, matchScore = 2, mismatchScore = -1, gapScore = -2, enableTraceback = false, useStreamingIO = true)) { c =>
      for (i <- 0 until N) {
        c.io.validIn.get.poke(true.B)
        c.io.charAIn.get.poke(seqA_bin(i).U)
        c.io.charBIn.get.poke(seqB_bin(i).U)
        c.clock.step(1)
      }

      // Finish input
      c.io.validIn.get.poke(false.B)
      c.clock.step(N + 1)

      val out = c.io.outScore.peek().litValue.toInt
      println(s"[STREAMING] Alignment Score = $out (Expected = $expected)")
      c.io.outScore.expect(expected.S)
    }
  }
}
