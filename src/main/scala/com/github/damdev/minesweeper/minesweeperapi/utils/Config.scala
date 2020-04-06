package com.github.damdev.minesweeper.minesweeperapi.utils

import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.auto._

object Config {

  case class HttpConfig(interface: String, port: Int)
  case class ServerConfig(http: HttpConfig)
  case class MinesweeperApiConfig(server: ServerConfig)

  def apply(): Result[MinesweeperApiConfig] = {
    ConfigSource.default.load[MinesweeperApiConfig]
  }
}
