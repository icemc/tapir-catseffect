package me.abanda.util

import me.abanda.config.Config

trait BaseModule {
  def idGenerator: IdGenerator
  def clock: Clock
  def config: Config
}
