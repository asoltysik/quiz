package quiz.services

import cats.data.EitherT
import org.scalajs.dom.raw.XMLHttpRequest
import io.circe.parser.decode
import cats.implicits._
import io.circe.generic.auto._
import quiz.Domain.{AnswerInfo, Quiz, UserId}
import quiz.Utils.StatusCodeError
import quiz.{Request, Utils}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object QuizService {

  def getAllQuizes(): EitherT[Future, Exception, List[Quiz]] = {
    Request
      .get("/quizes")
      .subflatMap(req => decode[List[Quiz]](req.responseText))
  }

}
