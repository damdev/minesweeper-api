package com.github.damdev.minesweeper.minesweeperapi

import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardStatus, Game, Position}
import io.circe.Encoder
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

object GameCodecs {

  implicit val boardStatusEncoder: Encoder[BoardStatus] = (a: BoardStatus) =>
    Json.fromString(BoardStatus.toString(a))
//  implicit val boardStatusDecoder: Decoder[BoardStatus] = (a: JsonS)

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
      p => p.asJsonObject.+:("adjacent_mines", p.adjacentMines(a).asJson)
    ).map(_.asJson).toList),
    "ascii" -> Json.fromString(a.toString)
  )
//  implicit val boardDecoder: Decoder[Board] = deriveDecoder

  implicit val gameEncoder: Encoder[Game] = deriveEncoder
//  implicit val gameDecoder: Decoder[Game] = deriveDecoder
}
