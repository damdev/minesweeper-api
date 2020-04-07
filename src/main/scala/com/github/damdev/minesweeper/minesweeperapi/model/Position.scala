package com.github.damdev.minesweeper.minesweeperapi.model


case class Position(x: Int, y: Int, mine: Boolean, flagged: Boolean = false, revealed: Boolean = false) {
  def toggleFlag(): Position = this.copy(flagged = !flagged)

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
    else if (flagged) "F"
    else userAdjacentMines(b).map(am => if(am > 0) am.toString else "R").getOrElse("_")
}

