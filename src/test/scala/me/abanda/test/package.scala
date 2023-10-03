package me.abanda


import com.softwaremill.quicklens.ModifyPimp
import me.abanda.config.Config

import scala.concurrent.duration._

package object test {
  val DefaultConfig: Config = Config.read
  val TestConfig: Config = DefaultConfig.modify(_.db.migrateOnStart).setTo(true)
}
