package quiz.users

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.Credentials
import cats.data._
import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import cats.syntax._
import cats.instances.future._
import doobie._
import doobie.implicits._
import doobie.postgres._
import org.mindrot.jbcrypt.BCrypt
import quiz.{Db, Session, Utils}
import quiz.Domain._
import quiz.Errors.{EmailAlreadyExists, Errors, Error, UnspecifiedError}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object UserService extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  // TODO: use blocking dispatcher


  val route =
    post {
      entity(as[User]) { user =>
        UserValidation.validate(user) match {
          case Valid(validatedUser) =>
            onSuccess(addUser(validatedUser).unsafeToFuture()) {
              case Left(error) => complete(StatusCodes.BadRequest, Errors(NonEmptyList.of(error)))
              case Right(userInfo) =>
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
          complete(getUserInfo(id).unsafeToFuture())
        }
      }
    }

  def addUser(user: User): IO[Either[Error, UserInfo]] = {
    val hash = BCrypt.hashpw(user.password, BCrypt.gensalt())
    sql"insert into users (email, name, hashed_password) values (${user.email}, ${user.name}, $hash)"
      .update
      .withUniqueGeneratedKeys[UserInfo]("id", "email", "name")
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => EmailAlreadyExists
        case _ => UnspecifiedError
      }
      .transact(Db.xa)
  }

  def getUserInfo(id: Int): IO[Option[UserInfo]] = {
    sql"select id, email, name from users where id = $id"
      .query[UserInfo]
      .option
      .transact(Db.xa)
  }

  def getUserInfoAndHash(email: String): IO[Option[(UserInfo, String)]]= {
    sql"select id, email, name, hashed_password from users where email = $email"
      .query[(UserInfo, String)]
      .option
      .transact(Db.xa)
  }

}
