package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    MinesweeperapiServer.stream[IO].compile.drain.as(ExitCode.Success)
}