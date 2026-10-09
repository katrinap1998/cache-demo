package refactored

import cats.effect.IO
import cats.syntax.parallel._
import munit.CatsEffectSuite
import scala.concurrent.duration._

class RefactoredConcurrentSuite extends CatsEffectSuite {
  private val req = Request("k")

  test("concurrent misses compute once") {
    for {
      service <- CacheService.create(5.minutes)
      call = service.cacheResponse(req)
      pair <- (call, call).parTupled
    } yield assert(pair._1 == pair._2)
  }
}
