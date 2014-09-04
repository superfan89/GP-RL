import AssemblyKeys._
name :="GP-RL"

scalaVersion :="2.11.2"

version :="0.1"

resolvers ++=
 List(
     "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
            "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
 )

libraryDependencies  ++= Seq(
    "org.scalanlp" %% "breeze" % "0.9",
//    "org.scalanlp" %% "breeze-natives" % "0.9",
    "org.jfree" % "jfreechart" % "1.0.19",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

// Turn this on in cross-jvm-compilation
//javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-bootclasspath", "C:\\Program Files (x86)\\Java\\jre7\\lib\\rt.jar")

mainClass := Some("com.hccl.nlip.GUIStandalone")

mainClass in (Compile, run) := Some("com.hccl.nlip.GUIStandalone")

mainClass in assembly := Some("com.hccl.nlip.GUIStandalone")