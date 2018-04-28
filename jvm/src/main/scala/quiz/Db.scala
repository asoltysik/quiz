package quiz

import cats.effect.IO
import com.typesafe.scalalogging.Logger
import doobie.util.log.{ExecFailure, LogHandler, ProcessingFailure, Success}
import doobie.util.transactor.Transactor

object Db {

  val xa: Transactor.Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:quizes",
    "postgres",
    "postgres"
  )

  val doobieLogger: LogHandler = {
    val logger = Logger("doobie")
    LogHandler {

      case Success(s, a, e1, e2) => ()

      case ProcessingFailure(s, a, e1, e2, t) =>
        logger.info(s"""Failed Resultset Processing:
             |    ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n   ")}
             |
             | arguments = [${a.mkString(", ")}]
             | elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
             | failure = ${t.getMessage}
           """.stripMargin)

      case ExecFailure(s, a, e1, t) =>
        logger.error(s"""Failed statement execution:
             |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n   ")}
             |
             | arguments = [${a.mkString(", ")}]
             | elapsed = ${e1.toMillis} ms exec (failed)
             | failure = ${t.getMessage}
           """.stripMargin)
    }
  }

}
