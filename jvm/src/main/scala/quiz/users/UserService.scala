package quiz.users

// cats
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.Sync
import quiz.users.UserValidation.ValidationResult

// tsec
import tsec.passwordhashers.jca.SCrypt

// internal
import quiz.domain.users._

class UserService[F[_]: Sync](val repo: UserRepository[F]) {

  def register(registration: UserRegistration): F[ValidationResult[User]] = {
    UserValidation
      .validate(registration)
      .traverse(
        validRegistration =>
          for {
            hash <- SCrypt.hashpw[F](validRegistration.password.value)
            user <- repo.addUser(validRegistration.info, hash)
          } yield user
      )
  }

  def getUser(id: UserId): F[Option[User]] = repo.getUser(id)
}
