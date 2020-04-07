package com.github.damdev.minesweeper.minesweeperapi.utils

import java.security.MessageDigest

import cats.Applicative
import cats.implicits._
import cats.effect._
import com.github.damdev.minesweeper.minesweeperapi.repository.UserRepository
import doobie.util.transactor.Transactor
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.{BasicAuth, DigestAuth}
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import sun.security.provider.MD5
import sun.security.rsa.RSASignature.MD5withRSA


case class User(username: String, hash: String)

class Authentication[F[_]: Sync: Applicative](userRepository: UserRepository[F]) {


  def validate: BasicAuthenticator[F, User] = { credentials =>
      userRepository.validate(credentials.username, Authentication.hash(credentials.password)).value
  }

  def authUser: AuthMiddleware[F, User] =
    BasicAuth.apply("minesweeper-api", validate)
}

object Authentication {
  def hash(password: String): String = new String(MessageDigest.getInstance("MD5").digest(password.getBytes))

  def apply[F[_]: Sync: Applicative](userRepository: UserRepository[F]): Authentication[F] = new Authentication(userRepository)
}