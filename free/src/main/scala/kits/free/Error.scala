package kits.free

sealed abstract class Error[E] {

  type T

  type Member[U <: Union] = kits.free.Member[Error[E], U]

}

object Error {

  case class Fail[E](value: E) extends Error[E] { type T = Nothing }

  def run[E] = new Run {
    type Sum[U <: Union] = Error[E] :+: U
    type F[A] = Either[E, A]
    def run[U <: Union, A](free: Free[Error[E] :+: U, A]): Free[U, Either[E, A]] =
      Free.handleRelay(free)(a => Right(a): Either[E, A]) {
        case Fail(e) => _ => Right(Pure(Left(e)))
      }
  }

  def fail[U <: Union, E](value: E)(implicit F: Member[Error[E], U]): Free[U, Nothing] = Free(F.inject[Nothing](Fail(value)))

  def recover[U <: Union: Error[E]#Member, E, A](free: Free[U, A])(handle: E => Free[U, A]): Free[U, A] =
    Free.interpose(free)(a => a)((_: Error[E]) match {
      case Fail(e) => _ => Right(handle(e))
    })

}
