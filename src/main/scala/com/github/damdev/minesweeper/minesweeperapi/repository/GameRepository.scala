package com.github.damdev.minesweeper.minesweeperapi.repository

import cats.data.OptionT
import cats.effect.Effect
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, BoardStatus, FlagType, Game, Position}
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

  implicit val FlagMeta: Meta[FlagType] =
    Meta[String].imap(ft => FlagType.fromString(ft).get)(ft => FlagType.asString(ft))

  def upsert(p: Position, gameId: String): Update0 = sql"""
    INSERT INTO POSITIONS (GAME_ID, X, Y, MINE, FLAG, REVEALED)
    VALUES ($gameId, ${p.x}, ${p.y}, ${p.mine}, ${p.flag}, ${p.revealed})
    ON DUPLICATE KEY UPDATE MINE = ${p.mine}, FLAG = ${p.flag}, REVEALED = ${p.revealed}
  """.update

  def findByGame(gameId: String): Query0[Position] = sql"""
    SELECT X, Y, MINE, FLAG, REVEALED
    FROM POSITIONS
    WHERE GAME_ID = $gameId""".query

  def table(): Update0 = sql"""
    CREATE TABLE POSITIONS (
      GAME_ID VARCHAR(255) NOT NULL,
      X INT NOT NULL,
      Y INT NOT NULL,
      MINE BOOLEAN NOT NULL,
      FLAG VARCHAR(20),
      REVEALED BOOLEAN NOT NULL,
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
    INSERT INTO GAMES (ID, BOARD_STATUS, OWNER, LAST_MOVE_ERROR)
    VALUES (${game.id}, ${game.boardStatus}, ${game.owner}, ${game.lastMoveError})
    ON DUPLICATE KEY UPDATE BOARD_STATUS = ${game.boardStatus}, LAST_MOVE_ERROR = ${game.lastMoveError}
  """.update

  def select(id: String): Query0[(String, BoardStatus, String, Option[String])] = sql"""
    SELECT ID, BOARD_STATUS, OWNER, LAST_MOVE_ERROR
    FROM GAMES
    WHERE ID = $id
  """.query[(String, BoardStatus, String, Option[String])]

  def delete(id: String): Update0 = sql"""
    DELETE FROM GAMES WHERE ID = $id
  """.update

  def table(): Update0 = sql"""
    CREATE TABLE GAMES (
      ID VARCHAR(255) NOT NULL,
      BOARD_STATUS VARCHAR(255) NOT NULL,
      OWNER VARCHAR(255) NOT NULL,
      LAST_MOVE_ERROR VARCHAR(255),
      PRIMARY KEY (ID),
      FOREIGN KEY (OWNER)
        REFERENCES USERS(USERNAME)
        ON DELETE CASCADE
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
      (id, boardStatus, owner, lastMoveError) <- OptionT(select(id).option)
      positions <- OptionT.liftF(PositionSQL.findByGame(id).to[List])
  } yield(buildGame(id, owner, boardStatus, lastMoveError, positions))).value.transact(xa)

  def delete(id: String): F[Int] = GameSQL.delete(id).run.transact(xa)

  def setup(): F[Int] = (for {
    g <- GameSQL.table().run
    p <- PositionSQL.table().run
  } yield (g + p)).transact(xa)

  private def buildGame(id: String, owner: String, boardStatus: BoardStatus, lastMoveError: Option[String], positions: List[Position]): Game =
    Game(id, Board(positions = positions.map(p => (p.x -> p.y) -> p).toMap), owner, boardStatus, lastMoveError)
}

object GameRepository {
  def apply[F[_]: Effect](xa: Transactor[F]): GameRepository[F] =
    new GameRepository(xa)
}