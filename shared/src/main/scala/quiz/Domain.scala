package quiz

object Domain {

  type UserId = Int

  final case class UserCredentials(email: String, password: String)
  case class UserInfo(id: Option[Int], email: String, name: String)
  case class User(id: Option[Int], email: String, name: String, password: String)

  final case class Quiz[U](
                            id: Option[Int], name: String, createdBy: U, duration: Int, questions: List[Question] = List.empty)
  final case class Question(id: Option[Int], question: String, number: Int, answers: List[Answer] = List.empty)
  final case class Answer(id: Option[Int], answer: String, correct: Boolean)

}
