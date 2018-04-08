package quiz.ui

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.{MouseEvent, Node}
import org.scalajs.dom.{document, html}
import cats.implicits._
import quiz.Domain.UserCredentials
import quiz.{Main, SitePart}
import quiz.services.UserService

import scala.concurrent.ExecutionContext.Implicits.global

object Login extends SitePart {
  override def link: String = s"#login"

  @dom def render: Binding[Node] = {
    def submit() = {
      val email = document.getElementById("inputEmail").asInstanceOf[html.Input].value
      val password = document.getElementById("inputPassword").asInstanceOf[html.Input].value
      UserService.login(UserCredentials(email, password))
        .map(userInfo => {
          Main.Model.user.value = Some(userInfo)
          Main.route.state.value = Home
        })
        .leftMap(e => println(e))
    }

    val keyDownHandler = { event: MouseEvent =>
      submit()
    }

    <div>
      <div class="form-group">
        <label for="inputLogin">Email</label>
        <input type="email" class="form-control" id="inputEmail" placeholder="Email"/>
      </div>
      <div class="form-group">
        <label for="inputPassword">Password</label>
        <input type="password" class="form-control" id="inputPassword" placeholder="Password"/>
      </div>
      <button type="submit" class="btn btn-primary" onclick={keyDownHandler}>Login</button>
    </div>
  }
}
