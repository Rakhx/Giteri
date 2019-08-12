
name := "giteri"
version := "1.0-SNAPSHOT"
scalaVersion := "2.12.8"

exportJars := true

libraryDependencies += "org.jfree" % "jfreechart" % "1.0.19"
libraryDependencies += "org.graphstream" % "gs-core" % "1.3"
libraryDependencies += "org.graphstream" % "gs-algo" % "1.3"
libraryDependencies += "org.graphstream" % "gs-ui" % "1.3"

libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"

enablePlugins(SbtOsgi)

OsgiKeys.exportPackage := Seq("giteri.*, org.graphstream.*")
OsgiKeys.importPackage := Seq("*;resolution:=optional")
OsgiKeys.privatePackage := Seq("!scala.*,*")

mainClass in (Compile, run ) := Some("giteri.run.Main")
mainClass in (Compile, packageBin ) := Some("giteri.run.Main")
