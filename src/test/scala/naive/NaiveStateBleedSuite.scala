package naive

import munit.CatsEffectSuite
import naive.CacheService.Request

// both tests share CacheService.cacheRef
class NaiveStateBleedSuite extends CatsEffectSuite {
  test("populates the cache") {
    CacheService.cacheResponse(Request("key1"))
      .map(r => assert(r.value.nonEmpty))
  }
  private val msg = "Cache should start empty!"
  test("starts with an empty cache") {
    CacheService.cacheRef.get.map(c => assert(c.isEmpty, msg))
  }
}
