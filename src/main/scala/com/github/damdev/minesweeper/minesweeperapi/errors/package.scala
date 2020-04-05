package com.github.damdev.minesweeper.minesweeperapi

package object errors {

  trait MinesweeperError {
    def msg: String
  }

  case class BoardError(msg: String) extends MinesweeperError
}
