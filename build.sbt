import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "MBFFZ"

version := "0.1"

scalaVersion := "2.12.8"

val copyFrontendFastOpt = taskKey[File]("Return main process fast compiled file directory.")
lazy val fastOptCompileCopy = taskKey[Unit]("Compile and copy paste projects and generate corresponding json file.")

val copyFrontendFullOpt = taskKey[File]("Return main process full compiled file directory.")
lazy val fullOptCompileCopy = taskKey[Unit]("Compile and copy paste projects, and generate corresponding json file.")


lazy val `shared` = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)
  .settings(
    libraryDependencies ++= Seq(
      "fr.hmil" %%% "roshttp" % "2.1.0",
      "io.suzaku" %%% "boopickle" % "1.3.0",
      "com.lihaoyi" %%% "upickle" % "0.7.1",
      // scala tags for making type safe html
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-stubs" % "1.0.0-RC1"
    )
  )

lazy val sharedJVM = `shared`.jvm
lazy val sharedJS = `shared`.js

lazy val `server` = project.in(file("backend"))
  .settings(
    libraryDependencies ++= Seq(
      // cask
      "com.lihaoyi" %% "cask" % "0.1.9",
      "com.lihaoyi" %% "ujson" % "0.7.1"
    )
  )
  .dependsOn(sharedJVM)

lazy val `frontend` = project.in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      "com.raquo" %%% "laminar" % "0.6"
    ),
    scalaJSUseMainModuleInitializer := true,
    copyFrontendFastOpt := {
      (fastOptJS in Compile).value.data
    },
    copyFrontendFullOpt := {
      (fullOptJS in Compile).value.data
    }
  )
  .dependsOn(sharedJS)

val copyPath: String = "backend/src/main/resources/frontend/"


fullOptCompileCopy := {
  val frontendDirectory = (copyFrontendFullOpt in `frontend`).value
  IO.copyFile(frontendDirectory, baseDirectory.value / copyPath / "frontend-scala.js")
  IO.copyFile(
    frontendDirectory.getParentFile / "frontend-opt.js.map",
    baseDirectory.value / copyPath / "frontend-opt.js.map"
  )
}

fastOptCompileCopy := {
  val frontendDirectory = (copyFrontendFastOpt in `frontend`).value
  IO.copyFile(frontendDirectory, baseDirectory.value / copyPath / "frontend-scala.js")
  IO.copyFile(
    frontendDirectory.getParentFile / "frontend-fastopt.js.map",
    baseDirectory.value / copyPath / "frontend-fastopt.js.map"
  )
}

run in Compile := (run in Compile in server).evaluated
