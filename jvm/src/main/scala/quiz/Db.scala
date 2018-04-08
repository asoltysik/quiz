package quiz

import cats.effect.IO
import doobie.util.transactor.Transactor

object Db {

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:quizes",
    "postgres",
    "postgres"
  )

}
