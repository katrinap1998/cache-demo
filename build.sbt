ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "cats-ref-cache-demo",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"         % "3.5.4",
      "org.typelevel" %% "munit-cats-effect"   % "2.0.0"  % Test
    ),
    // One suite at a time, so the naive version's global Ref is not shared across suites
    Test / parallelExecution := false
  )
