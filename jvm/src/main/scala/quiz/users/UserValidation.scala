package quiz.users

import cats.data.ValidatedNel
import cats.data.Validated._
import cats.implicits._
import quiz.Errors.{
  RegistrationError,
  WrongEmailFormat,
  WrongNameCharacters,
  WrongPasswordLength
}
import quiz.domain.users._

object UserValidation {

  val minPasswordLength = 6
  val maxPasswordLength = 50

  type ValidationResult[A] = ValidatedNel[RegistrationError, A]

  private def validateEmail(email: Email): ValidationResult[Email] = {
    /* This is just for user convenience, one must be careful with email validation as
     * very.“(),:;<>[]”.VERY.“very@\\ "very”.unusual@strange.example.com
     * is apparently a valid email address */
    """.+@.+""".r.findFirstIn(email.value) match {
      case Some(validEmail) => email.validNel
      case None             => WrongEmailFormat().invalidNel
    }
  }

  private def validateName(name: String): ValidationResult[String] = {
    """[^\p{L} ]+""".r.findAllIn(name).toList match {
      case Nil  => name.validNel
      case list => WrongNameCharacters(list.flatten.distinct).invalidNel
    }
  }

  private def validatePassword(
      password: Password): ValidationResult[Password] = {
    if (minPasswordLength to maxPasswordLength contains password.value.length)
      password.validNel
    else
      WrongPasswordLength(password.value.length,
                          minPasswordLength,
                          maxPasswordLength).invalidNel
  }

  def validate(user: UserRegistration): ValidationResult[UserRegistration] = {
    val infoValidation: ValidationResult[UserInfo] = (
      validateEmail(user.info.email),
      validateName(user.info.name),
    ).mapN(UserInfo)

    val passwordValidation = validatePassword(user.password)

    (infoValidation, passwordValidation).mapN(UserRegistration)
  }

}
