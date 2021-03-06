package com.github.damdev.minesweeper.minesweeperapi

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
        case e @ NotYourGameError(_) => Forbidden(e.msg)
        case e @ ImpossibleGameError => BadRequest(e.msg)
      }
    }
  }

  case class GameNotFoundError(id: String) extends MinesweeperHttpError {
    override def msg: String = s"Game with id: $id not found."
  }

  case class NotYourGameError(id: String) extends MinesweeperHttpError {
    override def msg: String = s"Game with id: $id is not yours."
  }

  case object ImpossibleGameError extends MinesweeperHttpError {
    override def msg: String = s"You are trying to generate an impossible game."
  }

  case object GameTerminatedError extends MinesweeperError {
    override def msg: String = "Game already finished."
  }

  case class BoardError(msg: String) extends MinesweeperError

  case class IndexOutOfBoardError(x: Int, y: Int) extends MinesweeperError {
    override def msg: String = s"Index out of board, ($x, $y)"
  }

  case class UndoRevealError(x: Int, y: Int) extends MinesweeperError {
    override def msg: String = s"Can't undo a reveal, on ($x, $y)"
  }

  case class TooManyFlagsError(mines: Int) extends MinesweeperError {
    override def msg: String = s"Too many red flags. #Mines: $mines"
  }
}
