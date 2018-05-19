package quiz.ui

import cats.implicits._
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{MouseEvent, Node}
import quiz.Domain.{AnswerInfo, Quiz, UserId}
import quiz.{Main, SitePart}
import quiz.services.QuizService

import scala.concurrent.ExecutionContext.Implicits.global

object Home extends SitePart {

  override def link: String = s"#home"

  object Model {

    def refreshQuizes() = {
      QuizService
        .getAllQuizes()
        .map(quizList => {
          quizes.value.clear()
          quizes.value ++= quizList
          quizList
        })
    }

    val quizes = Vars[Quiz]()
    val quizStarter = Var[Option[QuizStarter]](None)
  }

  @dom private def quizDescription(quiz: Quiz): Binding[Node] = {
    val openRunner = { _: MouseEvent =>
      Model.quizStarter.value = Some(new QuizStarter(quiz))
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
    Model.quizStarter.bind match {
      case Some(quizStarter) => quizStarter.render.bind
      case None =>
        if (Model.quizes.bind.isEmpty) {
          Model.refreshQuizes()
        }
        <div class="container row">
          <div class="col-3">
            <button class="btn btn-primary">Add a quiz</button>
          </div>
          <div class="col-9">{renderQuizes.bind}</div>
        </div>
    }
  }

  @dom private def unloggedView: Binding[Node] = {
    <div class="jumbotron">
      <h1 class="display-4">Quizes!</h1>
      <p class="lead">Login to see the quizes.</p>
    </div>
  }

  @dom def render: Binding[Node] = {
    if (Main.Model.user.bind.isDefined) {
      loggedView.bind
    } else {
      unloggedView.bind
    }
  }
}
