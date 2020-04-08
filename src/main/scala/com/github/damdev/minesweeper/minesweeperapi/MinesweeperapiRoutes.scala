package com.github.damdev.minesweeper.minesweeperapi

import cats.effect.Sync
import cats.implicits._
import com.github.damdev.minesweeper.minesweeperapi.codecs.GameCodecs._
import com.github.damdev.minesweeper.minesweeperapi.codecs.UserCodecs._
import com.github.damdev.minesweeper.minesweeperapi.model.FlagType
import com.github.damdev.minesweeper.minesweeperapi.services._
import com.github.damdev.minesweeper.minesweeperapi.utils.{User, UserRequest}
import org.http4s.{AuthedRoutes, HttpRoutes, ParseFailure, QueryParamDecoder}
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import io.circe.syntax._
import io.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher


object MinesweeperapiRoutes {

  def games[F[_]: Sync](G: GameAlg[F]): AuthedRoutes[User, F] = {
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
      case GET -> Root / "game" / id / "flag" / IntVar(x) / IntVar(y) :? OptionalFlagTypeQueryParamMatcher(flagType) as user  =>
        for {
          flagged <- G.flag(user, id, x, y, flagType.getOrElse(FlagType.RedFlag))
          resp <- flagged.fold(_.toResponse[F], g => Ok(g))
        } yield resp

    }
  }

  def users[F[_]: Sync](U: UserAlg[F]): HttpRoutes[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ POST -> Root / "users" => for {
        user <- req.as[UserRequest]
        u <- U.saveUser(User.fromRequest(user))
        resp <- Ok(s"User ${u.username} created.")
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

object OptionalFlagTypeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[FlagType]("flag_type")(
  QueryParamDecoder[String].emap(s => FlagType.fromString(s).toRight(ParseFailure(s"Invalid flag ${s} type only red_flag or question_mark.", s"Invalid flag ${s} type only red_flag or question_mark.")))
)
object OptionalMinesQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("mines")
object OptionalHeightQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("height")
object OptionalWidthQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("width")