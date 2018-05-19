package quiz.users

import cats.data.NonEmptyList
import utest._
import cats.syntax.apply._
import cats.instances.list._
import quiz.domain.users._
import quiz.Errors.{WrongEmailFormat, WrongNameCharacters, WrongPasswordLength}

object UserValidationTests extends TestSuite {

  val validEmails =
    List("foo@bar.com",
         "a+b@example.com",
         "_______@example.com",
         "a-b@example",
         "much.“more\\ unusual”@example.com",
         "this\" \"should @ be allowed").map(Email)

  // We want to include on most obvious invalid emails, regex is not comprehensive
  val invalidEmails =
    List("foo", "foo@", "@example.com", " ").map(Email)

  val validNames =
    List("张伟",
         "张秀英",
         "John      Smith",
         "A",
         "Andrzej Sołtysik",
         "Åke      Andersson",
         "Ronaldo Luís Nazário de Lima")
  val invalidNames =
    List("1 D0n't actually kn0w 1f 1t's possible for names t0 have numbers",
         "$@#$@#%")

  val validPasswords =
    List("a" * UserValidation.minPasswordLength,
         "a" * UserValidation.maxPasswordLength).map(Password)
  val invalidPasswords =
    List("", "a" * (UserValidation.maxPasswordLength + 30), "abc").map(Password)

  val validUserInfos = (validEmails, validNames).mapN(UserInfo)
  val validUserRegistrations =
    (validUserInfos, validPasswords).mapN(UserRegistration)

  val tests = Tests {
    "valid users should be validated correctly" - {
      val results = validUserRegistrations.map(UserValidation.validate)
      assert(results.forall(_.isValid))
    }

    "validation of invalid email should contain WrongEmailFormat" - {
      val invalidInfos = (invalidEmails, validNames).mapN(UserInfo)
      val invalidRegistrations =
        (invalidInfos, validPasswords).mapN(UserRegistration)
      val results =
        invalidRegistrations.map(UserValidation.validate(_).toEither)
      assert(results.forall(_ == Left(NonEmptyList.of(WrongEmailFormat()))))
    }

    "validation of invalid name should contain WrongNameCharacters" - {
      val invalidInfos = (validEmails, invalidNames).mapN(UserInfo)
      val invalidRegistrations =
        (invalidInfos, validPasswords).mapN(UserRegistration)
      val results =
        invalidRegistrations.map(UserValidation.validate(_).toEither)

      for (elem <- results) {
        assertMatch(elem) {
          case Left(NonEmptyList(WrongNameCharacters(_), Nil)) =>
        }
      }
    }

    "validation of invalid password should contain WrongPasswordLength" - {
      val invalidRegistrations =
        (validUserInfos, invalidPasswords).mapN(UserRegistration)
      val results =
        invalidRegistrations.map(UserValidation.validate(_).toEither)

      for (elem <- results) {
        assertMatch(elem) {
          case Left(
              NonEmptyList(WrongPasswordLength(
                             _,
                             UserValidation.minPasswordLength,
                             UserValidation.maxPasswordLength),
                           _)) =>
        }
      }
    }

  }
}
