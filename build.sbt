name := """scala-ldap"""

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies += "org.specs2" %% "specs2-core" % "2.4.14" % "test"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.3"

libraryDependencies += "com.unboundid" % "unboundid-ldapsdk" % "2.3.8"

