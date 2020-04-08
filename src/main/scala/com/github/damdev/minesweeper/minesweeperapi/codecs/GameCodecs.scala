package com.github.damdev.minesweeper.minesweeperapi.codecs

import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardStatus, FlagType, Game, Position}
import io.circe.{Decoder, Encoder, Json}
import io.circe._
import io.circe.syntax._

object GameCodecs {

  implicit val flagEncoder: Encoder[FlagType] = (ft: FlagType) => Json.fromString(FlagType.asString(ft))

  implicit val boardStatusEncoder: Encoder[BoardStatus] = (a: BoardStatus) =>
    Json.fromString(BoardStatus.toString(a))

  implicit val positionEncoder: Encoder.AsObject[Position] = (p: Position) => Json.obj(
    "x" -> p.x.asJson,
    "y" -> p.y.asJson,
    "mine" -> p.publicIsMine().asJson,
    "flagged" -> p.flag.asJson,
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
    "board" -> g.board.asJson
  )
}
