package quiz.domain

import quiz.domain.users.UserId

package object quizes {

  case class QuizId(value: Int) extends AnyVal
  case class QuestionId(value: Int) extends AnyVal
  case class AnswerId(value: Int) extends AnyVal

  case class QuizDuration(seconds: Int) extends AnyVal

  case class QuestionNumber(value: Int) extends AnyVal

  case class Quiz(id: QuizId,
                  name: String,
                  author: UserId,
                  duration: QuizDuration)
  case class Question(id: QuestionId, question: String, number: QuestionNumber)
  class AnswerInfo(val id: AnswerId, val answer: String)
  case class AnswerFull(override val id: AnswerId,
                        override val answer: String,
                        correct: Boolean)
      extends AnswerInfo(id, answer)

}
