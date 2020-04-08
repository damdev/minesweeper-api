package com.github.damdev.minesweeper.minesweeperapi.codecs
import com.github.damdev.minesweeper.minesweeperapi.utils.User
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object UserCodecs {

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

}
