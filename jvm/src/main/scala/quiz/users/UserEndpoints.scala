package quiz.users

import cats.effect.{Effect, IO}
import org.http4s._
import org.http4s.dsl.io._

case class UserEndpoints[F[_]: Effect](userRepository: UserRepository[F]) {

  val service: HttpService[IO] = HttpService {
    case GET -> Root / "users" => Ok()
  }
}
