package kits.free

import scala.util.{Failure, Success, Try}

sealed abstract class Error[E] {

  type T

  type Member[U] = kits.free.Member[Error[E], U]

}

object Error {

  case class Fail[E](value: E) extends Error[E] { type T = Nothing }

  def run[E] = new Run {
    type Sum[U] = Error[E] :+: U
    type F[A] = Either[E, A]
    def run[U, A](free: Free[Error[E] :+: U, A]): Free[U, Either[E, A]] =
      Free.handleRelay(free)(a => Right(a): Either[E, A]) {
        case Fail(e) => _ => Right(Pure(Left(e)))
      }
  }

  def fail[U, E](value: E)(implicit F: Member[Error[E], U]): Free[U, Nothing] = Free(F.inject[Nothing](Fail(value)))

  def recover[U: Error[E]#Member, E, A](free: Free[U, A])(handle: E => Free[U, A]): Free[U, A] =
    Free.interpose(free)(a => a)((_: Error[E]) match {
      case Fail(e) => _ => Right(handle(e))
    })

  def fromOption[U: Error[Unit]#Member, A](option: Option[A]): Free[U, A] = option.fold(fail(()): Free[U, A])(a => Pure(a))

  def fromEither[U: Error[E]#Member, E, A](either: Either[E, A]): Free[U, A] = either.fold(e => fail(e), a => Pure(a))

  def fromTry[U: Error[Throwable]#Member, A](t: Try[A]): Free[U, A] =
    t match {
      case Success(a) => Pure(a)
      case Failure(e) => fail(e)
    }

}
