package quiz.services

import cats.data.EitherT
import io.circe
import io.circe.{Decoder, Encoder}
import org.scalajs.dom.raw.{Event, MessageEvent}
import quiz.Domain._
import org.scalajs.dom.{WebSocket, window}
import quiz.Websocket._
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Future, Promise}

class QuizWebsocket(quiz: Quiz[UserId, AnswerInfo]) {
  implicit val circeConfig =
    Configuration.default.withDiscriminator("commandType")

  private val host = window.location.host
  private val ws = new WebSocket("ws://" + host + "/ws/quizRunner")

  private def send[T <: Response: Decoder](
      command: Command): EitherT[Future, io.circe.Error, T] = {
    EitherT {
      val p = Promise[Either[io.circe.Error, T]]()
      ws.onmessage = { e: MessageEvent =>
        val response = decode[T](e.data.toString)
        p success response
      }
      ws.send(command.asJson.toString())
      p.future
    }
  }

  def start(user: UserInfo): EitherT[Future, circe.Error, StartResponse] = {
    send[StartResponse](Starting(user.id.get, quiz.id))
  }

  def answer(
      question: Question[AnswerInfo],
      answer: AnswerInfo): EitherT[Future, circe.Error, AnswerResponse] = {
    send[AnswerResponse](Answering(question.id, answer.answer))
  }

  def finish(): EitherT[Future, circe.Error, FinishResponse] = {
    val result = send[FinishResponse](Finishing)
    result.value.onComplete(_ => ws.close())
    result
  }

}
