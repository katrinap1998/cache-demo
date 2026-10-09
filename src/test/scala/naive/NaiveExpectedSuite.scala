package naive

import munit.CatsEffectSuite
import naive.CacheService.Request

class NaiveExpectedSuite extends CatsEffectSuite {
  test("returns the cached response within the TTL") {
    for {
      first  <- CacheService.cacheResponse(Request("someId"))
      second <- CacheService.cacheResponse(Request("someId"))
    } yield assert(first == second)
  }
}
