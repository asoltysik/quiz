package quiz.ui

import com.thoughtworks.binding.{Binding, dom}
import cats.instances.future._
import com.thoughtworks.binding.Binding.Vars
import org.scalajs.dom.{document, html}
import org.scalajs.dom.raw.{MouseEvent, Node}
import quiz.Domain.User
import quiz.Errors.ApiError
import quiz.Utils.BadRequestError
import quiz.{Main, SitePart}
import quiz.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global

object Register extends SitePart {
  def link: String = "#register"

  object Model {
    val errors = Vars[ApiError]()
  }

  @dom def render: Binding[Node] = {
    val keyDownHandler = { event: MouseEvent =>
      val email = document
        .getElementById("inputEmailRegister")
        .asInstanceOf[html.Input]
        .value
      val password = document
        .getElementById("inputPasswordRegister")
        .asInstanceOf[html.Input]
        .value
      val name = document
        .getElementById("inputNameRegister")
        .asInstanceOf[html.Input]
        .value
      UserService
        .register(User(None, email, name, password))
        .map { userInfo =>
          Main.Model.user.value = Some(userInfo)
          Main.route.state.value = Home
          Model.errors.value.clear()
        }
        .leftMap {
          case BadRequestError(req) => {
            val errors = UserService.decodeErrors(req)
            Model.errors.value.clear()
            Model.errors.value ++= errors.toList
          }
        }
    }

    <div>
      <div>
        {
        for(error <- Model.errors) yield
          <div class="alert alert-danger" data:role="alert">{error.toString}</div>
        }
      </div>
      <div class="form-group">
        <label for="inputEmailRegister">Email</label>
        <input type="email" class="form-control" id="inputEmailRegister" placeholder="Email"/>
      </div>
      <div class="form-group">
        <label for="inputPasswordRegister">Password</label>
        <input type="password" class="form-control" id="inputPasswordRegister" placeholder="Password"/>
      </div>
      <div class="form-group">
        <label for="inputNameRegister">Name</label>
        <input type="text" class="form-control" id="inputNameRegister" placeholder="Name"/>
      </div>
      <button type="submit" class="btn btn-primary" onclick={keyDownHandler}>Register</button>
    </div>
  }
}
