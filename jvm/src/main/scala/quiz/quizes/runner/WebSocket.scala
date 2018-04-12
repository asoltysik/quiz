package quiz.quizes.runner

import akka.actor.{PoisonPill, Props}
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.{ByteString, Timeout}
import io.circe.generic.extras.auto._
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import io.circe.parser.decode
import io.circe.syntax._
import quiz.Domain._

import scala.concurrent.duration._
import quiz.{Db, Main}
import quiz.Domain.{UserId, UserInfo}

sealed trait Command
case class Starting(userId: UserId, quizId: Int) extends Command
case class Answering(userId: UserId, questionId: Int, answerText: String)
    extends Command
case class Finishing(userId: UserId, quizId: Int) extends Command

sealed trait Response
case class AnswerResponse(questionId: Int,
                          answerText: String,
                          correct: Boolean)
    extends Response
case object StartResponse extends Response
case class FinishResponse(result: Map[Int, (String, Boolean)]) extends Response

object WebSocket {

  implicit val circeConfig =
    Configuration.default.withDiscriminator("commandType")
  implicit val jsonEntityStreamingSupport = EntityStreamingSupport.json()

  def webSocketService: Flow[Message, Message, Any] = {

    implicit val askTimeout = Timeout(5.seconds)

    val ref = Main.system.actorOf(Props[QuizActor])

    Flow[Message]
      .map {
        case TextMessage.Strict(text) => ByteString(text)
      }
      .via(jsonEntityStreamingSupport.framingDecoder)
      .map(bs => {
        decode[Command](bs.utf8String)
      })
      .map {
        case Left(e) => throw new IllegalArgumentException("Wrong json!")
        case Right(value) => value
      }
      .ask[Response](ref)
      .map(response => TextMessage(response.asJson.toString))
  }

}
