package com.github.damdev.minesweeper.minesweeperapi.model


case class Position(x: Int, y: Int, mine: Boolean, flag: Option[FlagType] = None, revealed: Boolean = false) {
  def flag(flagType: FlagType): Position = this.copy(flag = Some(flagType))

  def unflag(): Position = this.copy(flag = None)

  def reveal(): Position = this.copy(revealed = true)

  def publicIsMine(): Option[Boolean] = if(revealed) Some(mine) else None

  // Maybe cache it in a lazy val?
  def userAdjacentMines(b: Board): Option[Int] = if(revealed) Some(privateAdjacentMines(b)) else None

  def privateAdjacentMines(b: Board): Int = this.neighbours(b).count(_.mine)

  def privateHasNoAdjacentMines(b: Board): Boolean = privateAdjacentMines(b) == 0

  def isNeighbour(pp: Position) = Math.abs(pp.x - this.x) <= 1 && Math.abs(pp.y - this.y) <= 1 && this != pp

  def neighbours(b: Board): Iterator[Position] = b.positions.valuesIterator.filter(this.isNeighbour)

  def toString(b: Board): String =
    if(mine && revealed) "*"
    else if (flag.isDefined) flag.filter(_ == FlagType.RedFlag).map(_ => "F").getOrElse("?")
    else userAdjacentMines(b).map(am => if(am > 0) am.toString else "R").getOrElse("_")
}

sealed trait FlagType

object FlagType {

  case object RedFlag extends FlagType
  case object QuestionMark extends FlagType

  def fromString(s: String): Option[FlagType] = s.toLowerCase match {
    case "red_flag" => Some(RedFlag)
    case "question_mark" => Some(QuestionMark)
    case _ => None
  }

  def asString(f: FlagType): String = f match {
    case RedFlag => "red_flag"
    case QuestionMark => "question_mark"
  }
}