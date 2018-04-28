package quiz.users

import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import cats.data._
import cats.data.Validated.{Invalid, Valid}
import quiz.Session
import quiz.Domain._
import quiz.Errors.{EmailAlreadyExists, Errors, UnspecifiedError}

object UserEndpoints extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  // TODO: use blocking dispatcher

  private val exceptionHandler = ExceptionHandler {
    case _: EmailAlreadyExists =>
      complete(
        StatusCodes.BadRequest,
        Errors(NonEmptyList.of(EmailAlreadyExists()))
      )
    case _ =>
      complete(
        StatusCodes.InternalServerError,
        Errors(NonEmptyList.of(UnspecifiedError()))
      )
  }

  // @formatter:off
  val route =
    handleExceptions(exceptionHandler) {
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
    }
  // @formatter:on

}
