package quiz.users

import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.Credentials
import cats.data._
import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import cats.syntax._
import cats.instances.future._
import doobie._
import quiz.{Db, Session, Utils}
import quiz.Domain._
import quiz.Errors.{EmailAlreadyExists, ApiError, Errors, UnspecifiedError}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object UserService extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  // TODO: use blocking dispatcher

  implicit def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case EmailAlreadyExists =>
      complete(
        StatusCodes.BadRequest,
        Errors(NonEmptyList.of(EmailAlreadyExists))
      )
    case _ =>
      complete(
        StatusCodes.InternalServerError,
        Errors(NonEmptyList.of(UnspecifiedError))
      )
  }

  // @formatter:off
  val route =
    post {
      entity(as[User]) { user =>
        UserValidation.validate(user) match {
          case Valid(validatedUser) =>
            onSuccess(UserRepository.addUser(validatedUser).unsafeToFuture()) { userInfo =>
                Session.setClientSession(Session.ClientSession(userInfo.id.get)) {
                  complete(userInfo)
                }
            }
          case Invalid(errors) => complete(StatusCodes.BadRequest, Errors(errors))
        }
      }
    } ~
    Session.requireSession { session =>
      pathPrefix(IntNumber) { id =>
        get {
          complete(UserRepository.getUserInfo(id).unsafeToFuture())
        }
      }
    }
  // @formatter:on

}
