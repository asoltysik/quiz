package quiz.users

import cats.effect.IO
import doobie.implicits._
import doobie.postgres._
import org.mindrot.jbcrypt.BCrypt
import quiz.Db
import quiz.Domain.{User, UserInfo}
import quiz.Errors.{EmailAlreadyExists, UnspecifiedError}

object UserRepository {

  def addUser(user: User): IO[UserInfo] = {
    val hash = BCrypt.hashpw(user.password, BCrypt.gensalt())
    sql"insert into users (email, name, hashed_password) values (${user.email}, ${user.name}, $hash)".update
      .withUniqueGeneratedKeys[UserInfo]("id", "email", "name")
      .exceptSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => throw EmailAlreadyExists()
        case _ => throw UnspecifiedError()
      }
      .transact(Db.xa)
  }

  def getUserInfo(id: Int): IO[Option[UserInfo]] = {
    sql"select id, email, name from users where id = $id"
      .query[UserInfo]
      .option
      .transact(Db.xa)
  }

  def getUserInfoAndHash(email: String): IO[Option[(UserInfo, String)]] = {
    sql"select id, email, name, hashed_password from users where email = $email"
      .query[(UserInfo, String)]
      .option
      .transact(Db.xa)
  }
}
