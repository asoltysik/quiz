package quiz.quizes

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import quiz.Db
import quiz.Domain._

object QuizRepository {

  // this is a workaround for not being able to exclude fields from compositing in doobie
  // a little bit ugly but saves a lot of complexity in queries
  implicit val dummyListAnswer: Composite[List[Answer]] = Composite.unitComposite.imap(x => List.empty[Answer])(x => {})
  implicit val dummyListQuestion: Composite[List[Question]] = Composite.unitComposite.imap(x => List.empty[Question])(x => {})

  def getExpandedQuiz(id: Int): IO[Option[Quiz[UserId]]] = {
    sql"""select
          quizes.id, quizes.name, quizes.created_by, quizes.duration, q.id, q.question, q.question_no, a.id, a.answer, a.is_correct
          from quizes inner join questions q ON (quizes.id = q.quiz_id and quizes.id = $id)
          inner join answers a ON q.id = a.question_id
       """
      .query[(Quiz[UserId], Question, Answer)]
      .to[List]
      // convert into a map: Quiz -> Question -> Answer
      .map(_.foldMap(e => Map(e._1 -> List((e._2, e._3)))))
      .map(_.mapValues(_.foldMap(e => Map(e._1 -> List(e._2)))))
      // convert the map into the hierarchical structure of Quiz
      .map(_.mapValues(_.map {
      case (question, answers) => question.copy(answers = answers)
    }))
      .map(_.map {
        case (quiz, questions) => quiz.copy(questions = questions.toList)
      })
      .map(_.headOption)
      .transact(Db.xa)
  }

  def getQuiz(id: Int): IO[Option[Quiz[UserId]]] = {
    sql"select id, name, created_by, duration from quizes where id = $id"
      .query[Quiz[UserId]]
      .option
      .transact(Db.xa)
  }

  def getQuizes(): IO[List[Quiz[UserId]]] = {
    sql"select id, name, created_by, duration from quizes"
      .query[Quiz[UserId]]
      .to[List]
      .transact(Db.xa)
  }

  def addQuiz(quiz: Quiz[UserId]): IO[Quiz[UserId]] = {
    sql"insert into quizes (name, created_by, duration) values (${quiz.name}, ${quiz.createdBy}, ${quiz.duration})"
      .update
      .withUniqueGeneratedKeys[Quiz[UserId]]("id", "name", "created_by")
      .transact(Db.xa)
  }

  def getQuestionsForQuiz(quizId: Int, withAnswers: Boolean): IO[List[Question]] = {
    // TODO: withAnswers
    sql"select id, question, question_no from questions where quiz_id = $quizId"
      .query[Question]
      .to[List]
      .transact(Db.xa)
  }

  def addQuestions(quizId: Int, questions: List[Question]): IO[Int] = {
    val sql = "insert into questions (quiz_id, question, question_no) values (?, ?, ?)"
    Update[(Int, Question)](sql).updateMany(
      questions.map(question => (quizId, question))
    ).transact(Db.xa)
  }

}