package com.fuel.util.cache

import cats.effect.IO
import java.time.LocalDate
import scala.collection.mutable

trait MemCache[A, B, F[_]] {
  def getOrCache(key: A)(f: A => F[B]): F[B]
}

object DateCache extends MemCache[LocalDate, Double, IO]{

  private val cache = mutable.Map[LocalDate, Double]()

  override def getOrCache(key: LocalDate)(f: LocalDate => IO[Double]): IO[Double] = {
    if(cache.contains(key)) IO.pure(cache(key)) else {
      f(key).map { d =>
        cache += ((key, d))
        d
      }
    }
  }
}
