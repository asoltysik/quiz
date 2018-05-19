package quiz.quizes

import cats.syntax._
import cats.implicits._
import cats._
import cats.instances._
import cats.effect.Effect
import org.http4s._
import org.http4s.dsl.Http4sDsl

case class QuizEndpoints[F[_]: Effect](quizRepository: QuizRepository[F])
    extends Http4sDsl[F] {

  val service: HttpService[F] = HttpService {
    case GET -> Root =>
      quizRepository.getQuizes().flatMap(x => Ok("hi"))
  }
}
