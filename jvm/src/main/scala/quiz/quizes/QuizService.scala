package quiz.quizes

import akka.http.scaladsl.server.Directives
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.postgres._
import cats.implicits._
import io.circe.generic.extras.Configuration
import quiz.{Db, Session}
import quiz.Domain._
import shapeless._



object QuizService extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.extras.auto._

  implicit val circeConfig: Configuration = Configuration.default.withDefaults

  // this is a workaround for not being able to exclude fields from compositing in doobie
  // a little bit ugly but saves a lot of complexity in queries
  implicit val dummyListAnswer: Composite[List[Answer]] = Composite.unitComposite.imap(x => List.empty[Answer])(x => {})
  implicit val dummyListQuestion: Composite[List[Question]] = Composite.unitComposite.imap(x => List.empty[Question])(x => {})

  val route =
    Session.requireSession { session =>
      pathPrefix("quizes") {
        post {
          entity(as[Quiz[UserId]]) { quiz =>
            complete(addQuiz(quiz).transact(Db.xa).unsafeToFuture())
          }
        } ~
          path(IntNumber) { id =>
            parameter("expanded".as[Boolean]) { expanded =>
              if (expanded) {
                complete(getExpandedQuiz(id).transact(Db.xa).unsafeToFuture())
              } else {
                complete(getQuiz(id).transact(Db.xa).unsafeToFuture())
              }
            }
          } ~
          get {
            complete(getQuizes().transact(Db.xa).unsafeToFuture())
          }
      } ~
      pathPrefix("questions") {
        parameter("quizId".as[Int]) { quizId =>
          get {
            complete(getQuestionsForQuiz(quizId, false).transact(Db.xa).unsafeToFuture())
          } ~
            post {
              entity(as[List[Question]]) { questionList =>
                complete(addQuestions(quizId, questionList).transact(Db.xa).unsafeToFuture())
              }
            }
        }
      }
    }

  def getExpandedQuiz(id: Int): ConnectionIO[Option[Quiz[UserId]]] = {
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
  }

  def getQuiz(id: Int): ConnectionIO[Option[Quiz[UserId]]] = {
    sql"select id, name, created_by, duration from quizes where id = $id"
      .query[Quiz[UserId]]
      .option
  }

  def getQuizes(): ConnectionIO[List[Quiz[UserId]]] = {
    sql"select id, name, created_by, duration from quizes"
      .query[Quiz[UserId]]
      .to[List]
  }

  def addQuiz(quiz: Quiz[UserId]): ConnectionIO[Quiz[UserId]] = {
    sql"insert into quizes (name, created_by, duration) values (${quiz.name}, ${quiz.createdBy}, ${quiz.duration})"
      .update
      .withUniqueGeneratedKeys[Quiz[UserId]]("id", "name", "created_by")
  }

  def getQuestionsForQuiz(quizId: Int, withAnswers: Boolean): ConnectionIO[List[Question]] = {
    // TODO: withAnswers
    sql"select id, question, question_no from questions where quiz_id = $quizId"
      .query[Question]
      .to[List]
  }

  def addQuestions(quizId: Int, questions: List[Question]): ConnectionIO[Int] = {
    val sql = "insert into questions (quiz_id, question, question_no) values (?, ?, ?)"
    Update[(Int, Question)](sql).updateMany(
      questions.map(question => (quizId, question))
    )
  }

}
