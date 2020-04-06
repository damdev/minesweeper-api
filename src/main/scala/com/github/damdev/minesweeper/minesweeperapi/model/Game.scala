package com.github.damdev.minesweeper.minesweeperapi.model

import com.github.damdev.minesweeper.minesweeperapi.errors._

case class Game(board: Board, boardStatus: BoardStatus = BoardStatus.Continue, lastMoveError: Option[String] = None) {

  def ifNotFinished(f: => Game): Game = if (boardStatus.continue) f else copy(lastMoveError = Some(GameTerminatedError.msg))

  def reveal(x: Int, y: Int): Game = ifNotFinished {
    board.reveal(x, y)
      .fold(
        err => copy(lastMoveError = Some(err.msg)),
        rr => copy(board = rr.board, boardStatus = rr.boardStatus, lastMoveError = None)
      )
  }

  def flag(x: Int, y: Int): Game = ifNotFinished {
    board.flag(x, y)
      .fold(
        err => copy(lastMoveError = Some(err.msg)),
        b => copy(board = b, lastMoveError = None)
      )
  }
}
