package com.github.damdev.minesweeper.minesweeperapi.codecs
import com.github.damdev.minesweeper.minesweeperapi.utils.{User, UserRequest}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object UserCodecs {

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

  implicit val userRequestEncoder: Encoder[UserRequest] = deriveEncoder[UserRequest]
  implicit val userRequestDecoder: Decoder[UserRequest] = deriveDecoder[UserRequest]

}
