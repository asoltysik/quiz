package quiz.quizes

import cats.Monad
import quiz.domain.quizes._

class QuizService[F[_]: Monad](val repo: QuizRepository[F]) {

  def getAllQuizes(): F[List[Quiz]] = ???

}
