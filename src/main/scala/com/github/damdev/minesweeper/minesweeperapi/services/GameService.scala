package com.github.damdev.minesweeper.minesweeperapi.services

import cats.effect.Effect
import cats.effect._
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.errors._
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardParser, Game}

class GameService[F[_]: Effect] extends GameAlg[F] {

  val BOARD: Board = BoardParser.parse(
  """|____X
     |___X_
     |__X__
     |_____""".stripMargin)

  val gamesById: scala.collection.mutable.Map[String, Game] =
    (scala.collection.mutable.Map.newBuilder += ("0" -> Game(board = BOARD))).result()

  def get(id: String): F[Either[MinesweeperHttpError, Game]] = Effect.apply.point(
    gamesById.get(id).map(_.asRight[MinesweeperHttpError]).getOrElse(GameNotFoundError(id).asLeft[Game])
  )

  def reveal(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    get(id).map(_.map({ g =>
      val revealed = g.reveal(x, y)
      gamesById.update(id, revealed)
      revealed
    }))
  }

}

object GameService {
  def impl[F[_]: Effect] = new GameService[F]()
}

trait GameAlg[F[_]] {
  def get(id: String): F[Either[MinesweeperHttpError, Game]]
}
