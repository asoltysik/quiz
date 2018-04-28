package quiz.quizes

import akka.http.scaladsl.server.Directives
import io.circe.generic.extras.Configuration
import quiz.Session
import quiz.Domain._

object QuizEndpoints extends Directives {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.extras.auto._

  implicit val circeConfig: Configuration = Configuration.default.withDefaults

  // @formatter:off
  val route =
    Session.requireSession { session =>
      post {
        entity(as[Quiz[UserId, FullAnswer]]) { quiz =>
          complete(QuizRepository.addQuiz(quiz).unsafeToFuture())
        }
      } ~
      path(IntNumber) { id =>
        parameter("expanded".as[Boolean]) { expanded =>
          if (expanded) {
            complete(QuizRepository.getExpandedQuiz(id).unsafeToFuture())
          } else {
            complete(QuizRepository.getQuiz(id).unsafeToFuture())
          }
        }
      } ~
      get {
        complete(QuizRepository.getQuizes().unsafeToFuture())
      }
    } ~
    pathPrefix("questions") {
      parameter("quizId".as[Int]) { quizId =>
        get {
          complete(QuizRepository.getQuestionsForQuiz(quizId, withAnswers = false).unsafeToFuture())
        } ~
        post {
          entity(as[List[Question[FullAnswer]]]) { questionList =>
            complete(QuizRepository.addQuestions(quizId, questionList).unsafeToFuture())
          }
        }
      }
    }
  // @formatter:on

}