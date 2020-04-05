package com.github.damdev.minesweeper.minesweeperapi.model

import com.github.damdev.minesweeper.minesweeperapi.errors.{BoardError, MinesweeperError}

case class Board(positions: Map[(Int, Int), Position]) {
  def flag(x: Int, y: Int): Either[MinesweeperError, Board] = positions.get(x -> y).map { p =>
    Right(this.copy(positions.updated(x -> y, p.toggleFlag())))
  }.getOrElse(Left(BoardError("Index out of board")))

  def winOrContinue(): RevealResult =
    if(positions.values.filter(!_.mine).forall(p => p.revealed)) RevealResult.win(this) else RevealResult.continue(this)

  def reveal(x: Int, y: Int): Either[MinesweeperError, RevealResult] = {
    positions.get(x -> y).map { p =>
      val revealed = p.reveal()
      Right(
        if (p.mine) {
          RevealResult.lose(this.copy(this.positions.updated(x -> y, revealed)))
        } else if (revealed.adjacentMines(this).contains(0)) {
          this.copy(revealAllNeighbourgNoMines(this.positions.updated(x -> y, revealed), revealed)).winOrContinue()
        } else {
          this.copy(this.positions.updated(x -> y, revealed)).winOrContinue()
        }
      )
    }.getOrElse(Left(BoardError(s"Index out of board, ($x, $y)")))
  }

  def hasNoAdjacentMines(positions: Map[(Int, Int), Position], p: Position): Boolean =
    positions.values.filter(p.isNeighbour).count(_.mine) == 0

  def revealAllNeighbourgNoMines(positions: Map[(Int, Int), Position], position: Position): Map[(Int, Int), Position] = {
    val revealed = positions.values.filter(position.isNeighbour).filter(p => !p.mine && !p.revealed).map(_.reveal())
    val toReveal = positions.values.filter(position.isNeighbour).filter(p => !p.mine && !p.revealed && hasNoAdjacentMines(positions, p)).map(_.reveal())
    val newPositions = revealed.foldLeft(positions)((ps, n) => ps.updated(n.x -> n.y, n))
    toReveal.foldLeft(newPositions)(revealAllNeighbourgNoMines)
  }

  override def toString: String = {
    val maxX = positions.keys.map(_._1).max
    val maxY = positions.keys.map(_._2).max
    val representation = positions.values.toList.sortBy(p => p.y*10000 + p.x).foldLeft((0, "")) { (s, pos) =>
      if (pos.y != s._1) {
        (pos.y, s._2 + s"\n${pos}")
      } else {
        (pos.y, s._2 + s"${pos}")

      }
    }
    representation._2
  }
}

case class RevealResult(board: Board, win: Boolean, lose: Boolean)

object RevealResult {
  def win(b: Board) = RevealResult(b, true, false)
  def continue(b: Board) = RevealResult(b, false, false)
  def lose(b: Board) = RevealResult(b, false, true)
}

object BoardParser {
  def isMine(i: Char): Boolean = 'X' == i.toUpper

  def parse(s: String): Board = {
    val lines = s.split("\n")
    val positions = for {
      (line, y) <- lines.zipWithIndex
      (char, x) <- line.zipWithIndex
    } yield (x, y) -> Position(x, y, isMine(char))
    Board(positions.toMap)
  }
}

