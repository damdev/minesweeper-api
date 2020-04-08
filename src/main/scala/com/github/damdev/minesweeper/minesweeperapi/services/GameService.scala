package com.github.damdev.minesweeper.minesweeperapi.services

import java.util.UUID

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.errors._
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, FlagType, Game, Position}
import com.github.damdev.minesweeper.minesweeperapi.repository.GameRepository
import com.github.damdev.minesweeper.minesweeperapi.utils.Config.DefaultGameConfig
import com.github.damdev.minesweeper.minesweeperapi.utils.User

import scala.util.Random

private class GameService[F[_]: Effect](gameRepository: GameRepository[F], config: DefaultGameConfig) extends GameAlg[F] {


  private def withGame(id: String, user: User)(f: Game => F[Game]): F[Either[MinesweeperHttpError, Game]] = (for {
    g <- EitherT(gameRepository.get(id).map(_.fold(GameNotFoundError(id).asLeft[Game])(_.asRight[GameNotFoundError])))
    a <- EitherT(if(g.owner != user.username) (NotYourGameError(id): MinesweeperHttpError).asLeft[Game].pure[F] else f(g).map(_.asRight[MinesweeperHttpError]))
  } yield a).value

  override def get(user: User, id: String): F[Either[MinesweeperHttpError, Game]] = withGame(id, user)(a => a.pure[F])

  override def reveal(user: User, id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    withGame(id, user) { g: Game =>
      val revealed = g.reveal(x, y)
      gameRepository.upsert(revealed)
    }
  }

  override def flag(user: User, id: String, x: Int, y: Int, flagType: FlagType): F[Either[MinesweeperHttpError, Game]] = {
    withGame(id, user) { g: Game =>
      val flagged = g.flag(x, y, flagType)
      gameRepository.upsert(flagged)
    }
  }

  override def unflag(user: User, id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]] = {
    withGame(id, user) { g: Game =>
      val flagged = g.unflag(x, y)
      gameRepository.upsert(flagged)
    }
  }

  override def generateGame(user: User, mines: Option[Int], width: Option[Int], height: Option[Int]): F[Either[MinesweeperHttpError, Game]] =
    GameGenerator.generate(user,
      mines.getOrElse(config.mines),
      width.getOrElse(config.width),
      height.getOrElse(config.height)).traverse(gameRepository.upsert)

}

object GameGenerator {

  def pickMine(alreadyPicked: Set[(Int, Int)])(width: Int, height: Int): (Int, Int) = {
    val p = Random.between(0, width) -> Random.between(0, height)
    if (alreadyPicked.contains(p)) {
      pickMine(alreadyPicked)(height, width)
    } else p
  }

  def generateBoard(mines: Int, width: Int, height: Int): Either[MinesweeperHttpError, Board] = {
    if(height * width < mines) { ImpossibleGameError.asLeft[Board] } else {
      val minePositions: Set[(Int, Int)] = (0 to mines).toList.foldLeft(Set[(Int, Int)]()){ (picked: Set[(Int, Int)], _: Int) =>
        picked + pickMine(picked)(width, height)
      }
      val positions = for {
        x <- 0 until width
        y <- 0 until height
      } yield (x -> y, Position(x, y, minePositions.contains(x -> y)))
      Board(positions.toMap).asRight[MinesweeperHttpError]
    }
  }

  def generate(user: User, mines: Int, width: Int, height: Int): Either[MinesweeperHttpError, Game] =
    generateBoard(mines, width, height).map(board => Game(UUID.randomUUID().toString, board, user.username))

}

object GameAlg {
  def impl[F[_]: Effect](gameRepository: GameRepository[F], config: DefaultGameConfig): GameAlg[F] = new GameService[F](gameRepository, config)
}

trait GameAlg[F[_]] {
  def get(user: User, id: String): F[Either[MinesweeperHttpError, Game]]

  def reveal(user: User, id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]]

  def unflag(user: User, id: String, x: Int, y: Int): F[Either[MinesweeperHttpError, Game]]

  def flag(user: User, id: String, x: Int, y: Int, flagType: FlagType): F[Either[MinesweeperHttpError, Game]]

  def generateGame(user: User, mines: Option[Int], width: Option[Int], height: Option[Int]): F[Either[MinesweeperHttpError, Game]]
}

