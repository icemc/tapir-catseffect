package me.abanda.config

import me.abanda.version.BuildInfo
import com.typesafe.scalalogging.StrictLogging
import me.abanda.email.EmailConfig
import me.abanda.http.HttpConfig
import me.abanda.infrastructure.DBConfig
import me.abanda.passwordreset.PasswordResetConfig
import me.abanda.user.UserConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.collection.immutable.TreeMap

/** Maps to the `application.conf` file. Configuration for all modules of the application. */
case class Config(db: DBConfig, api: HttpConfig, email: EmailConfig, passwordReset: PasswordResetConfig, user: UserConfig)

object Config extends StrictLogging {
  def log(config: Config): Unit = {
    val baseInfo = s"""
                      |Bootzooka configuration:
                      |-----------------------
                      |DB:             ${config.db}
                      |API:            ${config.api}
                      |Email:          ${config.email}
                      |Password reset: ${config.passwordReset}
                      |User:           ${config.user}
                      |
                      |Build & env info:
                      |-----------------
                      |""".stripMargin

    val info = TreeMap(BuildInfo.toMap.toSeq: _*).foldLeft(baseInfo) { case (str, (k, v)) =>
      str + s"$k: $v\n"
    }

    logger.info(info)
  }

  def read: Config = ConfigSource.default.loadOrThrow[Config]
}
