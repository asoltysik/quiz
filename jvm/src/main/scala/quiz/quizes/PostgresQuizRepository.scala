package quiz.quizes

import cats.Monad
import cats.effect.{Effect, IO}
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import quiz.Db
import quiz.domain.quizes._

trait QuizRepository[F[_]] {

  def getQuizes(): F[List[Quiz]]
  def getQuiz(id: Int): F[Option[Quiz]]
  def addQuiz(quiz: Quiz): F[Quiz]

}

case class PostgresQuizRepository[F[_]: Monad](xa: Transactor[F])
    extends QuizRepository[F] {

  object Statements {

    def getQuizes(): Query0[Quiz] =
      sql"select id, name, created_by, duration from quizes"
        .query[Quiz]

    def getQuiz(quizId: Int): Query0[Quiz] =
      sql"select id, name, created_by, duration from quizes where id = $quizId"
        .query[Quiz]

    def getQuestionsForQuiz(quizId: Int): Query0[(Question, AnswerFull)] =
      sql"""select q.id, q.question, q.question_no, a.id, a.answer, a.is_correct
            from questions q inner join answers a on q.id = a.question_id
            where q.quiz_id = $quizId"""
        .query[(Question, AnswerFull)]
  }

  implicit val doobieLogger: LogHandler = Db.doobieLogger

  def getQuiz(id: Int): F[Option[Quiz]] = {
    Statements
      .getQuiz(id)
      .option
      .transact(xa)
  }

  def getQuizes(): F[List[Quiz]] = {
    Statements
      .getQuizes()
      .to[List]
      .transact(xa)
  }

  def addQuiz(quiz: Quiz): F[Quiz] = {
    sql"insert into quizes (name, created_by, duration) values (${quiz.name}, ${quiz.author}, ${quiz.duration})".update
      .withUniqueGeneratedKeys[Quiz]("id", "name", "created_by")
      .transact(xa)
  }

}
