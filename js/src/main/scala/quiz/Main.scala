package quiz

import cats.implicits._
import com.thoughtworks.binding.Binding.{Var, Vars}
import com.thoughtworks.binding.{Binding, Route, dom}
import org.scalajs.dom.document
import org.scalajs.dom.raw.{MouseEvent, Node}
import org.scalajs.dom.window.location
import quiz.Domain.UserInfo
import quiz.services.UserService
import quiz.ui.{Home, Login, QuizModal}

import scala.concurrent.ExecutionContext.Implicits.global

trait SitePart {
  def link: String
  def render: Binding[Node]
}

object Main extends {

  object Model {
    val user = Var[Option[UserInfo]](None)
  }

  def isLogged: Boolean = Model.user.value.isDefined

  UserService.checkSession().map(userInfo => {
    Model.user.value = Some(userInfo)
  })

  val defaultPart = Home

  val parts = Seq(
    Login,
    defaultPart
  )

  val route: Route.Hash[SitePart] = Route.Hash[SitePart](defaultPart)(
    new Route.Format[SitePart] {
      override def unapply(hashText: String): Option[SitePart] =
        Some(parts.find(_.link == hashText).getOrElse(defaultPart))
      override def apply(state: SitePart): String = state.link
    }
  )

  val part: Var[SitePart] = route.state

  route.watch()

  @dom def boot: Binding[Node] = {
    <div class="container">
      <div class="row">
        { header.bind }
      </div>

      <div class="row">
        { part.bind.render.bind }
      </div>
    </div>
  }

  @dom def header: Binding[Node] = {
    Model.user.bind match {
      case Some(user) => headerLogged.bind
      case None => <a class="btn" href={Login.link}>Login</a>
    }
  }

  @dom def headerLogged: Binding[Node] = {
    val logoutClick = { event: MouseEvent =>
      UserService.logout()
        .map(req => {
          location.reload(false)
        })
    }

    <div>
      <a class="btn btn-success">Profile</a>
      <a class="btn btn-primary" onclick={logoutClick}>Logout</a>
    </div>
  }

  def main(args: Array[String]): Unit = {
    dom.render(document.body, boot)
  }
}
