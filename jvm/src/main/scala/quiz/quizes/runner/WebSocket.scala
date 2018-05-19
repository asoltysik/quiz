package quiz.quizes.runner

/*import akka.actor.Props
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.Flow
import akka.util.{ByteString, Timeout}
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.parser.decode
import io.circe.syntax._
import quiz.Websocket._

import scala.concurrent.duration._
import quiz.Main

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

}*/
