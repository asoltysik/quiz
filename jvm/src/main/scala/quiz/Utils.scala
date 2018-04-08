package quiz

import akka.http.scaladsl.server.directives.Credentials
import quiz.users.UserService
import doobie.implicits._

import scala.concurrent.{ExecutionContext, Future}

object Utils {

  implicit val executionContext = ExecutionContext.global

}
