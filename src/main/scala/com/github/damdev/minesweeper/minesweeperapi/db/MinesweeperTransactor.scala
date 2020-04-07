package com.github.damdev.minesweeper.minesweeperapi.db

import cats.effect.{Blocker, ContextShift, Effect, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object MinesweeperTransactor {

  def apply[F[_]: Effect]()(implicit contextShift: ContextShift[F]): Resource[F, HikariTransactor[F]] = {
      for {
        ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
        be <- Blocker[F]    // our blocking EC
        xa <- HikariTransactor.newHikariTransactor[F](
          "org.h2.Driver",                        // driver classname
          "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",   // connect URL
          "sa",                                   // username
          "",                                     // password
          ce,                                     // await connection here
          be                                      // execute JDBC operations here
        )
      } yield xa
  }
}
