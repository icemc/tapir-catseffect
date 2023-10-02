package me.abanda.infrastructure

import me.abanda.config.Sensitive

case class DBConfig(username: String, password: Sensitive, url: String, migrateOnStart: Boolean, driver: String, connectThreadPoolSize: Int)
