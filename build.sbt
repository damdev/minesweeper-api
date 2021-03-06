val Http4sVersion = "0.21.2"
val CirceVersion = "0.13.0"
val Specs2Version = "4.8.3"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.8.8"

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.damdev.minesweeper",
    name := "minesweeper-api",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.0",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "org.specs2"      %% "specs2-core"         % Specs2Version % "test",
      "org.specs2"      %% "specs2-mock"         % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "com.github.pureconfig" %% "pureconfig"    % "0.12.3",
      "com.h2database"  %  "h2"                  % "1.4.200",
      "org.postgresql"  %  "postgresql"          % "42.2.1",
      "org.tpolecat"    %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"    %% "doobie-hikari"       % DoobieVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
)

herokuFatJar in Compile := Some((assemblyOutputPath in assembly).value)
herokuAppName in Compile := "damdev-minesweeper-api"

assemblyMergeStrategy in assembly := {
  case "module-info.class"                                => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val stage = taskKey[sbt.File]("Assembly as stage for heroku")

stage := assembly.value
