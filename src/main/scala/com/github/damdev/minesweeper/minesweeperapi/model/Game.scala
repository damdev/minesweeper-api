package com.github.damdev.minesweeper.minesweeperapi.model

import com.github.damdev.minesweeper.minesweeperapi.errors._

case class Game(board: Board, boardStatus: BoardStatus = BoardStatus.Continue, lastMoveError: Option[String] = None) {

  private def revealWithErr(x: Int, y: Int): Either[MinesweeperError, Game] = if (boardStatus.continue) board.reveal(x, y).map(rr =>
    copy(rr.board, rr.boardStatus)
  ) else Left(GameTerminatedError)

  def reveal(x: Int, y: Int): Game = {
    revealWithErr(x, y).fold(e => copy(lastMoveError = Some(e.msg)), identity)
  }

  def flag(x: Int, y: Int): Game = {
    board.flag(x, y)
      .fold(
        err => copy(lastMoveError = Some(err.msg)),
        b => copy(board = b, lastMoveError = None)
      )
  }
}
