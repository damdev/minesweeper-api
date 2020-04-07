package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.Sync
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.GameCodecs._
import com.github.damdev.minesweeper.minesweeperapi.services.GameAlg
import com.github.damdev.minesweeper.minesweeperapi.utils.User
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl


object MinesweeperapiRoutes {

  def game[F[_]: Sync](G: GameAlg[F]): AuthedRoutes[User, F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F]{}
    import dsl._
    AuthedRoutes.of[User, F] {
      case GET -> Root / "game" / id as user  =>
        for {
          game <- G.get(id)
          resp <- game.fold(_.toResponse[F], g => Ok(g))
        } yield resp
      case GET -> Root / "game" / id / "reveal" / IntVar(x) / IntVar(y) as user  =>
        for {
          revealed <- G.reveal(id, x, y)
          resp <- revealed.fold(_.toResponse[F], g => Ok(g))
        } yield resp
      case GET -> Root / "game" / id / "flag" / IntVar(x) / IntVar(y) as user  =>
        for {
          revealed <- G.flag(id, x, y)
          resp <- revealed.fold(_.toResponse[F], g => Ok(g))
        } yield resp

    }
  }

  def helloWorldRoutes[F[_]: Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }
}