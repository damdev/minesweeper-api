package com.github.damdev.minesweeper.minesweeperapi.codecs

import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardStatus, Game, Position}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import io.circe._
import io.circe.syntax._

object GameCodecs {

  implicit val boardStatusEncoder: Encoder[BoardStatus] = (a: BoardStatus) =>
    Json.fromString(BoardStatus.toString(a))

  implicit val positionEncoder: Encoder.AsObject[Position] = (p: Position) => Json.obj(
    "x" -> p.x.asJson,
    "y" -> p.y.asJson,
    "mine" -> p.publicIsMine().asJson,
    "flagged" -> p.flagged.asJson,
    "revealed" -> p.revealed.asJson,
  ).asObject.get

  implicit val positionDecoder: Decoder[Position] = deriveDecoder

  implicit val boardEncoder: Encoder[Board] = (a: Board) => Json.obj(
    "positions" -> Json.fromValues(a.positions.values.map(
      p => p.asJsonObject.+:("adjacent_mines", p.userAdjacentMines(a).asJson)
    ).map(_.asJson).toList),
    "ascii" -> Json.fromString(a.toString)
  )

  implicit val gameEncoder: Encoder[Game] = deriveEncoder
}
