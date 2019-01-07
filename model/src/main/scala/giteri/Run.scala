package giteri

import java.io.File

import giteri.run.jarVersion.JarVersion

object Run {
  def run(network: File, one: Double, oneActi: Integer, two: Double, twoActi: Integer,
          three: Double, threeActi: Integer, four: Double, fourActi: Integer,
          five : Double, fiveActi: Integer, six: Double, sixActi:Integer,
          sevn: Double, sevnActi: Integer, hei: Double, heiActi: Integer,
          nine: Double, nineActi: Integer, seed: Long):Double ={ //region Param
    val rand = new java.util.Random(seed)
    JarVersion.run(network,
      oneActi.==(1),twoActi.==(1), threeActi.==(1), fourActi.==(1), fiveActi.==(1), sixActi.==(1), sevnActi.==(1), heiActi.==(1), nineActi.==(1),
      one, two, three, four, five, six, sevn, hei, nine)

    // val s = io.Source.fromFile(network).getLines()
   //  (math.abs(one * 2 - 10) + rand.nextGaussian(), math.abs(10 - two) + rand.nextGaussian())

  }
}

object Appli extends App{
  println("tefh")
}
