package geneweaver

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScoreMatrixTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ScoreMatrix"

  it should "compute correct alignment score for hardcoded sequences" in {
    test(new ScoreMatrix) { dut =>
      // Since everything is combinational, we can just evaluate
      dut.clock.step(1)

      // Expected score based on Needleman-Wunsch with:
      // match = +2, mismatch = -1, gap = -2
      // Sequences: A G C T A C G T and A C G T T A G C
      
      val expectedScore = 4

      dut.io.outScore.expect(expectedScore.S)
    }
  }
}
