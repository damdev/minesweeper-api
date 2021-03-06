package com.github.damdev.minesweeper.minesweeperapi.model

import com.github.damdev.minesweeper.minesweeperapi.errors._
import cats.implicits._

case class Board(positions: Map[(Int, Int), Position]) {
  def patch(x: Int, y: Int, patch: Either[RevealPatch, FlagPatch]): Either[MinesweeperError, RevealResult] = {
    patch.fold(_.patch(x, y, this), _.patch(x, y, this).map(RevealResult.continue))
  }

  private def withPosition[T](x: Int, y: Int)(f: Position => Either[MinesweeperError, T]): Either[MinesweeperError, T] =
    positions.get(x -> y).map(f).getOrElse(Left(IndexOutOfBoardError(x, y)))

  def flag(x: Int, y: Int, flagType: FlagType): Either[MinesweeperError, Board] = withPosition(x, y) { p =>
      val newBoard = copy(positions.updated(x -> y, p.flag(flagType)))
      if (newBoard.flagCount > newBoard.mineCount)
        Left(TooManyFlagsError(mineCount))
      else
        Right(newBoard)
  }

  def unflag(x: Int, y: Int): Either[MinesweeperError, Board] = withPosition(x, y) { p =>
      val newBoard = copy(positions.updated(x -> y, p.unflag()))
      if (newBoard.flagCount > newBoard.mineCount)
        Left(TooManyFlagsError(mineCount))
      else
        Right(newBoard)
  }

  private def mineCount = positions.values.count(_.mine)
  private def flagCount = positions.values.count(_.flag.contains(FlagType.RedFlag))

  private def winOrContinue(): RevealResult =
    if(positions.values.filter(!_.mine).forall(p => p.revealed)) RevealResult.win(this) else RevealResult.continue(this)

  def reveal(x: Int, y: Int): Either[MinesweeperError, RevealResult] = withPosition(x, y) { p =>
      val revealed = p.reveal()
      Right(
        if (p.mine) {
          RevealResult.lose(copy(positions.updated(x -> y, revealed)))
        } else if (revealed.privateHasNoAdjacentMines(this)) {
          val tbr = toBeRevealed(positions.values.toSet, Set(revealed), Set(revealed), Set())
          copy(positions = tbr.foldLeft(positions)((ps, n) => ps.updated(n.x -> n.y, n.reveal()))).winOrContinue()
        } else {
          copy(positions.updated(x -> y, revealed)).winOrContinue()
        }
      )
  }

  private def toBeRevealed(positions: Set[Position], toDiscover: Set[Position], toReveal: Set[Position], scanned: Set[Position]): Set[Position] = {
      val newToRevealed = positions.filter(p => toDiscover.exists(_.isNeighbour(p))).filter(p => !p.mine && !p.revealed)
      val newToDiscover = positions.filter(p => toDiscover.exists(_.isNeighbour(p)) && !scanned.contains(p)).filter(p => !p.mine && !p.revealed && p.privateHasNoAdjacentMines(this))
    if(newToDiscover.isEmpty) {
      newToRevealed ++ toReveal
    } else {
      toBeRevealed(positions, newToDiscover, newToRevealed ++ toReveal, scanned ++ toDiscover)
    }
  }

  override def toString: String = {
    val representation = positions.values.toList.sortBy(p => p.y*10000 + p.x).foldLeft((0, "")) { (s, pos) =>
      if (pos.y != s._1) {
        (pos.y, s._2 + s"\n${pos.toString(this)}")
      } else {
        (pos.y, s._2 + s"${pos.toString(this)}")
      }
    }
    representation._2
  }
}

case class RevealResult(board: Board, boardStatus: BoardStatus)

trait BoardStatus {
  def continue: Boolean

  override def toString: String = BoardStatus.toString(this)
}

object BoardStatus {

  def toString(boardStatus: BoardStatus): String = boardStatus match {
    case Continue => "Continue"
    case Lose => "Lose"
    case Win => "Win"
  }

  def fromString(boardStatus: String): BoardStatus = boardStatus match {
    case "Lose" => Lose
    case "Win" => Win
    case _ => Continue
  }
  case object Continue extends BoardStatus { val continue: Boolean = true }
  case object Win extends BoardStatus { val continue: Boolean = false }
  case object Lose extends BoardStatus { val continue: Boolean = false }

}


object RevealResult {
  def win(b: Board) = RevealResult(b, BoardStatus.Win)
  def continue(b: Board) = RevealResult(b, BoardStatus.Continue)
  def lose(b: Board) = RevealResult(b, BoardStatus.Lose)
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

case class FlagPatch(flag: Option[FlagType]) {
  def patch(x: Int, y: Int, board: Board): Either[MinesweeperError, Board] = flag.fold(board.unflag(x, y))(ft => board.flag(x, y, ft))
}
case class RevealPatch(revealed: Boolean) {
  def patch(x: Int, y: Int, board: Board): Either[MinesweeperError, RevealResult] = if(revealed) board.reveal(x ,y) else UndoRevealError(x ,y).asLeft[RevealResult]
}