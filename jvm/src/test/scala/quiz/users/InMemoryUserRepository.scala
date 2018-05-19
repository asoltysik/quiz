package quiz.users

import cats.effect.IO
import quiz.domain.users._

class InMemoryUserRepository extends UserRepository[IO] {

  val users = scala.collection.mutable.ListBuffer.empty[(User, String)]

  override def addUser(info: UserInfo, hash: String): IO[User] = {
    IO {
      val user = User(UserId(1), info)
      users.append((user, hash))
      user
    }
  }

  override def getUser(id: UserId): IO[Option[User]] =
    IO(users.find { case (u, h) => u.id == id }.map(_._1))

  override def getUserAndHash(email: Email): IO[Option[(User, String)]] =
    IO(users.find { case (u, h) => u.info.email == email })
}
