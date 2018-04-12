package quiz

import cats.data.{EitherT, NonEmptyList}
import io.circe.Json
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.XMLHttpRequest
import quiz.Errors.ApiError
import quiz.Utils.StatusCodeError

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Utils {

  abstract class StatusCodeError(request: XMLHttpRequest) extends Exception

  case class NotFoundError(request: XMLHttpRequest) extends StatusCodeError(request)
  case class BadRequestError(request: XMLHttpRequest) extends StatusCodeError(request)
  case class NotAuthorizedError(request: XMLHttpRequest) extends StatusCodeError(request)
  case class UnknownStatusError(request: XMLHttpRequest) extends StatusCodeError(request)

  def handleStatus(request: XMLHttpRequest): StatusCodeError = request.status match {
    case 400 => new BadRequestError(request)
    case 404 => new NotFoundError(request)
    case 403 =>
      Main.Model.user.value = None
      new NotAuthorizedError(request)
    case _ => new UnknownStatusError(request)
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
          case Failure(AjaxException(req)) => Try(Left(Utils.handleStatus(req)))
        }
    )
  }

  def get(url: String): EitherT[Future, StatusCodeError, XMLHttpRequest] = {
    EitherT (
      Ajax.get(url, headers = jsonHeaders)
        .transform {
          case Success(req) => Try(Right(req))
          case Failure(AjaxException(req)) => Try(Left(Utils.handleStatus(req)))
        }
    )
  }
}
