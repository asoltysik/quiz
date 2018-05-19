package quiz.domain

package object users {

  sealed trait Role
  case object Superuser extends Role
  case object User extends Role

  case class UserId(value: Int) extends AnyVal
  case class Email(value: String) extends AnyVal
  case class Password(value: String) extends AnyVal

  case class UserInfo(email: Email, name: String)
  case class User(id: UserId, info: UserInfo)
  final case class UserCredentials(email: Email, password: Password)
  final case class UserRegistration(info: UserInfo, password: Password)

}
