package giteri

import java.io.File
import java.util

import giteri.meme.entite.EntiteHandler
import giteri.network.networkStuff.NetworkConstructor
import giteri.run.IHMStub
import giteri.run.configurator.Configurator
import giteri.run.controller.Controller
import giteri.run.interfaces.Interfaces
import giteri.run.jarVersion.{JarVersion, StatAndPlotJarVersion, WorkerFactoryJarVersion}
import giteri.tool.other.WriteNRead

object Run {

  def run(network: File, one: Double, two: Double, three: Double, four: Double, five : Double, seed: Long):Double ={ //region Param
    val rand = new java.util.Random(seed)
    JarVersion.run(network, one, two, three, four, five)
    // val s = io.Source.fromFile(network).getLines()
   //  (math.abs(one * 2 - 10) + rand.nextGaussian(), math.abs(10 - two) + rand.nextGaussian())

  }



}

object Appli extends App{
  println("tefh")
}
