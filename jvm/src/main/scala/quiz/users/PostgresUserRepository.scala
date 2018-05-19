package quiz.users

import cats.Monad
import cats.effect.IO
import doobie.implicits._
import doobie.postgres._
import doobie.util.log.LogHandler
import doobie.util.meta.Meta
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.mindrot.jbcrypt.BCrypt
import quiz.Db
import quiz.Errors.{EmailAlreadyExists, UnspecifiedError}
import quiz.domain.users._

trait UserRepository[F[_]] {

  def addUser(user: UserInfo, hash: String): F[User]
  def getUser(id: UserId): F[Option[User]]
  def getUserAndHash(email: Email): F[Option[(User, String)]]
}

case class PostgresUserRepository[F[_]: Monad](xa: Transactor[F])
    extends UserRepository[F] {

  object Statements {
    def addUser(user: UserInfo, hash: String): Update0 =
      sql"insert into users (email, name, hashed_password) values (${user.email}, ${user.name}, $hash)".update

    def getUser(id: UserId): Query0[User] =
      sql"select id, email, name from users where id = ${id.value}"
        .query[User]

    def getUserAndHash(email: Email): Query0[(User, String)] =
      sql"select id, email, name, hashed_password from users where email = ${email.value}"
        .query[(User, String)]

  }

  implicit val doobieLogger: LogHandler = Db.doobieLogger

  def addUser(info: UserInfo, hash: String): F[User] = {
    Statements
      .addUser(info, hash)
      .withUniqueGeneratedKeys[User]("id", "email", "name")
      .exceptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => throw EmailAlreadyExists()
      }
      .transact(xa)
  }

  def getUser(id: UserId): F[Option[User]] = {
    Statements
      .getUser(id)
      .option
      .transact(xa)
  }

  def getUserAndHash(email: Email): F[Option[(User, String)]] = {
    Statements
      .getUserAndHash(email)
      .option
      .transact(xa)
  }
}
