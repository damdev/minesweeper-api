package com.github.damdev.minesweeper.minesweeperapi.model

import java.time.Instant

import com.github.damdev.minesweeper.minesweeperapi.errors._

case class Game(id: String, board: Board, owner: String, startTime: Instant = Instant.now(), finishTime: Option[Instant] = None, boardStatus: BoardStatus = BoardStatus.Continue, lastMoveError: Option[String] = None) {

  def ifNotFinished(f: => Game): Game = if (boardStatus.continue) f else copy(lastMoveError = Some(GameTerminatedError.msg))

  def elapsedTime(): Long = finishTime.getOrElse(Instant.now()).getEpochSecond - startTime.getEpochSecond

  def patch(x: Int, y: Int, patch: Either[RevealPatch, FlagPatch]): Game = ifNotFinished {
    board.patch(x, y, patch)
      .fold(
        err => copy(lastMoveError = Some(err.msg)),
        rr => copy(board = rr.board, boardStatus = rr.boardStatus,
          finishTime = if (boardStatus.continue) None else Some(Instant.now()), lastMoveError = None)
      )
  }
}
