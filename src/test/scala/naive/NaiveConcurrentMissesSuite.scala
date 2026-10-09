package naive

import cats.syntax.parallel._
import munit.CatsEffectSuite
import naive.CacheService.Request

// Pitfall 3, and the same test as refactored.RefactoredConcurrentSuite
class NaiveConcurrentMissesSuite extends CatsEffectSuite {
  private val req = Request("k")

  test("concurrent misses compute once") {
    val call = CacheService.cacheResponse(req)
    for {
      pair <- (call, call).parTupled
    } yield assert(pair._1 == pair._2)
  }
}
