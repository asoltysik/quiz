package quiz.users

import utest._

import cats.syntax.apply._
import cats.syntax.traverse._
import cats.instances.list._

// internal

import quiz.domain.users._

import UserValidationTests._

object UserServiceTests extends TestSuite {

  val tests = Tests {

    "register" - {
      val service = new UserService(new InMemoryUserRepository)

      "should not add invalid user, accumulating errors" - {
        val invalidInfos = (invalidEmails, invalidNames).mapN(UserInfo)
        val invalidRegistrations =
          (invalidInfos, validPasswords).mapN(UserRegistration)

        val validations =
          invalidRegistrations.map(service.register).sequence.unsafeRunSync()

        validations.foreach { validation =>
          assert(validation.isInvalid)
          assert(validation.toEither.left.get.length == 2)
        }
      }

      "should work for valid user" - {
        val validations =
          validUserRegistrations.map(service.register).sequence.unsafeRunSync()

        validations.foreach { validation =>
          assert(validation.isValid)
        }
      }

    }

  }
}
