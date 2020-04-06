package com.github.damdev.minesweeper.minesweeperapi.model

import cats.data.State
import org.specs2.Specification
import cats.effect.IO
import com.github.damdev.minesweeper.minesweeperapi.errors.{IndexOutOfBoardError, MinesweeperError, TooManyFlagsError}
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

    def flag(x: Int, y: Int): State[Either[MinesweeperError, Board], Either[MinesweeperError, Board]] = State { b =>
      val res = b.flatMap(_.flag(x, y))
      (res, res)
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
      playGame.runA(Right(b)).value.map(_.boardStatus) should_=== Right(BoardStatus.Win)

    }

    "recursive reveal" >> {
      val playGame = for {
        r <- reveal(0, 0)
      } yield {
        r
      }
      val result = playGame.runA(Right(b)).value.toOption.get

      val shouldBeRevealed = List(0 -> 0, 1 -> 0, 2 -> 0, 0 -> 1, 1 -> 1, 2 -> 1, 0 -> 2, 1 -> 2, 0 -> 3, 1 -> 3)
      val ps = result.board.positions

      ps.filter(p => shouldBeRevealed.contains(p._1)).forall(p => p._2.revealed) should_=== true
      ps.filter(p => !shouldBeRevealed.contains(p._1)).forall(p => !p._2.revealed) should_=== true

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
      playGame.runA(Right(b)).value.map(_.boardStatus) should_=== Right(BoardStatus.Lose)

    }
    "continue" >> {
      val playGame = for {
        _ <- reveal(1, 2)
        _ <- reveal(1, 0)
        _ <- reveal(0, 2)
        _ <- reveal(3, 0)
        r <- reveal(4, 3)
      } yield {
        r
      }
      playGame.runA(Right(b)).value.map(_.boardStatus) should_=== Right(BoardStatus.Continue)
    }

    "index out of board error" >> {
      val playGame = for {
        _ <- reveal(1, 2)
        _ <- reveal(1, 0)
        _ <- reveal(0, 2)
        _ <- reveal(10, 3)
        r <- reveal(3, 0)
      } yield {
        r
      }
      playGame.runA(Right(b)).value.map(_.boardStatus) should_=== Left(IndexOutOfBoardError(10, 3))
    }

    "too many flags error" >> {
      val playGame = for {
        _ <- flag(1, 2)
        _ <- flag(1, 0)
        _ <- flag(0, 2)
        r <- flag(3, 0)
      } yield {
        r
      }

      playGame.runA(Right(b)).value should_=== Left(TooManyFlagsError(3))
    }

    "ok flags" >> {
      val playGame = for {
        _ <- flag(1, 2)
        _ <- flag(1, 0)
        r <- flag(3, 0)
      } yield {
        r
      }
      playGame.runA(Right(b)).value.isRight should_== true
    }
  }
}
