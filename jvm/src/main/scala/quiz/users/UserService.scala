package quiz.users

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.Credentials
import cats.effect.IO
import doobie._
import doobie.implicits._
import org.mindrot.jbcrypt.BCrypt
import quiz.{Db, Utils}
import quiz.Domain._

import scala.concurrent.{ExecutionContext, Future}


object UserService extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  // TODO: use blocking dispatcher

  val route =
    post {
      entity(as[User]) { user =>
        complete(addUser(user).unsafeToFuture())
      }
    } ~
    pathPrefix(IntNumber) { id =>
      get {
        complete(getUserInfo(id).unsafeToFuture())
      }
    }

  def addUser(user: User): IO[UserInfo] = {
    val hash = BCrypt.hashpw(user.password, BCrypt.gensalt())
    sql"insert into users (email, name, hashed_password) values (${user.email}, ${user.name}, $hash)"
      .update
      .withUniqueGeneratedKeys[UserInfo]("id", "email", "name")
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
