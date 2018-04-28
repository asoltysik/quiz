package quiz.users

import cats.data.NonEmptyList
import utest._
import cats.syntax.apply._
import cats.instances.list._
import quiz.Domain.User
import quiz.Errors.{WrongEmailFormat, WrongNameCharacters, WrongPasswordLength}

object UserValidationTests extends TestSuite {

  val validEmails =
    List("foo@bar.com",
         "a+b@example.com",
         "_______@example.com",
         "a-b@example",
         "much.“more\\ unusual”@example.com",
         "this\" \"should @ be allowed")

  // We want to include on most obvious invalid emails, regex is not comprehensive
  val invalidEmails =
    List("foo", "foo@", "@example.com", " ")

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

  val validPasswords = List("a" * UserValidation.minPasswordLength,
                            "a" * UserValidation.maxPasswordLength)
  val invalidPasswords =
    List("", "a" * (UserValidation.maxPasswordLength + 30), "abc")

  val validUsers: List[User] =
    (List(Some(1)), validEmails, validNames, validPasswords).mapN(User)

  val tests = Tests {
    "valid users should be validated correctly" - {
      val results = validUsers.map(UserValidation.validate)
      assert(results.forall(_.isValid))
    }

    "validation of invalid email should contain WrongEmailFormat" - {
      val invalidUsers =
        (List(Some(1)), invalidEmails, validNames, validPasswords).mapN(User)
      val results = invalidUsers.map(UserValidation.validate(_).toEither)
      assert(results.forall(_ == Left(NonEmptyList.of(WrongEmailFormat()))))
    }

    "validation of invalid name should contain WrongNameCharacters" - {
      val invalidUsers =
        (List(Some(1)), validEmails, invalidNames, validPasswords).mapN(User)
      val results = invalidUsers.map(UserValidation.validate(_).toEither)

      for (elem <- results) {
        assertMatch(elem) {
          case Left(NonEmptyList(WrongNameCharacters(_), Nil)) =>
        }
      }
    }

    "validation of invalid password should contain WrongPasswordLength" - {
      val invalidUsers =
        (List(Some(1)), validEmails, validNames, invalidPasswords).mapN(User)
      val results = invalidUsers.map(UserValidation.validate(_).toEither)

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
