package quiz

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import quiz.quizes.QuizService
import quiz.quizes.runner.WebSocket
import quiz.users.UserService

import scala.io.StdIn

object Main extends App {

  implicit val system = ActorSystem("quiz")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  // @formatter:off
  val route =
    path("ws" / "quizRunner") {
      get {
        handleWebSocketMessages(WebSocket.webSocketService)
      }
    } ~
    rejectEmptyResponse {
      pathPrefix("quizes") {
        QuizService.route
      } ~
      pathPrefix("users") {
        UserService.route
      }
    } ~
    pathPrefix("session") {
      Session.routes
    } ~
    pathSingleSlash {
      getFromResource("index.html")
    } ~
    path("quiz-fastopt.js") {
      getFromResource("quiz-fastopt.js")
    } ~
    getFromResourceDirectory("")
  // @formatter:on

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println("Server is running on localhost:8080, type stop to stop.")
  while (StdIn.readLine() != "stop") {}
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
