package kits

package spec

import org.scalacheck.Properties

object LastSpec extends Properties("Last") {

  include(MonoidSpec[Monoid.Last[AnyVal]])

}
