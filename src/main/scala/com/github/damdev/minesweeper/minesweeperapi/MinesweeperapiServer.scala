package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.repository.{GameRepository, UserRepository}
import com.github.damdev.minesweeper.minesweeperapi.services.GameAlg
import com.github.damdev.minesweeper.minesweeperapi.utils.Authentication
import com.github.damdev.minesweeper.minesweeperapi.utils.Config.MinesweeperApiConfig
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object MinesweeperapiServer {

  def stream[F[_]: ConcurrentEffect](config: MinesweeperApiConfig, tx: Transactor[F])(implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]

      userRepository = UserRepository(tx)
      _ <- Stream.eval(userRepository.setup())
      authentication = Authentication(userRepository).authUser

      gameRepository = GameRepository(tx)
      _ <- Stream.eval(gameRepository.setup())
      gameAlg = GameAlg.impl[F](gameRepository, config.defaults)

      httpApp = (
        MinesweeperapiRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
          authentication(MinesweeperapiRoutes.game[F](gameAlg))
      ).orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(config.server.http.port, config.server.http.interface)
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}