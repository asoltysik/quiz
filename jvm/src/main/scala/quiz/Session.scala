package quiz

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive0, Directives}
import com.softwaremill.session._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.CsrfDirectives.{
  randomTokenCsrfProtection,
  setNewCsrfToken
}
import com.softwaremill.session.CsrfOptions.checkHeader
import org.mindrot.jbcrypt.BCrypt
import quiz.Domain.{UserCredentials, UserId}
import quiz.users.UserRepository

import scala.util.Try

object Session extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  case class ClientSession(id: UserId)

  object ClientSession {
    implicit def serializer: SessionSerializer[ClientSession, String] =
      new SingleValueSessionSerializer(
        _.id,
        (un: UserId) =>
          Try {
            ClientSession(un)
        }
      )
  }

  val sessionConfig: SessionConfig =
    SessionConfig.default(sys.env("SESSION_SECRET"))
  implicit val sessionManager =
    new SessionManager[ClientSession](sessionConfig)

  val requireSession =
    requiredSession(SessionOptions.oneOff, SessionOptions.usingCookies)

  def setClientSession(session: ClientSession): Directive0 =
    setSession(SessionOptions.oneOff, SessionOptions.usingCookies, session)

  // @formatter:off
  val routes =
    //randomTokenCsrfProtection(checkHeader) {
      path("doLogin") {
        post {
          entity(as[UserCredentials]) { credentials =>
            onSuccess(UserRepository.getUserInfoAndHash(credentials.email).unsafeToFuture()) {
              case Some((info, hash)) if BCrypt.checkpw(credentials.password, hash) =>
                setClientSession(ClientSession(info.id.get)) {
                  complete(info)
                }
              case _ => complete(StatusCodes.NotFound)
            }
          }
        }
      } ~
      path("doCheckSession") {
        requireSession { session =>
          onSuccess(UserRepository.getUserInfo(session.id).unsafeToFuture()) {
            case Some(userInfo) => complete(userInfo)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
      path("doLogout") {
        post {
          requireSession { session =>
            invalidateSession(SessionOptions.oneOff, SessionOptions.usingCookies) { ctx =>
              ctx.complete(StatusCodes.OK)
            }
          }
        }
      }
    //}
  // @formatter:on

}
