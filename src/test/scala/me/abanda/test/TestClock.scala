package me.abanda.test

import cats.effect.Sync
import com.typesafe.scalalogging.StrictLogging
import me.abanda.util.Clock

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.Duration

class TestClock(nowRef: AtomicReference[Instant]) extends Clock with StrictLogging {

  logger.info(s"New test clock, the time is: ${nowRef.get()}")

  def this(now: Instant) = this(new AtomicReference(now))

  def this() = this(Instant.now())

  def forward(d: Duration): Unit = {
    val newNow = nowRef.get().plus(d.toMillis, ChronoUnit.MILLIS)
    logger.info(s"The time is now $newNow")
    nowRef.set(newNow)
  }

  override def now[F[_]: Sync](): F[Instant] = Sync[F].delay {
    nowRef.get()
  }
}
