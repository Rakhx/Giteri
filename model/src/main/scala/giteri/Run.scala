package giteri

import java.io.File
import java.util

import giteri.meme.entite.EntiteHandler
import giteri.network.networkStuff.NetworkConstructor
import giteri.run.configurator.Configurator
import giteri.run.controller.Controller
import giteri.run.displaysStuff.IHMStub
import giteri.run.interfaces.Interfaces
import giteri.run.jarVersion.{JarVersion, StatAndPlotJarVersion, WorkerFactoryJarVersion}
import giteri.tool.other.WriteNRead

object Run {
  def run(network: File, one: Double, oneActi: Boolean, two: Double, twoActi: Boolean,
          three: Double, threeActi: Boolean, four: Double, fourActi: Boolean,
          five : Double, fiveActi: Boolean, six: Double, sixActi:Boolean,
          sevn: Double, sevnActi: Boolean, hei: Double, heiActi: Boolean,
          nine: Double, nineActi: Boolean, seed: Long):Double ={ //region Param
    val rand = new java.util.Random(seed)
    JarVersion.run(network,
      oneActi,twoActi, threeActi, fourActi, fiveActi, sixActi, sevnActi, heiActi, nineActi,
      one, two, three, four, five, six, sevn, hei, nine)

    // val s = io.Source.fromFile(network).getLines()
   //  (math.abs(one * 2 - 10) + rand.nextGaussian(), math.abs(10 - two) + rand.nextGaussian())

  }
}

object Appli extends App{
  println("tefh")
}
