package quiz

object Domain {

  type UserId = Int

  final case class UserCredentials(email: String, password: String)
  case class UserInfo(id: Option[Int], email: String, name: String)
  case class User(id: Option[Int],
                  email: String,
                  name: String,
                  password: String)

  final case class Quiz[U, +A <: Answer](id: Int,
                                         name: String,
                                         createdBy: U,
                                         duration: Int,
                                         questions: List[Question[A]] =
                                           List.empty)
  final case class Question[+A <: Answer](id: Int,
                                          question: String,
                                          number: Int,
                                          answers: List[A] = List.empty)
  sealed trait Answer
  final case class FullAnswer(id: Int, answer: String, correct: Boolean)
      extends Answer {
    def toAnswerInfo: AnswerInfo = AnswerInfo(id, answer)
  }
  final case class AnswerInfo(id: Int, answer: String) extends Answer

}
