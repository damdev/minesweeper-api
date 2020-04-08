package com.github.damdev.minesweeper.minesweeperapi.utils

import java.security.MessageDigest

import cats.Applicative
import cats.implicits._
import cats.effect._
import com.github.damdev.minesweeper.minesweeperapi.repository.UserRepository
import com.github.damdev.minesweeper.minesweeperapi.services.UserAlg
import doobie.util.transactor.Transactor
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.{BasicAuth, DigestAuth}
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import sun.security.provider.MD5
import sun.security.rsa.RSASignature.MD5withRSA


case class User(username: String, hash: String)
case class UserRequest(username: String, password: String)

object User {
  def hash(password: String): String = new String(MessageDigest.getInstance("MD5").digest(password.getBytes))

  def fromRequest(ur: UserRequest): User = User(ur.username, hash(ur.password))
}

class Authentication[F[_]: Sync: Applicative](U: UserAlg[F]) {


  def validate: BasicAuthenticator[F, User] = { credentials =>
      U.validate(credentials.username, User.hash(credentials.password)).value
  }

  def authUser: AuthMiddleware[F, User] =
    BasicAuth.apply("minesweeper-api", validate)
}

object Authentication {
  def apply[F[_]: Sync: Applicative](U: UserAlg[F]): Authentication[F] = new Authentication(U)
}