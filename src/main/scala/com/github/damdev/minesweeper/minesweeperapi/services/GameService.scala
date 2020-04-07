package com.github.damdev.minesweeper.minesweeperapi.services

import cats.effect.Effect
import cats.effect._
import cats.syntax._
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.errors._
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardParser, Game}
import com.github.damdev.minesweeper.minesweeperapi.repository.GameRepository

private class GameService[F[_]: Effect](gameRepository: GameRepository[F]) extends GameAlg[F] {

  override def get(id: String): F[Either[MinesweeperHttpError, Game]] =
    gameRepository.get(id).map(_.fold(GameNotFoundError(id).asLeft[Game])(_.asRight[GameNotFoundError]))

  override def reveal(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    get(id).map(_.map({ g =>
      val revealed = g.reveal(x, y)
      gameRepository.upsert(revealed)
      revealed
    }))
  }

  override def flag(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    get(id).map(_.map({ g =>
      val flagged = g.flag(x, y)
      gameRepository.upsert(flagged)
      flagged
    }))
  }

}

object GameAlg {
  def impl[F[_]: Effect](gameRepository: GameRepository[F]): GameAlg[F] = new GameService[F](gameRepository)
}

trait GameAlg[F[_]] {
  def get(id: String): F[Either[MinesweeperHttpError, Game]]

  def flag(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]]

  def reveal(id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]]
}
