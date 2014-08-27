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
    "org.scalanlp" %% "breeze-natives" % "0.9",
    "org.jfree" % "jfreechart" % "1.0.19",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

mainClass := Some("com.hccl.nlip.Main")

mainClass in assembly := Some("com.hccl.nlip.Main")