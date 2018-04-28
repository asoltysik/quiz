package quiz.users

import cats.data.ValidatedNel
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

  val minPasswordLength = 6
  val maxPasswordLength = 50

  type ValidationResult[A] = ValidatedNel[RegistrationError, A]

  private def validateEmail(email: String): ValidationResult[String] = {
    /* This is just for user convenience, one must be careful with email validation as
     * very.“(),:;<>[]”.VERY.“very@\\ "very”.unusual@strange.example.com
     * is apparently a valid email address */
    """.+@.+""".r.findFirstIn(email) match {
      case Some(validEmail) => email.validNel
      case None => WrongEmailFormat().invalidNel
    }
  }

  private def validateName(name: String): ValidationResult[String] = {
    """[^\p{L} ]+""".r.findAllIn(name).toList match {
      case Nil => name.validNel
      case list => WrongNameCharacters(list.flatten.distinct).invalidNel
    }
  }

  private def validatePassword(password: String): ValidationResult[String] = {
    if (minPasswordLength to maxPasswordLength contains password.length)
      password.validNel
    else
      WrongPasswordLength(password.length,
                          minPasswordLength,
                          maxPasswordLength).invalidNel
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
