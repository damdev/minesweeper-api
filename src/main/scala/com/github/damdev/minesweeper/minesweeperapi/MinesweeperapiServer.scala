package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.services.GameService
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object MinesweeperapiServer {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      helloWorldAlg = HelloWorld.impl[F]
      gameAlg = GameService.impl[F]

      httpApp = (
        MinesweeperapiRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
        MinesweeperapiRoutes.get[F](gameAlg)
      ).orNotFound

      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}