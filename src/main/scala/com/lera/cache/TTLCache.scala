package com.lera.cache

import com.google.common.cache.{CacheBuilder, Cache => GuavaCache}

import java.util.concurrent.TimeUnit

class Cache[K <: AnyRef, V <: AnyRef](cache: GuavaCache[K, V]) {

  def get(k: K): Option[V] = {
    Option(cache.getIfPresent(k))
  }

  def set(k: K, v: V): Unit = {
    cache.put(k, v)
  }

  def remove(k: K): Unit = {
    cache.invalidate(k)
  }

  def contains(k: K): Boolean = {
    cache.getIfPresent(k) != null
  }
}

/**
 * TTL cache store.
 * TTL duration is provided in application.conf file (maxTtlTime property) in seconds.
 */
object TTLCache {
  def apply[K <: AnyRef, V <: AnyRef](duration: Int): Cache[K, V] = {
    val ttlCache: GuavaCache[K, V] =
      CacheBuilder
        .newBuilder()
        .expireAfterWrite(duration, TimeUnit.SECONDS)
        .build()
    new Cache(ttlCache)
  }
}