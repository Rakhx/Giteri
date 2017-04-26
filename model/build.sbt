
name := "giteri"
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.11"

libraryDependencies += "org.jfree" % "jfreechart" % "1.0.19"
libraryDependencies += "org.graphstream" % "gs-core" % "1.3"
libraryDependencies += "org.graphstream" % "gs-algo" % "1.3"
libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"

OsgiKeys.exportPackage := Seq("giteri.*")
OsgiKeys.importPackage := Seq("*;resolution:=optional")
OsgiKeys.privatePackage := Seq("!scala.*,*")

enablePlugins(SbtOsgi)
