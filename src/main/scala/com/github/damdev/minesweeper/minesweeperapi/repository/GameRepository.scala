package com.github.damdev.minesweeper.minesweeperapi.repository

import cats.data.OptionT
import cats.effect.Effect
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardStatus, Game, Position}
import doobie.free.connection.ConnectionIO
import doobie.util.meta.Meta
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import doobie.implicits._
import doobie.syntax._
import cats._
import cats.implicits._

private object PositionSQL {

  def upsert(p: Position, gameId: String): Update0 = sql"""
    INSERT INTO POSITIONS (GAME_ID, X, Y, MINE, FLAGGED, REVEALED)
    VALUES ($gameId, ${p.x}, ${p.y}, ${p.mine}, ${p.flagged}, ${p.revealed})
    ON DUPLICATE KEY UPDATE SET MINE = ${p.mine}, FLAGGED = ${p.flagged}, REVEALED = ${p.revealed}
  """.update

  def findByGame(gameId: String): Query0[Position] = sql"""
    SELECT X, Y, MINE, FLAGGED, REVEALED
    FROM POSITIONS
    WHERE GAME_ID = $gameId""".query

  def table(): Update0 = sql"""
    CREATE TABLE POSITIONS (
      GAME_ID VARCHAR(255),
      X INT,
      Y INT,
      MINE BOOLEAN,
      FLAGGED BOOLEAN,
      REVEALED BOOLEAN,
      PRIMARY KEY (GAME_ID, X, Y),
      FOREIGN KEY (GAME_ID)
        REFERENCES GAMES(ID)
        ON DELETE CASCADE
    )
  """.update
}

private object GameSQL {
  implicit val StatusMeta: Meta[BoardStatus] =
    Meta[String].imap(BoardStatus.fromString)(BoardStatus.toString)

  def upsert(game: Game): Update0 = sql"""
    INSERT INTO GAMES (ID, BOARD_STATUS, LAST_MOVE_ERROR)
    VALUES (${game.id}, ${game.boardStatus}, ${game.lastMoveError})
    ON DUPLICATE KEY UPDATE SET BOARD_STATUS = ${game.boardStatus}, LAST_MOVE_ERROR = ${game.lastMoveError}
  """.update

  def select(id: String): Query0[(String, BoardStatus, Option[String])] = sql"""
    SELECT ID, BOARD_STATUS, LAST_MOVE_ERROR
    FROM GAMES
    WHERE ID = $id
  """.query[(String, BoardStatus, Option[String])]

  def delete(id: String): Update0 = sql"""
    DELETE FROM GAMES WHERE ID = $id
  """.update

  def table(): Update0 = sql"""
    CREATE TABLE GAMES (
      ID VARCHAR(255) NOT NULL,
      BOARD_STATUS VARCHAR(255) NOT NULL,
      LAST_MOVE_ERROR VARCHAR(255),
      PRIMARY KEY (ID)
    )
  """.update

}

class GameRepository[F[_]: Effect](val xa: Transactor[F]) {
  import GameSQL._

  def upsert(game: Game): F[Game] = (for {
    g <- GameSQL.upsert(game).run
    _ <- game.board.positions.values.toList.traverse(p => PositionSQL.upsert(p, game.id).run)
  } yield(g)).map(_ => game).transact(xa)


  def get(id: String): F[Option[Game]] = (for {
      (id, boardStatus, lastMoveError) <- OptionT(select(id).option)
      positions <- OptionT.liftF(PositionSQL.findByGame(id).to[List])
  } yield(buildGame(id, boardStatus, lastMoveError, positions))).value.transact(xa)

  def delete(id: String): F[Int] = GameSQL.delete(id).run.transact(xa)

  def setup(): F[Int] = (for {
    g <- GameSQL.table().run
    p <- PositionSQL.table().run
  } yield (g + p)).transact(xa)

  private def buildGame(id: String, boardStatus: BoardStatus, lastMoveError: Option[String], positions: List[Position]): Game =
    Game(id, Board(positions = positions.map(p => (p.x -> p.y) -> p).toMap), boardStatus, lastMoveError)
}

object GameRepository {
  def apply[F[_]: Effect](xa: Transactor[F]): GameRepository[F] =
    new GameRepository(xa)
}