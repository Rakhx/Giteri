package giteri

import java.io.File
import java.util

import giteri.meme.entite.EntiteHandler
import giteri.network.networkStuff.NetworkConstructor
import giteri.run.IHMStub
import giteri.run.configurator.Configurator
import giteri.run.controller.Controller
import giteri.run.interfaces.Interfaces
import giteri.run.jarVersion.{StatAndPlotJarVersion, WorkerFactoryJarVersion}
import giteri.tool.other.WriteNRead

object Run {

  def run(network: File, one: Double, two: Double, seed: Long) = { // Region Param
    val rand = new java.util.Random(seed)
    val s = io.Source.fromFile(network).getLines()
    (math.abs(one * 2 - 10) + rand.nextGaussian(), math.abs(10 - two) + rand.nextGaussian())


    //    val probaBehavior = new util.ArrayList[Double]
//    val debug = Configurator.overallDebug
//
//    probaBehavior.addAll(util.Arrays.asList(one, two, three, four, five))
//
//    // EndRegion
//    Configurator.methodOfGeneration = Configurator.MemeDistributionType.FollowingFitting
//    Configurator.displayPlotWhileSimulation = false
//    Configurator.withGraphicalDisplay = false
//    Configurator.turboMode = true
//    Configurator.systemPaused = false
//    Configurator.jarMode = true
//    Configurator.setThreadSleepMultiplicateur(0)
//
//    val nc = NetworkConstructor.getInstance
//    val eh = EntiteHandler.getInstance
//
//    val c = new Controller
//    val vControl = new Controller#VueController
//    val mControl = new Controller#ModelController(vControl)
//
//    val fenetre = new IHMStub
//    vControl.setView(fenetre.asInstanceOf[Interfaces.IView])
//
//    eh.setIHMController(vControl)
//    eh.addMemeListener(WorkerFactoryJarVersion.getInstance.getDrawer)
//    eh.addEntityListener(WorkerFactoryJarVersion.getInstance.getCalculator)
//
//    val nl = mControl.getReader
//    WriteNRead.getInstance.readAndCreateNetwork(network, nl, " ", "#")
//
//    val stat = StatAndPlotJarVersion.getInstance
//    stat.probaVoulu = probaBehavior
//
//    eh.suspend()
//    nc.suspend()
//    nc.start()
//    eh.start()
//    stat.fitNetwork(0)
//    -1
  }


}
