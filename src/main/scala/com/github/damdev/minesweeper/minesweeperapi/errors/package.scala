package com.github.damdev.minesweeper.minesweeperapi

import com.sun.deploy.net.HttpResponse
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import cats.effect._

package object errors {

  sealed trait MinesweeperError {
    def msg: String
  }

  sealed trait MinesweeperHttpError extends MinesweeperError {
    def toResponse[F[_]: Sync](implicit dsl: Http4sDsl[F]) = MinesweeperHttpError.toResponse(this)
  }

  object MinesweeperHttpError {
    def toResponse[F[_]: Sync](error: MinesweeperHttpError)(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
      import dsl._
      error match {
        case e @ GameNotFoundError(_) => NotFound(e.msg)
      }
    }
  }

  case class GameNotFoundError(id: String) extends MinesweeperHttpError {
    override def msg: String = s"Game with id: ${id} not found."
  }

  case object GameTerminatedError extends MinesweeperError {
    override def msg: String = "Game already finished."
  }
  case class BoardError(msg: String) extends MinesweeperError
}
