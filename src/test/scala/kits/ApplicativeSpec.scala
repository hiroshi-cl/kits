package kits

import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

trait ApplicativeSpec[F[_]] extends FunctorSpec[F] {
  implicit val F: Applicative[F]
  implicit val arb: Arbitrary[F[AnyVal]]
  describe("Applicative") {
    it("identity") { fa: F[AnyVal] =>
      F(fa)(F.pure((a: AnyVal) => a)) == fa
    }
    it("composition") {
      check { (fa: F[AnyVal], f: F[AnyVal => AnyVal], g: F[AnyVal => AnyVal]) =>
        F(fa)(F(f)(F(g)(F.pure((f: AnyVal => AnyVal) => (g: AnyVal => AnyVal) => f.compose(g))))) == F(F(fa)(f))(g)
      }
    }
    it("homomorphism") {
      check { (a: AnyVal, f: AnyVal => AnyVal) =>
        F(F.pure(a))(F.pure(f)) == F.pure(f(a))
      }
    }
    it("interchange") {
      check { (a: AnyVal, f: F[AnyVal => AnyVal]) =>
        F(F.pure(a))(f) == F(f)(F.pure((_: AnyVal => AnyVal)(a)))
      }
    }
  }
}