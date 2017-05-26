name := "fun-chat"

lazy val commons = Seq(
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val `fun-chat-server` = (project in file("fun-chat-server"))
  .settings(commons: _*)
  .settings(
    libraryDependencies ++= Seq(
      "joda-time"                  % "joda-time"             % "2.9.6",
      "com.typesafe.akka"          %% "akka-http"            % "10.0.0",
      "com.typesafe.akka"          %% "akka-http-core"       % "10.0.0",
      "com.typesafe.akka"          %% "akka-http-spray-json" % "10.0.0",
      "com.typesafe.akka"          %% "akka-http-testkit"    % "10.0.0" % "test",
      "com.auth0"                  % "java-jwt"              % "3.0.2",
      "com.typesafe.scala-logging" %% "scala-logging"        % "3.5.0",
      "org.postgresql"             % "postgresql"            % "9.4-1206-jdbc42",
      "org.scalikejdbc"            %% "scalikejdbc"          % "3.0.0",
      "org.scalikejdbc"            %% "scalikejdbc-test"     % "3.0.0" % "test",
      "org.scalikejdbc"            %% "scalikejdbc-config"   % "3.0.0",
      "com.h2database"             % "h2"                    % "1.4.195",
      "ch.qos.logback"             % "logback-classic"       % "1.2.3",
      "commons-pool"               % "commons-pool"          % "1.6",
      "commons-dbcp"               % "commons-dbcp"          % "1.4",
      "org.flywaydb"               % "flyway-core"           % "4.0.3",
      "org.scalatest"              % "scalatest_2.11"        % "3.0.1" % "test",
      "org.mockito"                % "mockito-all"           % "1.10.19" % "test"
    )
  )

lazy val `fun-chat-client` = (project in file("fun-chat-client"))
  .settings(commons: _*)
  .settings(
    libraryDependencies ++= Seq(
      "joda-time"                  % "joda-time"             % "2.9.6",
      "com.typesafe.akka"          %% "akka-http"            % "10.0.0",
      "com.typesafe.akka"          %% "akka-http-core"       % "10.0.0",
      "com.typesafe.akka"          %% "akka-http-spray-json" % "10.0.0",
      "com.typesafe.akka"          %% "akka-http-testkit"    % "10.0.0" % "test",
      "com.typesafe.scala-logging" %% "scala-logging"        % "3.5.0",
      "ch.qos.logback"             % "logback-classic"       % "1.2.3",
      "org.scalatest"              % "scalatest_2.11"        % "3.0.1" % "test",
      "org.mockito"                % "mockito-all"           % "1.10.19" % "test"
    )
  )
