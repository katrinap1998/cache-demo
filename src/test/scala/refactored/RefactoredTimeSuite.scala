package refactored

import cats.Applicative
import cats.effect.{Clock, IO, Ref}
import munit.CatsEffectSuite
import scala.concurrent.duration._

class RefactoredTimeSuite extends CatsEffectSuite {
  private val req = Request("a")

  // a clock the test controls
  def fakeClock(time: Ref[IO, FiniteDuration]): Clock[IO] =
    new Clock[IO] {
      def applicative = Applicative[IO]
      def monotonic = time.get
      def realTime = time.get
    }

  test("expires entries after the TTL") {
    for {
      time    <- Ref.of[IO, FiniteDuration](0.seconds)
      service <- CacheService.create(5.minutes, fakeClock(time))
      first   <- service.cacheResponse(req)
      _       <- time.update(_ + 6.minutes)
      fresh   <- service.cacheResponse(req)
    } yield assert(first != fresh)
  }
}
