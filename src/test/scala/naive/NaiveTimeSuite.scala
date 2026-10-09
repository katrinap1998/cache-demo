package naive

import cats.effect.IO
import munit.CatsEffectSuite
import naive.CacheService.Request
import scala.concurrent.duration._

// Pitfall 1: really sleeps for 1 minute, shorter than the 5 minute TTL, so the assertion fails
class NaiveTimeSuite extends CatsEffectSuite {
  override def munitIOTimeout: Duration = 30.minutes


  test("respects the TTL (FLAKY)") {
    for {
      first  <- CacheService.cacheResponse(Request("someId"))
      _      <- IO.sleep(1.minute)
      second <- CacheService.cacheResponse(Request("someId"))
    } yield assert(first != second)
  }
}
