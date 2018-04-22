package quiz.ui

import cats.syntax.option._
import cats.instances.future._
import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{MouseEvent, Node}
import quiz.Domain.{AnswerInfo, Quiz, UserId}
import quiz.Main
import quiz.services.QuizWebsocket

import scala.concurrent.ExecutionContext.Implicits.global

class QuizStarter(quiz: Quiz[UserId, AnswerInfo]) {

  val ws = new QuizWebsocket(quiz)

  object Model {
    val quizRunner = Var[Option[QuizRunner]](None)
    val results = Var(Map.empty[String, (String, Boolean)])
  }

  @dom def render(): Binding[Node] = {
    <div class="container">
      <div class="row">{quiz.name + ", duration: " + (quiz.duration / 60).toString + " minutes"}</div>
      {
      Model.quizRunner.bind match {
        case None => notStarted.bind
        case Some(quizRunner) =>
          if(quizRunner.Model.finished.bind) {
            finish()
            finished.bind
          }
          else {
            quizRunner.render.bind
          }
      }
      }
    </div>
  }

  @dom private def notStarted: Binding[Node] = {
    val startClick = { _: MouseEvent =>
      ws.start(Main.Model.user.value.get)
        .map(response => {
          Model.quizRunner.value =
            new QuizRunner(quiz.copy(questions = response.questions), ws).some
        })
        .leftMap(println(_))
    }

    <div class="row">
      <button class="btn btn-primary" onclick={startClick}>Start</button>
    </div>
  }

  @dom private def finished: Binding[Node] = {
    val results = Model.results.bind
    val finishedDiv = if (results.isEmpty) {
      <div class="row">Finished!</div>
    } else {
      val correct = results.values.count(_._2 == true)
      <div class="row">Correct: {correct.toString + " / " + results.size.toString}</div>
    }
    <div>
      {finishedDiv}
      <button class="btn btn-primary" onclick={returnClick()}>Return</button>
    </div>
  }

  private def finish() = {
    ws.finish()
      .map(response => Model.results.value = response.result)
  }

  private def returnClick() = { _: MouseEvent =>
    Home.Model.quizStarter.value = None
  }
}
