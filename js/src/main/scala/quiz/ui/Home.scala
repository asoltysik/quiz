package quiz.ui

import cats.implicits._
import cats._
import cats.data._
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{MouseEvent, Node}
import quiz.Domain.{Quiz, UserId}
import quiz.{Main, SitePart}
import quiz.services.{QuizService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global

object Home extends SitePart {

  override def link: String = s"#home"

  object Model {

    def refreshQuizes() = {
      QuizService.getAllQuizes()
        .map(quizList => {
          quizes.value.clear()
          quizes.value ++= quizList
          quizList
        })
    }

    val quizes = Vars[Quiz[UserId]]()
  }

  @dom private def quizDescription(quiz: Quiz[UserId]): Binding[Node] = {
    val openRunner = { event: MouseEvent =>
      QuizRunner.Model.quiz.value = Some(quiz)
      Main.route.state.value = QuizRunner
    }

    <div class="row">
      <div class="col">Name: {quiz.name}</div>
      <div class="col">Duration: {(quiz.duration / 60).toString} minutes</div>
      <div class="col">
        <a class="btn btn-primary" onclick={openRunner}>Start</a>
      </div>
    </div>
  }

  @dom private def renderQuizes: Binding[Node] = {
    <div class="container">
      {
      for (quiz <- Model.quizes) yield {
        quizDescription(quiz).bind
      }
      }
    </div>
  }

  @dom private def loggedView: Binding[Node] = {
    if(Model.quizes.bind.isEmpty) {
      Model.refreshQuizes()
    }
    renderQuizes.bind
  }

  @dom private def unloggedView: Binding[Node] = {
    <div class="jumbotron">
      <h1 class="display-4">Quizes!</h1>
      <p class="lead">Login to see the quizes.</p>
    </div>
  }

  @dom def render: Binding[Node] = {
    if(Main.Model.user.bind.isDefined) {
      loggedView.bind
    } else {
      unloggedView.bind
    }
  }
}
