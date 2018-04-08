package quiz.ui

import com.thoughtworks.binding.{Binding, dom}
import com.thoughtworks.binding.Binding.Var
import org.scalajs.dom.raw.Node
import quiz.Domain.{Quiz, UserId}
import quiz.SitePart

object QuizRunner extends SitePart {
  override def link: String = "#runner"

  object Model {
    val quiz = Var[Option[Quiz[UserId]]](None)
  }

  @dom def render: Binding[Node] = {
    <div class="container">
      { QuizModal.modal(Model.quiz.bind.get).bind }
      <div class="row">Chosen quiz: {Model.quiz.bind.get.name}</div>
      <div class="row">Press start when ready</div>
      <button type="button" class="btn btn-primary" data:data-toggle="modal" data:data-target="#quizModal">
        Start
      </button>
    </div>
  }
}

object QuizModal {

  @dom def modal(quiz: Quiz[UserId]): Binding[Node] = {
    <div class="modal fade" id="quizModal" data:tabindex="-1" data:role="dialog">
      <div class="modal-dialog" data:role="document">
        <div class="modal-content">
          <h5 class="modal-title">{ quiz.name }</h5>
          <button type="button" class="close" data:data-dismiss="modal" data:aria-label="Close">
            <span data:aria-hidden="true">&times;</span>
          </button>
        </div>

        <div class="modal-body">
          <p>Some text</p>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data:data-dismiss="modal">Close</button>
        </div>
      </div>
    </div>
  }

}
