package quiz

import quiz.Domain.{AnswerInfo, Question, UserId}

object Websocket {

  sealed trait Command
  case class Starting(userId: UserId, quizId: Int) extends Command
  case class Answering(questionId: Int, answerText: String) extends Command
  case object Finishing extends Command

  sealed trait Response
  case class AnswerResponse(questionId: Int,
                            answerText: String,
                            correct: Boolean)
      extends Response
  case class StartResponse(questions: List[Question[AnswerInfo]])
      extends Response
  case class FinishResponse(result: Map[String, (String, Boolean)])
      extends Response
  case object WrongCommand extends Response

}
