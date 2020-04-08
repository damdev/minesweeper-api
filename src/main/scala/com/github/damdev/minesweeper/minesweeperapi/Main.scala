package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.db.MinesweeperTransactor
import com.github.damdev.minesweeper.minesweeperapi.utils.Config
import org.log4s.getLogger

object Main extends IOApp {
  val logger = getLogger

  def run(args: List[String]): IO[ExitCode] =
    Config().fold(
      { err =>
        IO {
          logger.error(s"Error loading configuration: ${err.prettyPrint()}")
          ExitCode.Error
        }
      },
      { c =>
        MinesweeperTransactor[IO](c.database).use { tx =>
          MinesweeperapiServer.stream[IO](c, tx).compile.drain.as(ExitCode.Success)
        }
      })

}