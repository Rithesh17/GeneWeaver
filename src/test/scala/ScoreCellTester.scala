package geneweaver

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ScoreCellTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "ScoreCell"

  val matchScore = 2
  val mismatchScore = -1
  val gapScore = -2

  it should "compute correct score for a match (A vs A)" in {
    test(new ScoreCell(8)) { dut =>
      dut.io.diag.poke(4.S)
      dut.io.up.poke(2.S)
      dut.io.left.poke(3.S)
      dut.io.charA.poke(0.U) // A
      dut.io.charB.poke(0.U) // A
      dut.io.matchScore.poke(matchScore.S)
      dut.io.mismatchScore.poke(mismatchScore.S)
      dut.io.gapScore.poke(gapScore.S)
      dut.clock.step()
      dut.io.outScore.expect(6.S) // 4 + 2 (match)
    }
  }

  it should "compute correct score for a mismatch (A vs G)" in {
    test(new ScoreCell(8)) { dut =>
      dut.io.diag.poke(4.S)
      dut.io.up.poke(2.S)
      dut.io.left.poke(3.S)
      dut.io.charA.poke(0.U) // A
      dut.io.charB.poke(1.U) // G
      dut.io.matchScore.poke(matchScore.S)
      dut.io.mismatchScore.poke(mismatchScore.S)
      dut.io.gapScore.poke(gapScore.S)
      dut.clock.step()
      dut.io.outScore.expect(3.S) // 4 - 1 (mismatch)
    }
  }

  it should "choose max of gap paths (gap better than mismatch)" in {
    test(new ScoreCell(8)) { dut =>
      dut.io.diag.poke(1.S)
      dut.io.up.poke(6.S)
      dut.io.left.poke(5.S)
      dut.io.charA.poke(0.U)
      dut.io.charB.poke(1.U)
      dut.io.matchScore.poke(matchScore.S)
      dut.io.mismatchScore.poke(mismatchScore.S)
      dut.io.gapScore.poke(gapScore.S)
      dut.clock.step()
      dut.io.outScore.expect(4.S) // up (6 - 2)
    }
  }
}
