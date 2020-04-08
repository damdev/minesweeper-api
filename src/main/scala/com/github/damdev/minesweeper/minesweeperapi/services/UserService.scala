package com.github.damdev.minesweeper.minesweeperapi.services

import cats.data.OptionT
import cats.effect.Effect
import com.github.damdev.minesweeper.minesweeperapi.repository.UserRepository
import com.github.damdev.minesweeper.minesweeperapi.utils.User

private class UserService[F[_]: Effect](userRepository: UserRepository[F]) extends UserAlg[F] {

  override def saveUser(user: User): F[User] = userRepository.create(user)

  override def validate(username: String, hash: String): OptionT[F, User] = userRepository.validate(username, hash)
}

trait UserAlg[F[_]] {
  def saveUser(user: User): F[User]

  def validate(username: String, hash: String): OptionT[F, User]
}

object UserAlg {
  def impl[F[_]: Effect](userRepository: UserRepository[F]): UserAlg[F] = new UserService[F](userRepository)
}