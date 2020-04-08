package com.github.damdev.minesweeper.minesweeperapi.db

import java.net.URI

import cats.effect.{Blocker, ContextShift, Effect, Resource}
import com.github.damdev.minesweeper.minesweeperapi.utils.Config.DatabaseConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object MinesweeperTransactor {

  def apply[F[_]: Effect](config: DatabaseConfig)(implicit contextShift: ContextShift[F]): Resource[F, HikariTransactor[F]] = {
      for {
        ce <- ExecutionContexts.fixedThreadPool[F](32)
        be <- Blocker[F]
        xa <- HikariTransactor.newHikariTransactor[F](
          config.driver,
          config.url,
          config.username,
          config.password,
          ce,
          be
        )
      } yield xa
  }
}
