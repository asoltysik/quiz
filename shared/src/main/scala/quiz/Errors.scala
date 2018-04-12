package quiz

import cats.data.NonEmptyList
import quiz.Errors.ApiError

object Errors {

  case class Errors(errors: NonEmptyList[ApiError])

  sealed trait ApiError extends Throwable {
    override def toString: String = "Unexpected error happened"
  }

  sealed trait RegistrationError extends ApiError
  case class WrongEmailFormat() extends RegistrationError {
    override def toString: String = "Wrong email format"
  }
  case class WrongNameCharacters(wrongCharacters: List[Char])
      extends RegistrationError {
    override def toString: String =
      s"Invalid characters in name: ${wrongCharacters mkString ", "}"
  }
  case class WrongPasswordLength(is: Int, min: Int, max: Int)
      extends RegistrationError {
    override def toString: String =
      s"Password length should be between $min and $max characters"
  }
  case class EmailAlreadyExists() extends RegistrationError {
    override def toString: String = "Provided email already exists"
  }

  case class UnspecifiedError() extends ApiError
  case class JsonParsingError() extends ApiError
}
