package com.github.damdev.minesweeper.minesweeperapi.utils

import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.auto._

object Config {

  case class DatabaseConfig(url: String, driver: String, username: String, password: String)

  case class HttpConfig(interface: String, port: Int)

  case class ServerConfig(http: HttpConfig)

  case class DefaultGameConfig(mines: Int, width: Int, height: Int)

  case class MinesweeperApiConfig(server: ServerConfig, defaults: DefaultGameConfig, database: DatabaseConfig)

  def apply(): Result[MinesweeperApiConfig] = {
    ConfigSource.default.load[MinesweeperApiConfig]
  }
}
