package quiz.quizes.runner
/*

import java.time.LocalDateTime

import akka.actor.Actor
import quiz.Domain._
import quiz.quizes.QuizRepository
import quiz.Domain.UserId

import quiz.Websocket._

case class Uninitialized()
case class QuizData(quiz: Quiz, timeStarted: LocalDateTime)

sealed trait AnswerResult
case object Unanswered extends AnswerResult
case class Answered(answer: String, correct: Boolean)

class QuizActor extends Actor {
  def receive: Receive =
    active(Uninitialized(), Map.empty[Question[FullAnswer], AnswerResult])

  def active(quizState: Uninitialized,
             answeredMap: Map[Question[FullAnswer], AnswerResult]): Receive = {
    case Starting(userId, quizId) =>
      val quizOpt = QuizRepository.getExpandedQuiz(quizId).unsafeRunSync()
      val (quiz, questions) = quizOpt.getOrElse {
        throw new IllegalArgumentException("No quiz exists with this id")
      }
      val answersToSend =
        questions.map(q => q.copy(answers = q.answers.map(_.toAnswerInfo)))
      sender() ! StartResponse(answersToSend)
      context become active(
        QuizData(quiz, LocalDateTime.now()),
        questions.map(_ -> Unanswered).toMap
      )
    case _ => sender() ! WrongCommand
  }

  def active(quizState: QuizData,
             answeredMap: Map[Question[FullAnswer], AnswerResult]): Receive = {
    case Answering(questionId, answerText) =>
      val question = answeredMap.keys.find(_.id == questionId).get
      val answer = question.answers.find(_.answer == answerText)
      val correct = answer.exists(_.correct)
      sender() ! AnswerResponse(questionId, answerText, correct)
      context become active(
        quizState,
        answeredMap.updated(question, Answered(answerText, correct))
      )
    case Finishing =>
      val results = getFinalResults(quizState, answeredMap)
      sender() ! FinishResponse(results)
    case _ => sender() ! WrongCommand
  }

  private def getFinalResults(
      quizState: QuizData,
      answeredMap: Map[Question[FullAnswer], AnswerResult]) = {
    answeredMap.map {
      case (question, Unanswered) => (question.question, ("", false))
      case (question, Answered(answer, correct)) =>
        (question.question, (answer, correct))
    }
  }
}*/
