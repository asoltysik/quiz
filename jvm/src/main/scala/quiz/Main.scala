package quiz

import cats.effect.IO
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder
import quiz.quizes.{PostgresQuizRepository, QuizEndpoints}
import quiz.users.{PostgresUserRepository, UserEndpoints}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends StreamApp[IO] {
  override def stream(
      args: List[String],
      requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] = {

    val userRepo = PostgresUserRepository(Db.xa)
    val quizRepo = PostgresQuizRepository(Db.xa)

    val userService = UserEndpoints(userRepo).service
    val quizService = QuizEndpoints(quizRepo).service

    BlazeBuilder[IO]
      .mountService(userService, "/users")
      .mountService(quizService, "/quizes")
      .serve
  }
}
