name := "fun-chat"

lazy val commons = Seq(
  version := "1.0",
  scalaVersion := "2.12.2"
)

lazy val `fun-chat-common` = (project in file("fun-chat-common"))
  .settings(commons: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka"          %% "akka-http"            % "10.0.9",
      "com.typesafe.akka"          %% "akka-http-spray-json" % "10.0.9",
      "com.typesafe.akka"          %% "akka-http-testkit"    % "10.0.9" % "test",
      "org.scalatest"              %% "scalatest"            % "3.0.1" % "test",
      "com.typesafe.scala-logging" %% "scala-logging"        % "3.5.0",
      "ch.qos.logback"             % "logback-classic"       % "1.2.3",
      "org.scalikejdbc"            %% "scalikejdbc-config"   % "3.0.0",
      "org.scalikejdbc"            %% "scalikejdbc-test"     % "3.0.0" % "test",
      "org.flywaydb"               % "flyway-core"           % "4.2.0",
      "joda-time"                  % "joda-time"             % "2.9.9",
      "org.mockito"                % "mockito-all"           % "1.10.19" % "test"
    )
  )

lazy val `fun-chat-server` = (project in file("fun-chat-server"))
  .dependsOn(`fun-chat-common` % "test->test;compile->compile")
  .settings(commons: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.auth0"       % "java-jwt"     % "3.2.0",
      "org.postgresql"  % "postgresql"   % "9.4-1206-jdbc42",
      "org.scalikejdbc" %% "scalikejdbc" % "3.0.0",
      "com.h2database"  % "h2"           % "1.4.195",
      "commons-pool"    % "commons-pool" % "1.6",
      "commons-dbcp"    % "commons-dbcp" % "1.4"
    )
  )

lazy val `fun-chat-client` = (project in file("fun-chat-client"))
  .dependsOn(`fun-chat-common` % "test->test;compile->compile")
  .settings(commons: _*)
