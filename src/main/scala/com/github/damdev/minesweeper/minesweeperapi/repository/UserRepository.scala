package com.github.damdev.minesweeper.minesweeperapi.repository

import cats.data.OptionT
import cats.effect.{Bracket, Effect}
import com.github.damdev.minesweeper.minesweeperapi.utils.{Authentication, User}
import doobie.syntax._
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import cats.implicits._

private object UserRepositorySQL {

  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (USERNAME, HASH)
    VALUES (${user.username}, ${user.hash})
  """.update

  def byUserName(username: String): Query0[User] = sql"""
    SELECT USERNAME, HASH
    FROM USERS
    WHERE USERNAME = $username
  """.query[User]

  def byUsernameAndHash(username: String, hash: String): Query0[User] = sql"""
    SELECT USERNAME, HASH
    FROM USERS
    WHERE USERNAME = $username AND HASH = $hash
  """.query[User]

  def delete(username: Long): Update0 = sql"""
    DELETE FROM USERS WHERE USERNAME = $username
  """.update

  def table(): Update0 = sql"""
    CREATE TABLE USERS(
      USERNAME VARCHAR(255),
      HASH VARCHAR(255)
    )
  """.update

}

class UserRepository[F[_]: Effect](val xa: Transactor[F]) {
  import UserRepositorySQL._

  def create(user: User): F[User] =
    insert(user).run.transact(xa).map(_ => user)

  def findByUserName(username: String): OptionT[F, User] =
    OptionT(byUserName(username).option.transact(xa))

  def validate(username: String, hash: String): OptionT[F, User] =
    OptionT(byUsernameAndHash(username, hash).option.transact(xa))

  def setup(): F[Int] = (for {
    t <- table().run
    _ <- insert(User("dam", Authentication.hash("dam"))).run
  } yield t).transact(xa)

}

object UserRepository {
  def apply[F[_]: Effect](xa: Transactor[F]): UserRepository[F] = new UserRepository(xa)
}