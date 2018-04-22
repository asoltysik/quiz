package quiz.ui

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.Var
import cats.instances.future._
import io.circe.generic.extras.Configuration
import org.scalajs.dom.raw.{Event, MouseEvent, Node}
import quiz.Domain._
import quiz.Main
import quiz.Websocket.{AnswerResponse, Answering, StartResponse, Starting}
import quiz.services.QuizWebsocket

import scala.concurrent.ExecutionContext.Implicits.global

class QuizRunner(quiz: Quiz[UserId, AnswerInfo], ws: QuizWebsocket) {

  val numberOfQuestions = quiz.questions.length

  object Model {
    sealed trait AnswerResult
    case object Unanswered extends AnswerResult
    case class Correct(answer: AnswerInfo) extends AnswerResult
    case class Incorrect(answer: AnswerInfo) extends AnswerResult

    val currentNumber = Var(1)
    val finished = Var(false)
    val quizState = Var[Map[Question[AnswerInfo], AnswerResult]](
      quiz.questions.map(_ -> Unanswered).toMap
    )
  }
  @dom private def answerLabel(question: Question[AnswerInfo],
                               answerInfo: AnswerInfo): Binding[Node] = {
    Model.quizState.bind(question) match {
      case Model.Correct(answer) if answer == answerInfo =>
        <span style="color:green">Correct!</span>
      case Model.Incorrect(answer) if answer == answerInfo =>
        <span style="color:red">Incorect</span>
      case _ => <span></span>
    }
  }

  @dom private def question(questionNumber: Int): Binding[Node] = {
    val chosenAnswer = Var[Option[AnswerInfo]](None)

    val currentQuestion =
      Model.quizState.bind.keys.find(_.number == questionNumber).get

    def changed(answerInfo: AnswerInfo) = { _: Event =>
      chosenAnswer.value = Some(answerInfo)
    }

    val confirmClick = { _: MouseEvent =>
      chosenAnswer.value.foreach { answerInfo =>
        ws.answer(currentQuestion, answerInfo)
          .map { answerResponse =>
            val state = if (answerResponse.correct) {
              Model.Correct(answerInfo)
            } else {
              Model.Incorrect(answerInfo)
            }
            Model.quizState.value =
              Model.quizState.value.updated(currentQuestion, state)
          }
      }
    }

    <div class="row">
      <div class="row">Question: {currentQuestion.question}</div>
      <div class="row container">
      {
      import scalaz.std.list._
      for(answerInfo <- currentQuestion.answers) yield {
        val id = s"answer_${answerInfo.id}"
        <div class="form-check">
          <input class="form-check-input" type="radio" name="answerRadio" value={id} id={id}
                 onchange={changed(answerInfo)} />
          <label class="form-check-label" for={id}>{answerInfo.answer} {answerLabel(currentQuestion, answerInfo).bind}</label>
        </div>
      }
      }
      </div>
      <div class="row">
        {
        Model.quizState.bind(currentQuestion) match {
          case Model.Unanswered =>
            <button class="btn btn-primary" onclick={confirmClick}>Confirm</button>
          case _ => <button class="btn btn-primary" onclick={nextClick}>Next question</button>
        }
        }
      </div>
    </div>

  }

  @dom def render: Binding[Node] = {
    <div class="row container">
      {
      if(!Model.finished.bind) question(Model.currentNumber.bind).bind
      else <span />
      }
    </div>
  }

  private def nextClick = { _: MouseEvent =>
    val newNumber = Model.currentNumber.value + 1
    if (newNumber > numberOfQuestions) {
      Model.finished.value = true
    }
    Model.currentNumber.value = newNumber
  }

}
