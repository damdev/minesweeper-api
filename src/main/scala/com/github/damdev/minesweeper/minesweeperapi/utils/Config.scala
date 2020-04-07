package com.github.damdev.minesweeper.minesweeperapi.utils

import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.auto._

object Config {

  case class HttpConfig(interface: String, port: Int)

  case class ServerConfig(http: HttpConfig)

  case class DefaultGameConfig(mines: Int, width: Int, height: Int)

  case class MinesweeperApiConfig(server: ServerConfig, defaults: DefaultGameConfig)

  def apply(): Result[MinesweeperApiConfig] = {
    ConfigSource.default.load[MinesweeperApiConfig]
  }
}
