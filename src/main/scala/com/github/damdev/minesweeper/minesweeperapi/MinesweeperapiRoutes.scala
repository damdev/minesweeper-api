package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.Sync
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.GameCodecs._
import com.github.damdev.minesweeper.minesweeperapi.MinesweeperapiRoutes._
import com.github.damdev.minesweeper.minesweeperapi.services.GameAlg
import com.github.damdev.minesweeper.minesweeperapi.utils.Config.MinesweeperApiConfig
import com.github.damdev.minesweeper.minesweeperapi.utils.User
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher


object MinesweeperapiRoutes {

  def game[F[_]: Sync](G: GameAlg[F]): AuthedRoutes[User, F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F]{}
    import dsl._
    AuthedRoutes.of[User, F] {
      case GET -> Root / "game" / "new" :?
          OptionalMinesQueryParamMatcher(maybeMines) +&
          OptionalHeightQueryParamMatcher(maybeHeight) +&
          OptionalWidthQueryParamMatcher(maybeWidth) as user  =>
        for {
          game <- G.generateGame(user, maybeMines, maybeWidth, maybeHeight)
          resp <- game.fold(_.toResponse[F], g => Ok(g))
        } yield resp
      case GET -> Root / "game" / id as user  =>
        for {
          game <- G.get(user, id)
          resp <- game.fold(_.toResponse[F], g => Ok(g))
        } yield resp
      case GET -> Root / "game" / id / "reveal" / IntVar(x) / IntVar(y) as user  =>
        for {
          revealed <- G.reveal(user, id, x, y)
          resp <- revealed.fold(_.toResponse[F], g => Ok(g))
        } yield resp
      case GET -> Root / "game" / id / "flag" / IntVar(x) / IntVar(y) as user  =>
        for {
          revealed <- G.flag(user, id, x, y)
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

object OptionalMinesQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("mines")
object OptionalHeightQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("height")
object OptionalWidthQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("width")