package quiz.users

import utest._

import quiz.Db
import quiz.utils.DoobieTestUtils
import quiz.domain.users._
import quiz.users.UserValidationTests.validUserInfos

object PostgresTests extends TestSuite with DoobieTestUtils {

  val xa = Db.xa
  val statements = PostgresUserRepository(xa).Statements

  val tests = Tests {
    "addUser" - {
      checkSql(statements.addUser(validUserInfos.head, "foo"), xa)
    }

    "getUserAndHash" - {
      checkSql(statements.getUserAndHash(Email("abc@foo.pl")), xa)
    }

    "getUser" - {
      checkSql(statements.getUser(UserId(1)), xa)
    }
  }
}
