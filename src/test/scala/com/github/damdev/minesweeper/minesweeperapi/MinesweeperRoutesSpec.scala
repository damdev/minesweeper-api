package com.github.damdev.minesweeper.minesweeperapi

import java.time.Instant

import cats.effect.IO
import com.github.damdev.minesweeper.minesweeperapi.model.{Board, Game}
import com.github.damdev.minesweeper.minesweeperapi.services.GameAlg
import com.github.damdev.minesweeper.minesweeperapi.utils.User
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import org.specs2.mock.Mockito

class MinesweeperRoutesSpec extends org.specs2.mutable.Specification with Mockito {

  "HelloWorld" >> {
    "return 200" >> {
      uriReturns200()
    }
    "return hello world" >> {
      uriReturnsAGame()
    }
  }

  val start = Instant.now()
  val finish = start.plusSeconds(100)

  private[this] val newGame: Response[IO] = {
    val game = Game("id", Board.apply(Map()), "dam", startTime = start, finishTime = Some(finish))

    val getHW = AuthedRequest(User("dam", "dam"), Request[IO](Method.GET, uri"/games/new"))
    val G = mock[GameAlg[IO]]
    G.generateGame(any[User](), any[Option[Int]](), any[Option[Int]](), any[Option[Int]]()) returns IO(Right(game))
    MinesweeperapiRoutes.games(G).orNotFound(getHW).unsafeRunSync()
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    newGame.status must beEqualTo(Status.Ok)

  private[this] def uriReturnsAGame(): MatchResult[String] = {
    val expected = s"""{"id":"id","status":"Continue","owner":"dam","start_time":${start.getEpochSecond},"elapsed_time_seconds":100,"finish_time":${finish.getEpochSecond},"board":{"positions":[],"ascii":""},"last_move_error":null}"""
    newGame.as[String].unsafeRunSync() must beEqualTo(expected)
  }
}