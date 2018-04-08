package quiz

import cats.data.EitherT
import io.circe.Json
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import quiz.Utils.StatusCodeError

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Utils {

  abstract class StatusCodeError extends Exception

  class NotFoundError extends StatusCodeError
  class NotAuthorizedError extends StatusCodeError
  class UnknownStatusError extends StatusCodeError

  def handleStatus(statusCode: Int): StatusCodeError = statusCode match {
    case 400 => new NotFoundError()
    case 403 => {
      Main.Model.user.value = None
      new NotAuthorizedError()
    }
    case _ => new UnknownStatusError()
  }
}

object Request {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val jsonHeaders = Map("Content-type" -> "application/json")

  def post(url: String, body: Json = Json.Null): EitherT[Future, StatusCodeError, XMLHttpRequest] = {
    EitherT (
      Ajax.post(url, body.toString(), headers = jsonHeaders)
        .transform {
          case Success(req) => Try(Right(req))
          case Failure(AjaxException(req)) => Try(Left(Utils.handleStatus(req.status)))
        }
    )
  }

  def get(url: String): EitherT[Future, StatusCodeError, XMLHttpRequest] = {
    EitherT (
      Ajax.get(url, headers = jsonHeaders)
        .transform {
          case Success(req) => Try(Right(req))
          case Failure(AjaxException(req)) => Try(Left(Utils.handleStatus(req.status)))
        }
    )
  }
}
