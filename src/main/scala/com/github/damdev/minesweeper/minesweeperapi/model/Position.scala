package com.github.damdev.minesweeper.minesweeperapi.model


case class Position(x: Int, y: Int, mine: Boolean, flagged: Boolean = false, revealed: Boolean = false) {
  def toggleFlag(): Position = this.copy(flagged = !flagged)

  def reveal(): Position = this.copy(revealed = true)

  def publicIsMine(): Option[Boolean] = if(revealed) Some(mine) else None

  // Maybe cache it in a lazy val?
  def adjacentMines(b: Board): Option[Int] = if(revealed) Some(this.neighbours(b).count(_.mine)) else None

  def isNeighbour(pp: Position) = (pp.x - this.x) <= 1 && (pp.y - this.y) <= 1 && this != pp

  def neighbours(b: Board): Iterator[Position] = b.positions.valuesIterator.filter(this.isNeighbour)

  override def toString: String =
    if(mine) {
      if (flagged) "x" else if(revealed) "*" else "X"
    }
    else if (flagged) "F"
    else if (revealed) "R"
    else "_"
}

