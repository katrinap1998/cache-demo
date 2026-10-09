package refactored

import cats.effect.{Clock, IO, Ref, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._
import scala.concurrent.duration._

final case class Request(id: String)
final case class Response(value: String)

final case class CacheEntry(
  result: IO[Response],
  cachedAt: FiniteDuration
)

final class CacheService private (
  cache: Ref[IO, Map[String, CacheEntry]],
  clock: Clock[IO],
  ttl: FiniteDuration
) {
  def cacheResponse(req: Request): IO[Response] =
    for {
      now  <- clock.monotonic
      memo <- compute(req).memoize
      result <- cache.modify { entries =>
        entries.get(req.id) match {
          case Some(e) if now - e.cachedAt < ttl =>
            (entries, e.result)
          case _ =>
            (entries.updated(req.id, CacheEntry(memo, now)), memo)
        }
      }
      response <- result.onError { case _ =>
        cache.update(_ - req.id)
      }
    } yield response

  // inside CacheService: drop entries past their TTL
  def evictExpired: IO[Unit] =
    clock.monotonic.flatMap { now =>
      cache.update(_.filter { case (_, e) =>
        now - e.cachedAt < ttl
      })
    }

  private def compute(req: Request): IO[Response] =
    IO(Response(s"computed-${req.id}-${System.nanoTime()}"))
}

object CacheService {
  def create(
    ttl: FiniteDuration,
    clock: Clock[IO] = Clock[IO]
  ): IO[CacheService] =
    Ref.of[IO, Map[String, CacheEntry]](Map.empty)
      .map(new CacheService(_, clock, ttl))

  // run it in the background for the life of the app
  def sweep(service: CacheService): Resource[IO, Unit] =
    (IO.sleep(1.minute) *> service.evictExpired)
      .foreverM
      .background
      .void
}
