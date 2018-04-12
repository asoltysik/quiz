package quiz.quizes.runner

import java.time.LocalDateTime

import akka.actor.Actor
import quiz.Domain._
import quiz.quizes.QuizRepository
import quiz.Domain.UserId

case class Uninitialized()
case class QuizData(quiz: Quiz[UserId], timeStarted: LocalDateTime)

class QuizActor extends Actor {
  def receive: Receive =
    active(Uninitialized(), Map.empty[Question, (String, Boolean)])

  def active(quizState: Uninitialized,
             answeredMap: Map[Question, (String, Boolean)]): Receive = {
    case Starting(userId, quizId) =>
      val quizOpt = QuizRepository.getExpandedQuiz(quizId).unsafeRunSync()
      val quiz = quizOpt.getOrElse {
        throw new IllegalArgumentException("No quiz exists with this id")
      }
      sender() ! StartResponse
      context become active(
        QuizData(quiz, LocalDateTime.now()),
        Map.empty[Question, (String, Boolean)]
      )
  }

  def active(quizState: QuizData,
             answeredMap: Map[Question, (String, Boolean)]): Receive = {
    case Answering(userId, questionId, answerText) =>
      val question = quizState.quiz.questions.find(_.id.get == questionId).get
      val answer = question.answers.find(_.answer == answerText)
      val correct = answer.exists(_.correct)
      sender() ! AnswerResponse(questionId, answerText, correct)
      context become active(
        quizState,
        answeredMap.updated(question, (answerText, correct))
      )
    case Finishing(userId, quizId) =>
      val results = getFinalResults(quizState, answeredMap)
      sender() ! FinishResponse(results)
  }

  private def getFinalResults(
      quizState: QuizData,
      answeredMap: Map[Question, (String, Boolean)]
  ) = {
    val unanswered = quizState.quiz.questions
      .diff(answeredMap.keys.toList)
      .map(question => question.id.get -> ("", false))
      .toMap
    unanswered ++ answeredMap.map {
      case (question, value) => (question.id.get, value)
    }
  }
}
