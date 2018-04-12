package quiz

import cats.data.NonEmptyList
import quiz.Errors.Error

object Errors {

  case class Errors(errors: NonEmptyList[Error])

  sealed trait Error extends Throwable

  sealed trait RegistrationError extends Error
  case object WrongEmailFormat extends RegistrationError
  case class WrongNameCharacters(wrongCharacters: List[Char]) extends RegistrationError
  case class WrongPasswordLength(is: Int, min: Int, max: Int) extends RegistrationError
  case object EmailAlreadyExists extends RegistrationError

  case object UnspecifiedError extends Error
  case object JsonParsingError extends Error
}
