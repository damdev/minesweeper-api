package com.github.damdev.minesweeper.minesweeperapi.codecs

import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardStatus, FlagPatch, FlagType, Game, Position, RevealPatch}
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import io.circe.generic.semiauto._
import io.circe.syntax._
import cats.implicits._

object GameCodecs {

  implicit def eitherDecoder[A,B](implicit a: Decoder[A], b: Decoder[B]): Decoder[Either[A,B]] = {
    val l: Decoder[Either[A,B]]= a.map(Left.apply)
    val r: Decoder[Either[A,B]]= b.map(Right.apply)
    l or r
  }

  implicit val flagEncoder: Encoder[FlagType] = (ft: FlagType) => Json.fromString(FlagType.asString(ft))
  implicit val flagDecoder: Decoder[FlagType] = (c: HCursor) => (for {
    s <- c.value.asString
    ft <- FlagType.fromString(s)
  } yield ft).fold(DecodingFailure("Invalid flag type.", Nil).asLeft[FlagType])(f => f.asRight[DecodingFailure])

  implicit val flagPatchDecoder: Decoder[FlagPatch] = deriveDecoder[FlagPatch]

  implicit val revealPatchDecoder: Decoder[RevealPatch] = deriveDecoder[RevealPatch]

  implicit val boardStatusEncoder: Encoder[BoardStatus] = (a: BoardStatus) =>
    Json.fromString(BoardStatus.toString(a))

  implicit val positionEncoder: Encoder.AsObject[Position] = (p: Position) => Json.obj(
    "x" -> p.x.asJson,
    "y" -> p.y.asJson,
    "mine" -> p.publicIsMine().asJson,
    "flag" -> p.flag.asJson,
    "revealed" -> p.revealed.asJson,
  ).asObject.get

  implicit val boardEncoder: Encoder[Board] = (a: Board) => Json.obj(
    "positions" -> Json.fromValues(a.positions.values.map(
      p => p.asJsonObject.+:("adjacent_mines", p.userAdjacentMines(a).asJson)
    ).map(_.asJson).toList),
    "ascii" -> Json.fromString(a.toString)
  )

  implicit val gameEncoder: Encoder[Game] = (g: Game) => Json.obj(
    "id" -> Json.fromString(g.id),
    "status" -> g.boardStatus.asJson,
    "owner" -> g.owner.asJson,
    "start_time" -> g.startTime.getEpochSecond.asJson,
    "elapsed_time_seconds" -> g.elapsedTime().asJson,
    "finish_time" -> g.finishTime.map(_.getEpochSecond).asJson,
    "board" -> g.board.asJson,
    "last_move_error" -> g.lastMoveError.asJson
  )
}
