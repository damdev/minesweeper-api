package com.github.damdev.minesweeper.minesweeperapi.model

import cats.data.State
import org.specs2.Specification
import cats.effect.IO
import com.github.damdev.minesweeper.minesweeperapi.errors.MinesweeperError
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class BoardSpec extends org.specs2.mutable.Specification {

  "Simple Board should" >> {
    val b: Board = BoardParser.parse(
      """|____X
         |___X_
         |__X__
         |_____""".stripMargin)

    def reveal(x: Int, y: Int): State[Either[MinesweeperError, Board], Either[MinesweeperError, RevealResult]] = State { b =>
      val res = b.flatMap(_.reveal(x, y))
      (res.map(_.board), res)
    }

    "win" >> {
      val playGame = for {
        _ <- reveal(1, 2)
        _ <- reveal(1, 0)
        _ <- reveal(0, 2)
        _ <- reveal(3, 0)
        _ <- reveal(4, 3)
        // _ <- reveal(4, 0) // to lose
        _ <- reveal(4, 2)
        _ <- reveal(4, 1)
        _ <- reveal(4, 3)
        _ <- reveal(3, 3)
        _ <- reveal(2, 3)
        r <- reveal(3, 2)
      } yield {
        r
      }
      val result = playGame.runA(Right(b)).value.fold(
        _.msg,
        rr => rr.boardStatus.toString
      )
      result should_=== "Win"
    }
    "lose" >> {
      val playGame = for {
        _ <- reveal(1, 2)
        _ <- reveal(1, 0)
        _ <- reveal(0, 2)
        _ <- reveal(3, 0)
        _ <- reveal(4, 3)
        r <- reveal(4, 0) // to lose
      } yield {
        r
      }
      val result = playGame.runA(Right(b)).value.fold(
        _.msg,
        rr => rr.boardStatus.toString
      )
      result should_=== "Lose"
    }
  }
}
