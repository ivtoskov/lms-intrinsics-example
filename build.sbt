name := "lms-intrinsics-example"

version := "1.0"

scalaVersion := "2.11.2"

// LMS uses Scala Virtualized
scalaOrganization := "org.scala-lang.virtualized"

scalacOptions += "-Yvirtualize"

// LMS dependency
libraryDependencies += "org.scala-lang.lms" % "lms-core_2.11" % "0.9.0"

// Include the resolver to get the latest snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

// support for unsigned primives in Scala
libraryDependencies += "ch.ethz.acl" %% "scala-unsigned" % "0.1-SNAPSHOT"

// the main lms-intrinsics package
libraryDependencies += "ch.ethz.acl" %% "lms-intrinsics" % "0.0.2-SNAPSHOT"

