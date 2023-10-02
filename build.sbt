import sbtbuildinfo.BuildInfoKeys.{ buildInfoKeys, buildInfoOptions, buildInfoPackage }
import sbtbuildinfo.{ BuildInfoKey, BuildInfoOption }

import sbt._
import Keys._

val doobieVersion      = "1.0.0-RC4"
val http4sVersion      = "0.23.23"
val http4sBlazeVersion = "0.23.15"
val circeVersion       = "0.14.6"
val password4jVersion  = "1.7.3"
val sttpVersion        = "3.9.0"
val prometheusVersion  = "0.16.0"
val tapirVersion       = "1.7.5"
val macwireVersion     = "2.5.9"

val dbDependencies = Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.flywaydb"  % "flyway-core"     % "9.22.1"
)

val httpDependencies = Seq(
  "org.http4s"                    %% "http4s-dsl"                    % http4sVersion,
  "org.http4s"                    %% "http4s-blaze-server"           % http4sBlazeVersion,
  "org.http4s"                    %% "http4s-blaze-client"           % http4sBlazeVersion,
  "org.http4s"                    %% "http4s-circe"                  % http4sVersion,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "slf4j-backend"                 % sttpVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"           % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"        % tapirVersion
)

val monitoringDependencies = Seq(
  "io.prometheus"                  % "simpleclient"             % prometheusVersion,
  "io.prometheus"                  % "simpleclient_hotspot"     % prometheusVersion,
  "com.softwaremill.sttp.client3" %% "prometheus-backend"       % sttpVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % tapirVersion
)

val jsonDependencies = Seq(
  "io.circe"                      %% "circe-core"       % circeVersion,
  "io.circe"                      %% "circe-generic"    % circeVersion,
  "io.circe"                      %% "circe-parser"     % circeVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.client3" %% "circe"            % sttpVersion
)

val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.5",
  "ch.qos.logback"              % "logback-classic"          % "1.4.11",
  "org.codehaus.janino"         % "janino"                   % "3.1.10" % Runtime,
  "net.logstash.logback"        % "logstash-logback-encoder" % "7.4"    % Runtime
)

val configDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % "0.17.4"
)

val baseDependencies = Seq(
  "org.typelevel"              %% "cats-effect" % "3.5.2",
  "com.softwaremill.common"    %% "tagging"     % "2.3.4",
  "com.softwaremill.quicklens" %% "quicklens"   % "1.9.6"
)

val apiDocsDependencies = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
)

val securityDependencies = Seq(
  "com.password4j" % "password4j" % password4jVersion
)

val emailDependencies = Seq(
  "com.sun.mail" % "javax.mail" % "1.6.2" exclude ("javax.activation", "activation")
)

val scalatest = "org.scalatest" %% "scalatest" % "3.2.17" % Test
val macwireDependencies = Seq(
  "com.softwaremill.macwire" %% "macrosautocats" % macwireVersion
).map(_ % Provided)

val unitTestingStack = Seq(scalatest)

val embeddedPostgres = "com.opentable.components" % "otj-pg-embedded" % "1.0.2" % Test
val dbTestingStack   = Seq(embeddedPostgres)

val commonDependencies =
  baseDependencies ++ unitTestingStack ++ loggingDependencies ++ configDependencies

lazy val buildInfo = Seq(
  buildInfoPackage             := "me.abanda.version",
  buildInfoObject              := "BuildInfo",
  buildInfoKeys                := Seq[BuildInfoKey](scalaVersion, sbtVersion),
  buildInfoKeys += "name"      -> "http4s-macwire",
  buildInfoKeys += "version"   -> "0.0.1",
  buildInfoKeys += "gitCommit" -> git.gitHeadCommit.value.getOrElse("Not Set"),
  buildInfoKeys += "gitBranch" -> git.gitCurrentBranch.value,
//  buildInfoOptions += BuildInfoOption.ToJson,
//  buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val commonSettings = Seq(
  organization := "me.abanda",
  scalaVersion := "2.13.12",
  libraryDependencies ++= commonDependencies
)

lazy val root = (project in file("."))
  .settings(
    name := "http4s-macwire",
    libraryDependencies ++= dbDependencies ++ httpDependencies ++ jsonDependencies ++ apiDocsDependencies ++ monitoringDependencies ++ dbTestingStack ++ securityDependencies ++ emailDependencies ++ macwireDependencies
  )
  .settings(commonSettings, buildInfo)
