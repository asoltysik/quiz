package quiz.ui

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw.Node
import quiz.Domain.{Quiz, UserId}

object QuizModal {

  object Model {
    val quiz = Var[Option[Quiz[UserId]]](None)
  }

  @dom private def modal(quiz: Quiz[UserId]): Binding[Node] = {
    <div class="modal fade" id="quizModal" data:tabindex="-1" data:role="dialog">
      <div class="modal-dialog" data:role="document">
        <div class="modal-content">
          <h5 class="modal-title">{ quiz.name }</h5>
          <button type="button" class="close" data:data-dismiss="modal" data:aria-label="Close">
            <span data:aria-hidden="true">&times;</span>
          </button>
        </div>

        <div class="modal-body">
          <p>Press start when ready</p>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-primary" data:data-dismiss="modal">Start</button>
          <button type="button" class="btn btn-secondary" data:data-dismiss="modal">Close</button>
        </div>
      </div>
    </div>
  }

  @dom def render: Binding[Node] = {
    Model.quiz.bind match {
      case Some(quiz) => modal(quiz).bind
      case None => <div></div>
    }

  }

}

