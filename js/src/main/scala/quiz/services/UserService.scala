package quiz.services

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64

import quiz.Domain.{User, UserCredentials, UserInfo}
import io.circe.Error
import io.circe.syntax._
import io.circe.parser.decode
import io.circe.generic.auto._
import org.scalajs.dom.ext.{Ajax, AjaxException}
import cats.implicits._
import cats.data._
import cats.implicits._
import org.scalajs.dom.raw.XMLHttpRequest
import quiz.Utils.StatusCodeError
import quiz.{Request, Utils}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object UserService {

  def credentialsToBase64(email: String, pass: String): String = {
    val bytes = (email + ":" + pass).getBytes(StandardCharsets.UTF_8)
    Base64.getEncoder.encodeToString(bytes)
  }

  implicit val executionContext = ExecutionContext.global

  def register(user: User): EitherT[Future, StatusCodeError, XMLHttpRequest] = {
    Request.post("/users", user.asJson)
  }

  def login(credentials: UserCredentials): EitherT[Future, Exception, UserInfo] = {
    Request.post("/session/doLogin", credentials.asJson)
      .subflatMap(req => decode[UserInfo](req.responseText))
  }

  def checkSession(): EitherT[Future, Exception, UserInfo] = {
    Request.get("/session/doCheckSession")
      .subflatMap(req => decode[UserInfo](req.responseText))
  }

  def logout(): EitherT[Future, StatusCodeError, XMLHttpRequest] = {
    Request.post("/session/doLogout")
  }

}
