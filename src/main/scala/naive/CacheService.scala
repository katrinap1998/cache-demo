package naive

import cats.effect.{IO, Ref}
import java.time.{Duration, Instant}

object CacheService {
  final case class Request(id: String)
  final case class Response(value: String)

  private val ttl = Duration.ofMinutes(5)

  // one global Ref, allocated at class-load time
  val cacheRef =
    Ref.unsafe[IO, Map[String, (Response, Instant)]](Map.empty)

  def cacheResponse(request: Request): IO[Response] =
    for {
      now    <- IO(Instant.now())
      cached <- cacheRef.get.map(_.get(request.id))
      response <- cached match {
        case Some((resp, at)) if isValid(at, now) => IO.pure(resp)
        case _ => updateCache(request)
      }
    } yield response

  def updateCache(request: Request): IO[Response] =
    for {
      now      <- IO(Instant.now())
      response <- computeResponse(request)
      entry = (response, now)
      _        <- cacheRef.update(_.updated(request.id, entry))
    } yield response

  private def computeResponse(req: Request): IO[Response] =
    IO(Response(s"computed-${req.id}-${System.nanoTime()}"))

  private def isValid(cacheTime: Instant, now: Instant): Boolean =
    Duration.between(cacheTime, now).compareTo(ttl) < 0
}
