package quiz.quizes.runner

import java.time.LocalDateTime

import akka.actor.Actor
import quiz.Domain._
import quiz.quizes.QuizRepository
import quiz.Domain.UserId

import quiz.Websocket._

case class Uninitialized()
case class QuizData(quiz: Quiz[UserId, FullAnswer], timeStarted: LocalDateTime)

class QuizActor extends Actor {
  def receive: Receive =
    active(Uninitialized(), Map.empty[Question[FullAnswer], (String, Boolean)])

  def active(
      quizState: Uninitialized,
      answeredMap: Map[Question[FullAnswer], (String, Boolean)]): Receive = {
    case Starting(userId, quizId) =>
      val quizOpt = QuizRepository.getExpandedQuiz(quizId).unsafeRunSync()
      val quiz = quizOpt.getOrElse {
        throw new IllegalArgumentException("No quiz exists with this id")
      }
      val answersToSend =
        quiz.questions.map(q =>
          q.copy(answers = q.answers.map(_.toAnswerInfo)))
      sender() ! StartResponse(answersToSend)
      context become active(
        QuizData(quiz, LocalDateTime.now()),
        Map.empty[Question[FullAnswer], (String, Boolean)]
      )
    case _ => sender() ! WrongCommand
  }

  def active(
      quizState: QuizData,
      answeredMap: Map[Question[FullAnswer], (String, Boolean)]): Receive = {
    case Answering(questionId, answerText) =>
      val question = quizState.quiz.questions.find(_.id == questionId).get
      val answer = question.answers.find(_.answer == answerText)
      val correct = answer.exists(_.correct)
      sender() ! AnswerResponse(questionId, answerText, correct)
      context become active(
        quizState,
        answeredMap.updated(question, (answerText, correct))
      )
    case Finishing =>
      val results = getFinalResults(quizState, answeredMap)
      sender() ! FinishResponse(results)
    case _ => sender() ! WrongCommand
  }

  private def getFinalResults(
      quizState: QuizData,
      answeredMap: Map[Question[FullAnswer], (String, Boolean)]
  ) = {
    val unanswered = quizState.quiz.questions
      .diff(answeredMap.keys.toList)
      .map(question => question.question -> ("", false))
      .toMap
    unanswered ++ answeredMap.map {
      case (question, value) => (question.question, value)
    }
  }
}
