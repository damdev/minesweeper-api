package com.github.damdev.minesweeper.minesweeperapi.services

import cats.effect.Effect
import cats.effect._
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.errors._
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardParser, Game}

private class GameService[F[_]: Effect] extends GameAlg[F] {

  val BOARD: Board = BoardParser.parse(
    """|____X
       |___X_
       |__X__
       |_____""".stripMargin)

  val gamesById: scala.collection.mutable.Map[String, Game] =
    (scala.collection.mutable.Map.newBuilder += ("0" -> Game(board = BOARD))).result()

  override def get(id: String): F[Either[MinesweeperHttpError, Game]] =
    gamesById.get(id).map(_.asRight[MinesweeperHttpError]).getOrElse(GameNotFoundError(id).asLeft[Game]).pure[F]


  override def reveal(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    get(id).map(_.map({ g =>
      val revealed = g.reveal(x, y)
      gamesById.update(id, revealed)
      revealed
    }))
  }

  override def flag(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    get(id).map(_.map({ g =>
      val flagged = g.flag(x, y)
      gamesById.update(id, flagged)
      flagged
    }))
  }

}

object GameAlg {
  def impl[F[_]: Effect]: GameAlg[F] = new GameService[F]()
}

trait GameAlg[F[_]] {
  def get(id: String): F[Either[MinesweeperHttpError, Game]]

  def flag(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]]

  def reveal(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]]
}
