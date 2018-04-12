package quiz.users

import cats.data.{Validated, ValidatedNel}
import cats.data.Validated._
import cats.implicits._
import quiz.Domain.User
import quiz.Errors.{
  RegistrationError,
  WrongEmailFormat,
  WrongNameCharacters,
  WrongPasswordLength
}

object UserValidation {

  type ValidationResult[A] = ValidatedNel[RegistrationError, A]

  private def validateEmail(email: String): ValidationResult[String] = {
    // simple validation just for user convenience, should be email confirmation in the future
    """(\w+)@([\w\.]+)""".r.findFirstIn(email) match {
      case Some(validEmail) => email.validNel
      case None => WrongEmailFormat.invalidNel
    }
  }

  private def validateName(name: String): ValidationResult[String] = {
    """[^\p{L} ]+""".r.findAllIn(name).toList match {
      case Nil => name.validNel
      case list => WrongNameCharacters(list.flatten.distinct).invalidNel
    }
  }

  private def validatePassword(password: String): ValidationResult[String] = {
    if (6 to 50 contains password.length)
      password.validNel
    else
      WrongPasswordLength(password.length, 6, 50).invalidNel
  }

  def validate(user: User): ValidationResult[User] = {
    (
      Valid(user.id),
      validateEmail(user.email),
      validateName(user.name),
      validatePassword(user.password)
    ).mapN(User)
  }

}
